package com.eaglebank.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.eaglebank.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtUtilUTest {

    Key sharedKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final JwtUtil jwtUtil = new JwtUtil(sharedKey);

    @Test
    void generateToken_and_validateToken_successfully() {
        final User user = new User();
        user.setId("123L");
        user.setEmail("test@example.com");

        final String token = jwtUtil.generateToken(user);
        final JwtUserDetails userDetails = jwtUtil.validateTokenAndGetUserDetails(token);

        assertNotNull(token);
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.email());
        assertEquals("123L", userDetails.id());
    }

    @Test
    void validateTokenAndGetUserDetails_invalidToken_returnsNull() {
        final String invalidToken = "this.is.not.a.valid.token";

        final JwtUserDetails result = jwtUtil.validateTokenAndGetUserDetails(invalidToken);

        assertNull(result);
    }

    @Test
    void validateTokenAndGetUserDetails_expiredToken_returnsNull() {
        final User user = new User();
        user.setId("1L");
        user.setEmail("expired@example.com");

        final JwtUtil expiringUtil = new JwtUtil(sharedKey) {
            @Override
            public String generateToken(User user) {
                return Jwts.builder()
                        .setSubject(user.getEmail())
                        .claim("userId", user.getId())
                        .setIssuedAt(new Date(System.currentTimeMillis() - 2_000))
                        .setExpiration(new Date(System.currentTimeMillis() - 1_000)) // expired
                        .compact();
            }
        };

        final String expiredToken = expiringUtil.generateToken(user);

        final JwtUserDetails userDetails = expiringUtil.validateTokenAndGetUserDetails(
                expiredToken);

        assertNull(userDetails);
    }
}