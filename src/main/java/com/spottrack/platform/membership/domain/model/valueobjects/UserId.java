package com.spottrack.platform.membership.domain.model.valueobjects;

import java.util.UUID;


public record UserId(Long id) {
  public UserId {
    if (id == null || id == 0) {
      throw new IllegalArgumentException("id must not be null or 0");
    }

  }

}
