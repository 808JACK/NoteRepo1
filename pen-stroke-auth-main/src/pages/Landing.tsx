import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ArrowRight, BookOpen, Lock, Sparkles } from "lucide-react";
import { Link } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";

const Landing = () => {
  const { isAuthenticated, user } = useAuth();

  return (
    <div className="min-h-screen gradient-secondary">
      <div className="container mx-auto px-4 py-16">
        {/* Header */}
        <header className="text-center mb-16">
          <div className="inline-flex items-center gap-2 mb-6">
            <div className="p-3 rounded-xl gradient-primary shadow-glow">
              <BookOpen className="h-8 w-8 text-white" />
            </div>
            <h1 className="text-4xl font-bold text-gradient">NotesApp</h1>
          </div>
          <p className="text-xl text-muted-foreground max-w-2xl mx-auto leading-relaxed">
            Your personal digital notebook. Create, organize, and access your thoughts 
            anywhere, anytime with our beautiful and intuitive note-taking experience.
          </p>
        </header>

        {/* Hero Section */}
        <div className="text-center mb-16">
          {isAuthenticated ? (
            <div className="flex flex-col items-center justify-center min-h-[40vh] gap-6">
              <div className="text-center">
                <p className="text-2xl font-semibold text-gradient mb-2">
                  Welcome back, {user?.username}!
                </p>
                <p className="text-muted-foreground">
                  Ready to continue your note-taking journey?
                </p>
              </div>
              <Link to="/notes">
                <Button variant="gradient" size="lg" className="group">
                  Go to My Notes <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                </Button>
              </Link>
            </div>
          ) : (
            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
              <Link to="/signup">
                <Button variant="gradient" size="lg" className="group">
                  Get Started <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                </Button>
              </Link>
              <Link to="/login">
                <Button variant="glass" size="lg">
                  <Lock className="mr-2 h-4 w-4" />
                  Sign In
                </Button>
              </Link>
            </div>
          )}
        </div>

        {/* Features */}
        <div className="grid md:grid-cols-3 gap-8 max-w-4xl mx-auto">
          <Card className="gradient-card shadow-card border-0 hover:shadow-primary/20 hover:scale-105 transition-bounce">
            <CardHeader className="text-center">
              <div className="mx-auto mb-4 p-3 rounded-full bg-primary/10">
                <BookOpen className="h-6 w-6 text-primary" />
              </div>
              <CardTitle>Rich Note Taking</CardTitle>
              <CardDescription>
                Create and organize your notes with a beautiful, distraction-free interface
              </CardDescription>
            </CardHeader>
          </Card>

          <Card className="gradient-card shadow-card border-0 hover:shadow-primary/20 hover:scale-105 transition-bounce">
            <CardHeader className="text-center">
              <div className="mx-auto mb-4 p-3 rounded-full bg-primary/10">
                <Lock className="h-6 w-6 text-primary" />
              </div>
              <CardTitle>Secure Access</CardTitle>
              <CardDescription>
                Your notes are protected with secure authentication and encrypted storage
              </CardDescription>
            </CardHeader>
          </Card>

          <Card className="gradient-card shadow-card border-0 hover:shadow-primary/20 hover:scale-105 transition-bounce">
            <CardHeader className="text-center">
              <div className="mx-auto mb-4 p-3 rounded-full bg-primary/10">
                <Sparkles className="h-6 w-6 text-primary" />
              </div>
              <CardTitle>Smart Features</CardTitle>
              <CardDescription>
                Edit, delete, and manage your notes with intuitive controls and smooth animations
              </CardDescription>
            </CardHeader>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default Landing;