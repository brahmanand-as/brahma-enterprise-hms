package com.brahma.arogya.security.service;

import com.brahma.arogya.security.repository.JwtFilterJdbcRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {

  private final JwtFilterJdbcRepository db;

  private final JwtService jwt;

  private final UserDetailsService users;

  public JwtFilter(JwtService jwt, UserDetailsService users, JwtFilterJdbcRepository db) {
    this.jwt = jwt;
    this.users = users;
    this.db = db;
  }

  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String h = req.getHeader("Authorization");
    if (h != null && h.startsWith("Bearer ")) {
      try {
        var user = users.loadUserByUsername(jwt.subject(h.substring(7)));
        if (jwt.version(h.substring(7))
                != db.readStaffAccountsForDoFilterInternal(user.getUsername())
            || !user.isEnabled()) throw new IllegalArgumentException();
        SecurityContextHolder.getContext()
            .setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
      } catch (Exception ex) {
        SecurityContextHolder.clearContext();
        res.sendError(401, "Invalid or expired token");
        return;
      }
    }
    chain.doFilter(req, res);
  }
}
