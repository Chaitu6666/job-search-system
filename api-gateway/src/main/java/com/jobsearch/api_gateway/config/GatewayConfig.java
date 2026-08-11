package com.jobsearch.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {

        return builder.routes()

                // ==========================
                // USER SERVICE
                // ==========================
                .route("user-service-auth", r -> r
                        .path("/api/auth/**")
                        .uri("lb://user-service"))

                .route("user-service-users", r -> r
                        .path("/api/users/**")
                        .uri("lb://user-service"))

                // ==========================
                // JOB SERVICE
                // ==========================
                .route("job-service", r -> r
                        .path("/api/jobs/**")
                        .uri("lb://job-service"))

                // ==========================
                // APPLICATION SERVICE
                // ==========================
                .route("application-service", r -> r
                        .path("/api/applications/**")
                        .uri("lb://application-service"))

                // ==========================
                // MESSAGE SERVICE
                // ==========================
                .route("message-service", r -> r
                        .path("/api/messages/**")
                        .uri("lb://message-service"))

                // ==========================
                // JOBBASKET SERVICE
                // ==========================
                .route("jobbasket-service", r -> r
                        .path("/api/basket/**")
                        .uri("lb://jobbasket-service"))

                // ==========================
                // USER SERVICE DOCS
                // ==========================
                .route("user-service-docs", r -> r
                        .path("/user-service/v3/api-docs",
                                "/user-service/v3/api-docs/**")
                        .filters(f -> f.rewritePath(
                                "/user-service(?<segment>.*)",
                                "/${segment}"))
                        .uri("lb://user-service"))

                // ==========================
                // JOB SERVICE DOCS
                // ==========================
                .route("job-service-docs", r -> r
                        .path("/job-service/v3/api-docs",
                                "/job-service/v3/api-docs/**")
                        .filters(f -> f.rewritePath(
                                "/job-service(?<segment>.*)",
                                "/${segment}"))
                        .uri("lb://job-service"))

                // ==========================
                // APPLICATION SERVICE DOCS
                // ==========================
                .route("application-service-docs", r -> r
                        .path("/application-service/v3/api-docs",
                                "/application-service/v3/api-docs/**")
                        .filters(f -> f.rewritePath(
                                "/application-service(?<segment>.*)",
                                "/${segment}"))
                        .uri("lb://application-service"))

                // ==========================
                // MESSAGE SERVICE DOCS
                // ==========================
                .route("message-service-docs", r -> r
                        .path("/message-service/v3/api-docs",
                                "/message-service/v3/api-docs/**")
                        .filters(f -> f.rewritePath(
                                "/message-service(?<segment>.*)",
                                "/${segment}"))
                        .uri("lb://message-service"))

                // ==========================
                // JOBBASKET SERVICE DOCS
                // ==========================
                .route("jobbasket-service-docs", r -> r
                        .path("/jobbasket-service/v3/api-docs",
                                "/jobbasket-service/v3/api-docs/**")
                        .filters(f -> f.rewritePath(
                                "/jobbasket-service(?<segment>.*)",
                                "/${segment}"))
                        .uri("lb://jobbasket-service"))

                .build();
    }
}
















//package com.jobsearch.api_gateway.config;
//
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class GatewayConfig {
//
//    @Bean
//    public RouteLocator swaggerDocsRoutes(RouteLocatorBuilder builder) {
//        return builder.routes()
//
//                // ── user-service docs ─────────────────────────────────
//                .route("user-service-docs", r -> r
//                        .path("/user-service/v3/api-docs",
//                                "/user-service/v3/api-docs/**")
//                        .filters(f -> f.rewritePath(
//                                "/user-service(?<segment>.*)",
//                                "/${segment}"))
//                        .uri("lb://user-service"))
//
//                // ── job-service docs ──────────────────────────────────
//                .route("job-service-docs", r -> r
//                        .path("/job-service/v3/api-docs",
//                                "/job-service/v3/api-docs/**")
//                        .filters(f -> f.rewritePath(
//                                "/job-service(?<segment>.*)",
//                                "/${segment}"))
//                        .uri("lb://job-service"))
//
//                // ── application-service docs ──────────────────────────
//                .route("application-service-docs", r -> r
//                        .path("/application-service/v3/api-docs",
//                                "/application-service/v3/api-docs/**")
//                        .filters(f -> f.rewritePath(
//                                "/application-service(?<segment>.*)",
//                                "/${segment}"))
//                        .uri("lb://application-service"))
//
//                // ── message-service docs ──────────────────────────────
//                .route("message-service-docs", r -> r
//                        .path("/message-service/v3/api-docs",
//                                "/message-service/v3/api-docs/**")
//                        .filters(f -> f.rewritePath(
//                                "/message-service(?<segment>.*)",
//                                "/${segment}"))
//                        .uri("lb://message-service"))
//
//                // ── jobbasket-service docs ────────────────────────────
//                .route("jobbasket-service-docs", r -> r
//                        .path("/jobbasket-service/v3/api-docs",
//                                "/jobbasket-service/v3/api-docs/**")
//                        .filters(f -> f.rewritePath(
//                                "/jobbasket-service(?<segment>.*)",
//                                "/${segment}"))
//                        .uri("lb://jobbasket-service"))
//
//                .build();
//    }
//}
//
//





































//package com.jobsearch.api_gateway.config;
//
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//// Routes are defined in application.yml via spring.cloud.gateway.routes
//// This class is kept for any programmatic route overrides in the future
//// and to confirm the Gateway bean context loads correctly
//@Configuration
//public class GatewayConfig {
//
//    // Uncomment below to define routes programmatically instead of YAML
//    // (YAML routes are preferred — cleaner and easier to maintain)
//
//    /*
//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//            .route("user-service", r -> r
//                .path("/api/auth/**", "/api/users/**")
//                .uri("lb://user-service"))
//            .route("job-service", r -> r
//                .path("/api/jobs/**")
//                .uri("lb://job-service"))
//            .route("application-service", r -> r
//                .path("/api/applications/**")
//                .uri("lb://application-service"))
//            .route("message-service", r -> r
//                .path("/api/messages/**")
//                .uri("lb://message-service"))
//            .route("jobbasket-service", r -> r
//                .path("/api/basket/**")
//                .uri("lb://jobbasket-service"))
//            .build();
//    }
//    */
//}
