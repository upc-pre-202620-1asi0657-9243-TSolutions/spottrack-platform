package com.spottrack.platform.iam.application.internal.outboundservices.hashing;

public interface HashingService {
    boolean matches(String rawPassword, String encodedPassword);
    String encode(String rawPassword);
}
