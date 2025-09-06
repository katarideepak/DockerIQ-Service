package com.dockeriq.service.security;

import com.dockeriq.service.model.User;
import com.dockeriq.service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil = new JwtUtil();
    
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // remove "Bearer "
            
            try {
                String username = jwtUtil.extractUsername(token);
                
                // Debug logging
                log.info("JWT Debug - Username: {}", username);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtUtil.validateToken(token, username)) {
                        // Fetch user role from database using username (email)
                        Optional<User> userOptional = userRepository.findByEmail(username);
                        
                        if (userOptional.isPresent()) {
                            User user = userOptional.get();
                            String role = user.getRole();
                            
                            // check if role is valid
                            if (role == null || role.isEmpty() || !role.equals(jwtUtil.extractRole(token))) {
                                log.warn("JWT Debug - Role is null or empty: {}", username);
                                handleJwtException(response, "Role is null or empty or does not match", HttpStatus.FORBIDDEN);
                                return;
                            }

                            // Check if user is active
                            if (user.getActive() == null || !user.getActive()) {
                                log.warn("JWT Debug - User is inactive: {}", username);
                                handleJwtException(response, "User account is inactive", HttpStatus.FORBIDDEN);
                                return;
                            }
                            
                            log.info("JWT Debug - Username: {}, Role from DB: {}", username, role);
                            
                            // Create authorities from role (ensure uppercase)
                            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
                            );
                            
                            log.info("JWT Debug - Created authorities: {}", authorities);
                            
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            // Set authentication in context
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            log.info("JWT Debug - Authentication set in context");
                        } else {
                            log.warn("JWT Debug - User not found in database: {}", username);
                            handleJwtException(response, "User not found", HttpStatus.UNAUTHORIZED);
                            return;
                        }
                    } else {
                        log.warn("JWT Debug - Token validation failed for user: {}", username);
                    }
                } else {
                    log.warn("JWT Debug - Username is null or authentication already exists");
                }
            } catch (ExpiredJwtException ex) {
                handleJwtException(response, "JWT token has expired", HttpStatus.UNAUTHORIZED);
                return;
            } catch (MalformedJwtException ex) {
                handleJwtException(response, "Invalid JWT token format", HttpStatus.UNAUTHORIZED);
                return;
            } catch (SignatureException ex) {
                handleJwtException(response, "Invalid JWT signature", HttpStatus.UNAUTHORIZED);
                return;
            } catch (Exception ex) {
                handleJwtException(response, "JWT token validation failed", HttpStatus.UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
    
    private void handleJwtException(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", "Authentication Error");
        errorResponse.put("message", message);
        
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), errorResponse);
    }
}

