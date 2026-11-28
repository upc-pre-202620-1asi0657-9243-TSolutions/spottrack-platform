package com.spottrack.platform.gym.interfaces.acl;

/**
 * Read-only view of a gym branch for other bounded contexts, so they do not
 * depend on the gym's Branch entity.
 *
 * @param branchId the branch UUID
 * @param gymId    UUID of the gym that owns it
 * @param name     display name
 * @param address  street address, may be null
 */
public record BranchSnapshot(
        String branchId,
        String gymId,
        String name,
        String address) {
}
