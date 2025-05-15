package com.heslin.postopia.gateway.filter;

import com.heslin.postopia.common.jwt.JWTService;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RefreshScope
@Order(-1)
public class JWTFilter implements GlobalFilter {
    private final JWTService jwtService;

    public JWTFilter(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/user/auth")) {
            System.out.println("Skipping JWT validation for /user/auth endpoint");
            return chain.filter(exchange);
        }
        String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
        System.out.println("Authorization: " + authorization);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            if (jwtService.validateToken(token)) {
                Long userId = jwtService.extractUserId(token);
                String username = jwtService.extractUsername(token);
                // Set the userId and username in the request attributes or headers if needed
                exchange.getRequest().mutate().header("userId", String.valueOf(userId)).header("username", username).build();
                return chain.filter(exchange);
            }
        }
        System.out.println("unorthorized");
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
