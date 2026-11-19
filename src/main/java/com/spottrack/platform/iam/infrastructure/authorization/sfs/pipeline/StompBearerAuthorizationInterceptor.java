package com.spottrack.platform.iam.infrastructure.authorization.sfs.pipeline;

import com.spottrack.platform.iam.application.internal.outboundservices.tokens.TokenService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Authenticates STOMP sessions with the same JWT the REST API uses.
 *
 * <p>Browsers can't add headers to a WebSocket handshake, so {@code /ws/**} is open at HTTP level and
 * the token travels in the {@code Authorization} native header of the STOMP {@code CONNECT} frame.
 * A CONNECT without a valid token, or for a disabled user, is rejected and the session never opens,
 * so no frame of an unauthenticated client reaches the broker.</p>
 */
@Component
public class StompBearerAuthorizationInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;

    public StompBearerAuthorizationInterceptor(TokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }
        var token = extractBearerToken(accessor.getFirstNativeHeader(AUTHORIZATION_HEADER));
        if (token == null || !tokenService.validateToken(token)) {
            throw new MessageDeliveryException(message, "STOMP CONNECT requires a valid Bearer token");
        }
        var userDetails = userDetailsService.loadUserByUsername(tokenService.getUsernameFromToken(token));
        if (!userDetails.isEnabled()) {
            throw new MessageDeliveryException(message, "STOMP CONNECT rejected: user is disabled");
        }
        accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        return message;
    }

    private static String extractBearerToken(String header) {
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
