package com.eaglebank.security;

import com.eaglebank.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final Key key;
    private final long EXPIRATION = 1000 * 60 * 60;

    public JwtUtil(Key key) {
        this.key = key;
    }

    public String generateToken(final User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    public JwtUserDetails validateTokenAndGetUserDetails(final String token) {
        try {
            final Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            final String userId = claims.get("userId", String.class);
            final String email = claims.getSubject();

            return new JwtUserDetails(userId, email);
        } catch (JwtException e) {
            return null;
        }
    }
}
