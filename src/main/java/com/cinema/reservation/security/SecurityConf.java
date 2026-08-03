package com.cinema.reservation.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConf {
  private final JwtAuthFilter jwtAuthFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/ping", "/health/**")
                    .permitAll()
                    .requestMatchers("/error")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/reservations")
                    .hasAnyRole("EMPLOYEE", "MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/reservations")
                    .hasAnyRole("EMPLOYEE", "MANAGER")
                    .requestMatchers(HttpMethod.GET, "/reservations/**")
                    .authenticated()
                    .requestMatchers(HttpMethod.PUT, "/movies")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/projections")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.GET, "/projections")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(
                        (request, response, authException) -> {
                          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                          response.setContentType("application/json");
                          response.getWriter().write("{\"error\":\"Unauthorized\"}");
                        })
                    .accessDeniedHandler(
                        (request, response, accessDeniedException) -> {
                          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                          response.setContentType("application/json");
                          response.getWriter().write("{\"error\":\"Forbidden\"}");
                        }))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
