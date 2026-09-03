package com.spottrack.platform.reservation.infrastructure.scheduling.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Provides the TaskScheduler used by ReservationTimerStartedEventHandler to fire
 * the timer-expiry task at the exact timerExpiry instant.
 * Pool size of 5 means up to 5 timers can be tracked concurrently.
 */
@Configuration
@EnableScheduling
public class ReservationSchedulerConfiguration {

    @Bean
    public TaskScheduler reservationTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("reservation-timer-");
        return scheduler;
    }
}
