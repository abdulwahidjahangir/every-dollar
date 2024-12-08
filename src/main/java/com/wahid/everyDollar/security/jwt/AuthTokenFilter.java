package com.wahid.everyDollar.security.jwt;

import com.wahid.everyDollar.security.service.UserDetailServiceImpl;
import com.wahid.everyDollar.security.service.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        logger.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());

        try {
            String jwtToken = parseJwt(request);

            if (jwtToken != null && jwtUtils.validateJwtToken(jwtToken)) {
                String userEmail = jwtUtils.getEmailFromToken(jwtToken);

                UserDetailsImpl userDetails = (UserDetailsImpl) userDetailService.loadUserByUsername(userEmail);

                if (!userDetails.isVerified()) {
                    logger.error("User not verified: {}", userEmail);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    String jsonResponse = "{ \"message\": \"Please verify your account using the email we have sent you.\", " +
                            "\"isVerified\": false, \"isBlocked\": " + userDetails.isBlocked() + " }";

                    response.getWriter().write(jsonResponse);
                    return;
                }

                if (userDetails.isBlocked()) {
                    logger.error("User is blocked: {}", userEmail);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    String jsonResponse = "{ \"message\": \"This account is blocked. Please contact support.\", " +
                            "\"isVerified\": true, \"isBlocked\": true }";

                    response.getWriter().write(jsonResponse);
                    return;
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                logger.debug("Roles from JWT: {}", userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

            }

        } catch (Exception e) {
            logger.error("Can't set user authenticatio: {}", e);
        }
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String token = jwtUtils.getJwtFromHeader(request);
        logger.debug("AuthTokenFilter.java: {}", token);
        return token;
    }
}
