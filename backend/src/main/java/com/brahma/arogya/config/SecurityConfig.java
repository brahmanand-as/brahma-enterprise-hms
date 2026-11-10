package com.brahma.arogya.config;

import com.brahma.arogya.config.repository.SecurityConfigJdbcRepository;
import com.brahma.arogya.security.service.AccessPolicy;
import com.brahma.arogya.security.service.JwtFilter;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
  public UserDetailsService users(
      SecurityConfigJdbcRepository db,
      PasswordEncoder encoder,
      @Value("${hms.admin.username:admin}") String admin,
      @Value("${hms.admin.password:}") String password) {
    if (password.length() < 12)
      throw new IllegalStateException("Set HMS_ADMIN_PASSWORD (at least 12 characters)");
    Integer count = db.countStaffAccountsForUsers(admin);
    if (count != null && count == 0)
      db.insertStaffAccountsForUsers(admin, encoder.encode(password), "ADMIN");
    return username -> {
      var rows = db.findStaffAccountsForUsers(username);
      if (rows.isEmpty()) throw new UsernameNotFoundException("Invalid login");
      var row = rows.get(0);
      var authorities = new ArrayList<String>();
      authorities.add("ROLE_" + row.get("role"));
      authorities.addAll(db.findRolePermissionsForUsers(row.get("role")));
      return User.withUsername(username)
          .password(String.valueOf(row.get("password_hash")))
          .authorities(authorities.toArray(String[]::new))
          .disabled(
              !Boolean.TRUE.equals(row.get("enabled"))
                  && !Integer.valueOf(1).equals(row.get("enabled")))
          .build();
    };
  }

  @Bean
  public SecurityFilterChain chain(
      HttpSecurity http,
      @Value("${hms.cors.origin:http://localhost:4200}") String origin,
      JwtFilter jwtFilter,
      AccessPolicy policy)
      throws Exception {
    http.csrf(c -> c.disable())
        .cors(
            c ->
                c.configurationSource(
                    req -> {
                      CorsConfiguration cfg = new CorsConfiguration();
                      cfg.setAllowedOrigins(
                          Arrays.stream(origin.split(",")).map(String::trim).toList());
                      cfg.setAllowedMethods(
                          List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                      cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
                      return cfg;
                    }))
        .authorizeHttpRequests(
            a ->
                a.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR)
                    .permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(
                        "/api/auth/login",
                        "/api/public/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**")
                    .permitAll()
                    .anyRequest()
                    .access(
                        (authentication, context) ->
                            new org.springframework.security.authorization.AuthorizationDecision(
                                policy.allowed(authentication.get(), context.getRequest()))))
        .sessionManagement(
            c ->
                c.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
        .exceptionHandling(c -> c.authenticationEntryPoint((req, res, e) -> res.sendError(401)))
        .addFilterBefore(
            jwtFilter,
            org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
                .class);
    return http.build();
  }
}
