package com.heslin.postopia.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.service.jwt.JWTService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PostopiaAuthenticationFilter extends BasicAuthenticationFilter {

    @Autowired
    private JWTService jwtService;

    @Value("#{'${postopia.open.apis}'.split(',')}")
    private List<String> openApis;

    public PostopiaAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        boolean isOpenApi = openApis.contains(req.getRequestURI());
        boolean isAuthApi = req.getRequestURI().startsWith("/auth");
        String header = req.getHeader("Authorization");
        if ((header == null || !header.startsWith("Bearer ") ) && !isOpenApi || isAuthApi) {
            chain.doFilter(req, res);
            return;
        }

        Authentication authentication = getAuthentication(req, isOpenApi);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request, boolean isOpenApi) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            authorization = "Bearer invalid";
        }

        String token = authorization.split(" ")[1];
        try {
            if (jwtService.validateToken(token)) {
                Long userId = jwtService.extractUserId(token);
                String username = jwtService.extractUsername(token);
                User user = User.builder().id(userId).username(username).build();
                return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
            }
            
            return null;
        } catch (Exception e) {
            if (isOpenApi) {
                return new UsernamePasswordAuthenticationToken(null, null, new ArrayList<>());
            }
            System.err.println(e);
            return null;
        }

    }
    
}
