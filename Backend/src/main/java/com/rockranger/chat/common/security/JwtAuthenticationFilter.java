package com.rockranger.chat.common.security;

import com.rockranger.chat.profile.domain.UserProfile;
import com.rockranger.chat.profile.domain.UserProfileRepository;
import com.rockranger.chat.session.domain.UserSession;
import com.rockranger.chat.session.domain.UserSessionRepository;
import com.rockranger.chat.user.domain.User;
import com.rockranger.chat.user.domain.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSessionRepository userSessionRepository;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   UserRepository userRepository,
                                   UserProfileRepository userProfileRepository,
                                   UserSessionRepository userSessionRepository) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                Claims claims = jwtTokenService.parseAndValidateToken(token);
                String sub = claims.getSubject();
                String sidStr = claims.get("sid", String.class);

                if (sub != null && sidStr != null) {
                    UUID userId = UUID.fromString(sub);
                    UUID sessionId = UUID.fromString(sidStr);

                    // Check if session is active in database
                    Optional<UserSession> sessionOpt = userSessionRepository.findById(sessionId);
                    if (sessionOpt.isPresent() && sessionOpt.get().isActive()) {
                        Optional<User> userOpt = userRepository.findById(userId);
                        if (userOpt.isPresent()) {
                            User user = userOpt.get();
                            Optional<UserProfile> profileOpt = userProfileRepository.findById(userId);
                            String username = profileOpt.map(UserProfile::getUsername).orElse(null);

                            CurrentUser currentUser = new CurrentUser(
                                    user.getId(),
                                    sessionId,
                                    user.getEmailNormalized(),
                                    username,
                                    user.getStatus()
                            );

                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    currentUser,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("JWT authentication failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
