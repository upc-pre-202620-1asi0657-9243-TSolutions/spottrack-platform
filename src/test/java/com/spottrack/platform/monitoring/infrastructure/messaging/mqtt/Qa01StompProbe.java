package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import org.springframework.messaging.Message;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** Listens to /topic/monitoring/session-trackers/{id} the way the frontend does and stamps each message on arrival. */
final class Qa01StompProbe implements AutoCloseable {

    private final BlockingQueue<Long> arrivals = new LinkedBlockingQueue<>();
    private final WebSocketStompClient client;
    private final ThreadPoolTaskScheduler scheduler;
    private final StompSession session;

    private Qa01StompProbe(WebSocketStompClient client, ThreadPoolTaskScheduler scheduler, StompSession session) {
        this.client = client;
        this.scheduler = scheduler;
        this.session = session;
    }

    /** /ws is not in the permitAll list of WebSecurityConfiguration, so the handshake carries the JWT as a Bearer header. */
    static Qa01StompProbe connect(String apiBaseUrl, String jwt, String sessionTrackerId) throws Exception {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("qa01-stomp-");
        scheduler.initialize();
        var client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new AbstractMessageConverter(MimeTypeUtils.ALL) {
            @Override
            protected boolean supports(Class<?> clazz) {
                return byte[].class == clazz;
            }

            @Override
            protected Object convertFromInternal(Message<?> message, Class<?> targetClass, Object hint) {
                return message.getPayload();
            }
        });
        client.setTaskScheduler(scheduler);

        var handshake = new WebSocketHttpHeaders();
        handshake.add("Authorization", "Bearer " + jwt);
        var webSocketUrl = apiBaseUrl.replaceFirst("^http", "ws").replaceAll("/+$", "") + "/ws/websocket";
        var session = client.connectAsync(webSocketUrl, handshake, new StompHeaders(), new StompSessionHandlerAdapter() {
                    @Override
                    public void handleException(StompSession s, StompCommand c, StompHeaders h, byte[] p, Throwable e) {
                        System.err.println("STOMP: frame error: " + e);
                    }

                    @Override
                    public void handleTransportError(StompSession s, Throwable e) {
                        System.err.println("STOMP: transport error: " + e);
                    }
                }).get(10, TimeUnit.SECONDS);
        session.setAutoReceipt(true);

        var probe = new Qa01StompProbe(client, scheduler, session);
        var subscribed = new CountDownLatch(1);
        var subscription = session.subscribe("/topic/monitoring/session-trackers/" + sessionTrackerId,
                new StompSessionHandlerAdapter() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return byte[].class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        probe.arrivals.add(System.nanoTime());
                    }
                });
        subscription.addReceiptTask(subscribed::countDown);
        subscribed.await(5, TimeUnit.SECONDS);
        return probe;
    }

    void drain() {
        arrivals.clear();
    }

    /** Nanotime of the first message that arrived at or after {@code sinceNanos}, or -1 when none arrives before {@code deadlineNanos}. */
    long awaitArrival(long sinceNanos, long deadlineNanos) throws InterruptedException {
        while (true) {
            var remaining = deadlineNanos - System.nanoTime();
            var arrival = arrivals.poll(Math.max(remaining, 0), TimeUnit.NANOSECONDS);
            if (arrival == null) return -1;
            if (arrival >= sinceNanos) return arrival;
        }
    }

    @Override
    public void close() {
        if (session.isConnected()) session.disconnect();
        client.stop();
        scheduler.shutdown();
    }
}
