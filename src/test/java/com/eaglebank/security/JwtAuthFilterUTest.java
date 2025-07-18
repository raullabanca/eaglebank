package com.eaglebank.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eaglebank.model.User;
import com.eaglebank.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterUTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws Exception {
        final String token = "valid.jwt.token";
        final String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);

        final var jwtUserDetails = new JwtUserDetails("1L", "user@email.com");
        when(jwtUtil.validateTokenAndGetUserDetails(token)).thenReturn(jwtUserDetails);

        final User mockUser = new User();
        mockUser.setEmail("user@email.com");
        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(mockUser));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        final var auth = SecurityContextHolder.getContext()
                .getAuthentication();
        assertNotNull(auth);
        assertEquals("user@email.com", ((AuthenticatedUser) auth.getPrincipal()).getUsername());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_noHeader_doesNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext()
                .getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidUser_doesNotAuthenticate() throws Exception {
        final String token = "valid.jwt.token";
        final String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);

        final var jwtUserDetails = new JwtUserDetails("1L", "notfound@email.com");
        when(jwtUtil.validateTokenAndGetUserDetails(token)).thenReturn(jwtUserDetails);

        when(userRepository.findByEmail("notfound@email.com")).thenReturn(Optional.empty());

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext()
                .getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}