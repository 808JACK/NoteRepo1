package com.example.demo.Filter;

import com.example.demo.Entities.User;
import com.example.demo.Repo.jpa.AuthRepo;
import com.example.demo.Services.AuthServiceImpl;
import com.example.demo.Services.JwtService;
import com.example.demo.Services.UserService;
import com.example.demo.Util.TokenValidation;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AuthRepo authRepo;
    @Autowired
    @Lazy
    private JwtService jwtService;
    private final UserService userService;
    private final TokenValidation tokenValidation;
    private final ApplicationContext applicationContext;
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        log.info("[JwtAuthFilter] Incoming {} {}", method, path);

        try {
            // Allow unauthenticated access to auth endpoints and public resources
            if (path.startsWith("/auth/") || path.equals("/") || 
                path.startsWith("/uploads/") || path.startsWith("/actuator/")) {
                log.info("[JwtAuthFilter] Allowing unauthenticated access to: {}", path);
                filterChain.doFilter(request, response);
                return;
            }

            // Handle OPTIONS preflight requests
            if ("OPTIONS".equals(method)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = extractTokenFromRequest(request);
            
            if (token == null) {
                log.warn("[JwtAuthFilter] No token found for protected endpoint: {} {}", method, path);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Authentication required\"}");
                return;
            }

            log.info("[JwtAuthFilter] Token found, validating for {} {}", method, path);

            // Check if token is valid and not expired
            if (tokenValidation.isValid(token) && !tokenValidation.isExpired(token)) {
                log.info("[JwtAuthFilter] Token valid, proceeding with request");
                setAuthenticationContext(token, request);
                filterChain.doFilter(request, response);
            } else {
                // Token is expired or invalid, try to refresh
                Long userId = tokenValidation.extractUserId(token);
                if (userId == null) {
                    log.warn("[JwtAuthFilter] Could not extract userId from token");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Invalid token\"}");
                    return;
                }

                log.info("[JwtAuthFilter] Token expired/invalid, attempting refresh for user {}", userId);
                
                try {
                    AuthServiceImpl authService = applicationContext.getBean(AuthServiceImpl.class);
                    String newAccessToken = authService.refreshAT(userId);
                    
                    if ("REFRESH_EXPIRED".equals(newAccessToken)) {
                        log.warn("[JwtAuthFilter] Refresh token expired for user {}", userId);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setHeader("X-Refresh-Expired", "true");
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Session expired, please login again\"}");
                        return;
                    } else if (newAccessToken != null && !newAccessToken.isBlank()) {
                        log.info("[JwtAuthFilter] New access token generated for user {}", userId);
                        
                        // Set new token in response header for frontend to update
                        response.setHeader("X-New-Access-Token", newAccessToken);
                        
                        // Set authentication context with new token
                        setAuthenticationContext(newAccessToken, request);
                        filterChain.doFilter(request, response);
                    } else {
                        log.warn("[JwtAuthFilter] Token refresh failed for user {}", userId);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Token refresh failed\"}");
                        return;
                    }
                } catch (Exception e) {
                    log.error("[JwtAuthFilter] Error during token refresh: {}", e.getMessage());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Authentication failed\"}");
                    return;
                }
            }
        } catch (Exception ex) {
            log.error("Filter error for URI {}: {}", request.getRequestURI(), ex.getMessage(), ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authentication failed\"}");
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        // First try Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }

        // Then try cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private void setAuthenticationContext(String token, HttpServletRequest request) {
        try {
            Long userId = jwtService.getUserIdFromToken(token);
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = authRepo.findUserById(userId);
                if (user != null) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    authenticationToken.setDetails(userId); // Set userId as details
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (Exception e) {
            log.error("Error setting authentication context: {}", e.getMessage());
        }
    }
}
