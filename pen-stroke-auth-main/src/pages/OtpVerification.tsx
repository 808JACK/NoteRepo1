import { useState, useRef, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { BookOpen, ArrowLeft } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { useToast } from "@/hooks/use-toast";
import { apiService } from "@/lib/api";

const OtpVerification = () => {
  const [otp, setOtp] = useState(["", "", "", "", "", ""]);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { toast } = useToast();
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  useEffect(() => {
    // Focus first input on mount
    inputRefs.current[0]?.focus();
    
    // Check if signup data exists
    const signupDataStr = localStorage.getItem('signupData');
    if (!signupDataStr) {
      toast({
        title: "Error",
        description: "No signup data found. Please sign up first.",
        variant: "destructive",
      });
      navigate("/signup");
    }
  }, [navigate, toast]);

  const handleChange = (index: number, value: string) => {
    if (value.length > 1) return; // Only allow single digit
    
    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);

    // Auto-focus next input
    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent) => {
    if (e.key === "Backspace" && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const otpValue = otp.join("");
    
    if (otpValue.length !== 6) {
      toast({
        title: "Error",
        description: "Please enter all 6 digits",
        variant: "destructive",
      });
      return;
    }

    // Get signup data from localStorage
    const signupDataStr = localStorage.getItem('signupData');
    if (!signupDataStr) {
      toast({
        title: "Error",
        description: "Signup data not found. Please sign up again.",
        variant: "destructive",
      });
      navigate("/signup");
      return;
    }

    const signupData = JSON.parse(signupDataStr);

    setIsLoading(true);
    try {
      const response = await apiService.verifyOtp(signupData.email, otpValue, signupData);
      
      if (response.success) {
        // Clear signup data
        localStorage.removeItem('signupData');
        toast({
          title: "Success",
          description: response.message || "Email verified successfully! Please sign in.",
        });
        navigate("/login");
      } else {
        toast({
          title: "Error",
          description: response.message || "Invalid OTP",
          variant: "destructive",
        });
      }
    } catch (error) {
      console.error('OTP verification error:', error);
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "OTP verification failed. Please try again.",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen gradient-secondary flex items-center justify-center p-4">
      <Card className="w-full max-w-md gradient-card shadow-card border-0">
        <CardHeader className="text-center">
          <Link to="/signup" className="inline-flex items-center gap-2 mb-4 text-muted-foreground hover:text-primary transition-smooth">
            <ArrowLeft className="h-4 w-4" />
            Back to Signup
          </Link>
          <div className="mx-auto mb-4 p-3 rounded-xl gradient-primary shadow-glow">
            <BookOpen className="h-6 w-6 text-white" />
          </div>
          <CardTitle className="text-2xl text-gradient">Verify Your Email</CardTitle>
          <CardDescription>
            Enter the 6-digit code sent to your email
          </CardDescription>
          <div className="mt-4 p-3 bg-primary/10 rounded-lg">
            <p className="text-sm text-primary font-medium">Check your email for the 6-digit verification code</p>
          </div>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="flex gap-2 justify-center">
              {otp.map((digit, index) => (
                <Input
                  key={index}
                  ref={(el) => (inputRefs.current[index] = el)}
                  type="text"
                  inputMode="numeric"
                  maxLength={1}
                  value={digit}
                  onChange={(e) => handleChange(index, e.target.value)}
                  onKeyDown={(e) => handleKeyDown(index, e)}
                  className="w-12 h-12 text-center text-lg font-bold transition-smooth focus:shadow-primary/20"
                />
              ))}
            </div>
            <Button 
              type="submit" 
              className="w-full" 
              variant="gradient"
              disabled={isLoading}
            >
              {isLoading ? "Verifying..." : "Verify Email"}
            </Button>
          </form>
          <div className="mt-6 text-center">
            <Button 
              variant="link" 
              className="text-sm"
              onClick={async () => {
                const signupDataStr = localStorage.getItem('signupData');
                if (signupDataStr) {
                  try {
                    const signupData = JSON.parse(signupDataStr);
                    await apiService.signup(signupData);
                    toast({
                      title: "OTP Resent",
                      description: "A new verification code has been sent to your email",
                    });
                  } catch (error) {
                    toast({
                      title: "Error",
                      description: "Failed to resend OTP. Please try again.",
                      variant: "destructive",
                    });
                  }
                }
              }}
            >
              Resend Code
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default OtpVerification;