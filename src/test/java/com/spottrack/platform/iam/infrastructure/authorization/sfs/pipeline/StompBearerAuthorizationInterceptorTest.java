package com.spottrack.platform.iam.infrastructure.authorization.sfs.pipeline;

import com.spottrack.platform.iam.application.internal.outboundservices.tokens.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StompBearerAuthorizationInterceptorTest {

    private final MessageChannel channel = mock(MessageChannel.class);
    private TokenService tokenService;
    private UserDetailsService userDetailsService;
    private StompBearerAuthorizationInterceptor interceptor;

    @BeforeEach
    void setUp() {
        tokenService = mock(TokenService.class);
        userDetailsService = mock(UserDetailsService.class);
        interceptor = new StompBearerAuthorizationInterceptor(tokenService, userDetailsService);
    }

    @Test
    void connectWithAValidTokenAuthenticatesTheSession() {
        when(tokenService.validateToken("good")).thenReturn(true);
        when(tokenService.getUsernameFromToken("good")).thenReturn("ana");
        when(userDetailsService.loadUserByUsername("ana"))
                .thenReturn(User.withUsername("ana").password("x").roles("CLIENT").build());

        var result = interceptor.preSend(frame(StompCommand.CONNECT, "Bearer good"), channel);

        var accessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertThat(accessor).isNotNull();
        assertThat(accessor.getUser()).isNotNull();
        assertThat(accessor.getUser().getName()).isEqualTo("ana");
    }

    @Test
    void connectWithoutATokenIsRejected() {
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, null), channel))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void connectWithAnInvalidTokenIsRejected() {
        when(tokenService.validateToken("bad")).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, "Bearer bad"), channel))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void connectForADisabledUserIsRejected() {
        when(tokenService.validateToken("good")).thenReturn(true);
        when(tokenService.getUsernameFromToken("good")).thenReturn("ana");
        when(userDetailsService.loadUserByUsername("ana"))
                .thenReturn(User.withUsername("ana").password("x").roles("CLIENT").disabled(true).build());

        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, "Bearer good"), channel))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void framesOtherThanConnectPassThrough() {
        var subscribe = frame(StompCommand.SUBSCRIBE, null);

        assertThat(interceptor.preSend(subscribe, channel)).isSameAs(subscribe);
    }

    private static Message<byte[]> frame(StompCommand command, String authorization) {
        var accessor = StompHeaderAccessor.create(command);
        if (authorization != null) {
            accessor.setNativeHeader("Authorization", authorization);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
