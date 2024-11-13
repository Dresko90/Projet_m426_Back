package ch.epai.ict.m295.message_api.api.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import ch.epai.ict.m295.message_api.domain.User;
import ch.epai.ict.m295.message_api.domain.UserDirectory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${app.jwt.secretkey}")
    private String secretKey;

    private UserDirectory userDirectory;

    public JwtAuthenticationFilter(UserDirectory userDirectory) {
        super();
        this.userDirectory = userDirectory;
    }

    @Override
    protected void doFilterInternal(
                        @NonNull HttpServletRequest request,
                        @NonNull HttpServletResponse response, 
                        @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String role = null;
        String username = null;
   
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            Claims claims = extractClaims(authorization.substring(7));

            User user = this.userDirectory.getUser(Long.parseLong(claims.getSubject()));
            username = user.getEmail();
            role = extractRole(claims);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(username, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private Claims extractClaims(String token) {

        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String extractRole(Claims claims) {
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles", List.class);
        return roles.get(0);
    }
}
