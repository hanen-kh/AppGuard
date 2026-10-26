package com.sfm.pfe.agent_cron.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${agent.api-key}")
    private String validApiKey;




    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String path = request.getRequestURI();

        // 🚫 On ignore le filtre pour /ping
        if ("/ping".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 🔐 Vérification de l'API Key pour les autres routes
        String apiKey = request.getHeader("Authorization");
        if (apiKey == null || !apiKey.equals("Bearer " + validApiKey)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing Authorization header");

            return;
        }

        chain.doFilter(request, response);
    }

}
