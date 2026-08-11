package com.jobsearch.api_gateway.filter;

import com.jobsearch.api_gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = List.of(
            // ── Auth endpoints ────────────────────────────────────────────
            "/api/auth/register",
            "/api/auth/login",
            // ── Swagger UI static paths ───────────────────────────────────
            "/swagger-ui.html",
            "/swagger-ui",
            "/webjars",
            // ── Gateway's own api-docs ────────────────────────────────────
            "/v3/api-docs",
            // ── Per-service api-docs ──────────────────────────────────────
            "/user-service/v3/api-docs",
            "/job-service/v3/api-docs",
            "/application-service/v3/api-docs",
            "/message-service/v3/api-docs",
            "/jobbasket-service/v3/api-docs"
    );

    @Override
    public int getOrder() {
        return -2;
//        return 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.debug("Incoming request: {} {}", request.getMethod(), path);

        if (isPublicPath(path)) {
            log.debug("Public path — skipping JWT check: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or malformed Authorization header for: {}", path);
            return unauthorizedResponse(exchange,
                    "Authorization header missing or invalid");
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Invalid or expired JWT token for: {}", path);
            return unauthorizedResponse(exchange,
                    "Token is invalid or expired");
        }

        Long userId     = jwtUtil.extractUserId(token);
        String userType = jwtUtil.extractUserType(token);
        String username = jwtUtil.extractUsername(token);

        log.debug("JWT valid — userId: {}, userType: {}, username: {}",
                userId, userType, username);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id",   String.valueOf(userId))
                .header("X-User-Type", userType)
                .header("X-Username",  username)
                .build();

        return chain.filter(exchange.mutate()
                .request(mutatedRequest)
                .build());
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
                .anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange,
                                            String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"error\": \"%s\", \"status\": 401}", message);
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}