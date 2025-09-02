import React, { createContext, useContext, useState, useEffect } from "react";
import { LoginResponse } from "@/lib/api";

interface AuthContextType {
  user: LoginResponse | null;
  isAuthenticated: boolean;
  login: (userData: LoginResponse) => void;
  logout: () => void;
  updateToken: (newToken: string) => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<LoginResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Security cleanup: Remove refresh token from localStorage if it exists
    if (localStorage.getItem("refreshToken")) {
      localStorage.removeItem("refreshToken");
    }
    
    // Check if user is logged in on app start
    const savedUser = localStorage.getItem("user");
    if (savedUser) {
      try {
        const userData = JSON.parse(savedUser);
        setUser(userData);
      } catch (error) {
        console.error("Error parsing saved user data:", error);
        localStorage.removeItem("user");
      }
    }
    setIsLoading(false);
  }, []);

  const login = (userData: LoginResponse) => {
    setUser(userData);
    localStorage.setItem("user", JSON.stringify(userData));
    localStorage.setItem("accessToken", userData.accessToken);
    localStorage.setItem("userId", userData.userId.toString());
    // DO NOT store refresh token in localStorage for security
  };

  const logout = () => {
    setUser(null);
    // Clear all localStorage tokens
    localStorage.removeItem("user");
    localStorage.removeItem("accessToken");
    localStorage.removeItem("userId");
    localStorage.removeItem("refreshToken"); // Extra cleanup
    
    // Clear any cookies by setting them to expire
    document.cookie = "token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";

    
    // Optional: Call backend logout endpoint to invalidate tokens server-side
    try {
      fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8085'}/auth/logout`, {
        method: 'POST',
        credentials: 'include'
      }).catch(() => {
        // Ignore errors - logout should work even if backend is down
      });
    } catch (error) {
      // Ignore errors - logout should work even if backend is down
    }
  };

  const updateToken = (newToken: string) => {
    if (user) {
      const updatedUser = { ...user, accessToken: newToken };
      setUser(updatedUser);
      localStorage.setItem("user", JSON.stringify(updatedUser));
      localStorage.setItem("accessToken", newToken);
    }
  };

  const value = {
    user,
    isAuthenticated: !!user,
    login,
    logout,
    updateToken,
    isLoading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
