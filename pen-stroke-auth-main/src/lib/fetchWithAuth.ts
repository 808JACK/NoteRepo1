export async function fetchWithAuth(url: string, options: RequestInit = {}) {
  const token = localStorage.getItem("accessToken");

  // Merge headers
  const mergedHeaders: Record<string, string> = {
    ...(options.headers as Record<string, string> || {}),
    "Content-Type": "application/json",
  };

  if (token) {
    mergedHeaders["Authorization"] = `Bearer ${token}`;
  }

  options.headers = mergedHeaders;

  const response = await fetch(url, options);

  // Update access token if Gateway returns a new one
  const newToken = response.headers.get("X-New-Access-Token");
  
  if (newToken) {
    localStorage.setItem("accessToken", newToken);
    
    // Also update the user object in localStorage if it exists
    const userData = localStorage.getItem("user");
    if (userData) {
      try {
        const user = JSON.parse(userData);
        user.accessToken = newToken;
        localStorage.setItem("user", JSON.stringify(user));
      } catch (e) {
        console.error("Error updating user data with new token:", e);
      }
    }
    
    // Update token cookie if it exists (for backend compatibility)
    document.cookie = `token=${newToken}; path=/; SameSite=Lax`;
  }

  // Check if refresh token expired
  const refreshExpired = response.headers.get("X-Refresh-Expired");
  if (refreshExpired === "true") {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("userId");
    localStorage.removeItem("user");
    window.location.href = "/login";
    return; // stop execution
  }

  // Parse response data
  let data: unknown = null;
  const contentType = response.headers.get("Content-Type") || "";
  try {
    if (contentType.includes("application/json")) {
      data = await response.json();
    } else {
      // try text for empty bodies
      const text = await response.text();
      data = text ? { message: text } : {};
    }
  } catch (parseError) {
    console.error("Failed to parse response:", parseError);
    data = { message: "Failed to parse response" };
  }

  // Handle 401 Unauthorized specifically
  if (response.status === 401) {
    // If no refresh token expired header, this might be a token validation issue
    if (!refreshExpired) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("userId");
      localStorage.removeItem("user");
      window.location.href = "/login";
      return;
    }
  }

  // Check if request failed
  if (!response.ok) {
    const errorMessage = (data && typeof data === 'object' && 'message' in data && data.message) 
      ? String(data.message) 
      : `Request failed with status ${response.status}`;
    throw new Error(errorMessage);
  }

  return data;
}