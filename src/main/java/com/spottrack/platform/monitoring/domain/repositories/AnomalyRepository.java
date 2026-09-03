package com.spottrack.platform.monitoring.domain.repositories;

import com.spottrack.platform.monitoring.domain.model.aggregates.Anomaly;
import org.springframework.stereotype.Repository;

@Repository
public interface AnomalyRepository {
    Anomaly save(Anomaly anomaly);

}
