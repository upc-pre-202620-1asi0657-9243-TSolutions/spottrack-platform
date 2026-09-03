package com.spottrack.platform.iam.application.internal.outboundservices.tokens;

import java.util.List;

public interface TokenService {
    String generateToken(String username, List<String> roles);
    String getUsernameFromToken(String token);
    List<String> getRolesFromToken(String token);
    boolean validateToken(String token);
}
