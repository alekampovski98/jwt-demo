package io.intertec.jwtdemo.config;

import io.intertec.jwtdemo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final String BEARER = "Bearer ";

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    ofNullable(authHeader)
        .filter(h -> h.startsWith(BEARER))
        .map(h -> h.replace(BEARER, ""))
        .ifPresent(t -> authenticateUser(t, request));
    filterChain.doFilter(request, response);
  }

  private void authenticateUser(String token, HttpServletRequest request) {
    final String email = jwtService.extractEmail(token);
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(email);
      generateAuthenticationToken(token, userDetails)
          .ifPresent(t -> {
            t.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(t);
          });
    }
  }

  private Optional<UsernamePasswordAuthenticationToken> generateAuthenticationToken(String token, UserDetails userDetails) {
    UsernamePasswordAuthenticationToken authenticationToken = null;
    if (jwtService.isTokenValid(token, userDetails)) {
      authenticationToken = new UsernamePasswordAuthenticationToken(
          userDetails,
          userDetails.getPassword(),
          userDetails.getAuthorities()
      );
    }
    return ofNullable(authenticationToken);
  }
}
