package ch.epai.ict.m295.messaging.backend.api.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.security.Token;
import ch.epai.ict.m295.messaging.backend.domain.security.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private TokenRepository tokenRepository;
    
    public TokenAuthenticationFilter(TokenRepository tokenRepository) {
            super();
            this.tokenRepository = tokenRepository;
    }

    @Override
    protected void doFilterInternal(
                        @NonNull HttpServletRequest request,
                        @NonNull HttpServletResponse response, 
                        @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        User principal = null;

        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            Token token = Token.fromString(authorization.substring(7));
            principal = tokenRepository.getUserFromToken(token);
            if (principal != null) {
                request.setAttribute("principal", principal);
                request.setAttribute("token", token.toString());
            }
        }

        if (principal != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + principal.getRole().toString()));
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
