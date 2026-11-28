package com.spottrack.platform.reservation.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationStatus;
import com.spottrack.platform.reservation.infrastructure.persistence.jpa.repositories.ReservationPersistenceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ReservationRepositoryImplTest {

    @Test
    void findByStatusIsEmptyInsteadOfNull() {
        var repository = new ReservationRepositoryImpl(mock(ReservationPersistenceRepository.class),
                mock(ApplicationEventPublisher.class));

        for (var status : ReservationStatus.values()) {
            assertEquals(Optional.empty(), repository.findByStatus(status));
        }
    }
}
