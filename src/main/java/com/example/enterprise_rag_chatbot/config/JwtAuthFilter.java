package com.example.enterprise_rag_chatbot.config;

import com.example.enterprise_rag_chatbot.service.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
        throws ServletException, IOException {

    System.out.println("===== JWT FILTER =====");
    System.out.println("Request URI: " + request.getRequestURI());

    String authHeader = request.getHeader("Authorization");
    System.out.println("Authorization Header: " + authHeader);

    if (authHeader != null && authHeader.startsWith("Bearer ")) {

        String token = authHeader.substring(7);
        System.out.println("Token: " + token);

        boolean valid = jwtUtil.isTokenValid(token);
        System.out.println("Is Token Valid? " + valid);

        if (valid) {
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            System.out.println("Username: " + username);
            System.out.println("Role: " + role);

            var authToken = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
            System.out.println("Authentication Set");
        }
    }

    filterChain.doFilter(request, response);
}
}