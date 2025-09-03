import { fetchWithAuth } from './fetchWithAuth';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8085';

export interface SignUpRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  userId: number;
  username: string;
  email: string;
  accessToken: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface Note {
  id?: string;
  title: string;
  content: string;
  userId: string;
  createdAt?: string;
  updatedAt?: string;
  category?: string;
  isArchived?: boolean;
}

class ApiService {
  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<ApiResponse<T>> {
    const url = `${API_BASE_URL}${endpoint}`;
    
    // Get stored user data for token
    const userData = localStorage.getItem('user');
    let token = null;
    if (userData) {
      try {
        const user = JSON.parse(userData);
        token = user.accessToken;
      } catch (e) {
        console.error('Error parsing user data:', e);
      }
    }
    
    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.headers,
      },
      credentials: 'include', // Include cookies for authentication
      ...options,
    };

    try {
      // Add 30 second timeout
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 30000);
      
      const response = await fetch(url, {
        ...config,
        signal: controller.signal
      });
      
      clearTimeout(timeoutId);
      
      // Check for new access token in response headers
      const newToken = response.headers.get('X-New-Access-Token');
      if (newToken && userData) {
        try {
          const user = JSON.parse(userData);
          user.accessToken = newToken;
          localStorage.setItem('user', JSON.stringify(user));
          localStorage.setItem('accessToken', newToken);

        } catch (e) {
          console.error('Error updating token:', e);
        }
      }

      // Check for refresh expired header
      const refreshExpired = response.headers.get('X-Refresh-Expired');
      if (refreshExpired === 'true') {
        // Clear user data and redirect to login
        localStorage.removeItem('user');
        localStorage.removeItem('accessToken');

        window.location.href = '/login';
        throw new Error('Session expired, please login again');
      }
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('API request failed:', error);
      
      // Handle network errors
      if (error instanceof TypeError && error.message.includes('fetch')) {
        throw new Error('Unable to connect to server. Please check if the backend is running.');
      }
      
      throw error;
    }
  }

  // Auth endpoints
  async signup(data: SignUpRequest): Promise<ApiResponse<string>> {
    return this.request<string>('/auth/signup', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async verifyOtp(email: string, otp: string, signUpData: SignUpRequest): Promise<ApiResponse<string>> {
    return this.request<string>(`/auth/verify-otp?email=${encodeURIComponent(email)}&otp=${otp}`, {
      method: 'POST',
      body: JSON.stringify(signUpData),
    });
  }

  async login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    const response = await this.request<LoginResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    
    // Store only access token in localStorage for fetchWithAuth
    if (response.success && response.data) {
      localStorage.setItem('accessToken', response.data.accessToken);
      localStorage.setItem('userId', response.data.userId.toString());
      // DO NOT store refresh token in localStorage for security
    }
    
    return response;
  }

  // Notes endpoints using fetchWithAuth
  async createNote(note: Note): Promise<Note> {
    const data = await fetchWithAuth(`${API_BASE_URL}/api/notes`, {
      method: 'POST',
      body: JSON.stringify(note),
    });

    return data as Note;
  }

  async getNotesByUserId(userId: string): Promise<Note[]> {
    const data = await fetchWithAuth(`${API_BASE_URL}/api/notes/user/${userId}`, {
      method: 'GET',
    });

    return data as Note[];
  }

  async updateNote(id: string, note: Note): Promise<Note> {
    const data = await fetchWithAuth(`${API_BASE_URL}/api/notes/${id}`, {
      method: 'PUT',
      body: JSON.stringify(note),
    });

    return data as Note;
  }

  async deleteNote(id: string): Promise<void> {
    await fetchWithAuth(`${API_BASE_URL}/api/notes/${id}`, {
      method: 'DELETE',
    });
  }

  async searchNotes(userId: string, query: string): Promise<Note[]> {
    const data = await fetchWithAuth(`${API_BASE_URL}/api/notes/user/${userId}/search?q=${encodeURIComponent(query)}`, {
      method: 'GET',
    });

    return data as Note[];
  }
}

export const apiService = new ApiService();