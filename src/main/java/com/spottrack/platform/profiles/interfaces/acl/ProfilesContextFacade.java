package com.spottrack.platform.profiles.interfaces.acl;

import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

import java.util.Optional;

public interface ProfilesContextFacade {
    Long fetchClientIdByEmail(String email);
    Long fetchAdminIdByEmail(String email);

    /**
     * Returns the DNI string for the Client or Admin profile associated with the given email.
     * Returns "" if no profile exists, the profile is incomplete (no DNI set), or the
     * account type does not carry a DNI (e.g. Business).
     * Checks Client first, then Admin.
     */
    String fetchDniByEmail(String email);

    /**
     * Creates the Admin profile of the given IAM user. Idempotent: an existing profile is a success.
     * Returns the userId on success, or the reason the profile could not be created.
     */
    Result<Long, ApplicationError> provisionAdminProfile(Long userId, String email,
                                                         String firstName, String lastName,
                                                         String phoneNumber, String dni);

    /**
     * Creates the BusinessProfile of the given IAM user. Idempotent: an existing profile is a success.
     * Returns the userId on success, or the reason the profile could not be created.
     */
    Result<Long, ApplicationError> provisionBusinessProfile(Long userId, String companyName, String ruc,
                                                            String legalStructure, String companyPhone, String companyEmail,
                                                            String streetAddress, String city, String district);

    /**
     * Returns the gymId of the active gym for the given client.
     * Returns "" if the client has no active gym or if the active gym's whitelist entry was removed.
     */
    String fetchActiveGymIdByClientId(Long clientId);

    /**
     * Returns the client's full name, or null if no client with that id exists
     * or their profile is incomplete.
     */
    String fetchClientNameById(Long clientId);

    /**
     * Returns the email (the account username) of the client, or empty if no client with that id exists.
     */
    Optional<String> fetchClientEmailById(Long clientId);

    /**
     * Returns true if the client identified by clientId has an active association
     * with the gym identified by gymId.
     */
    boolean hasActiveAssociationWithGym(Long clientId, String gymId);

    /**
     * Returns true if the client is associated with the gym (active or not) and their DNI is still
     * whitelisted there. Returns false if the client has no complete profile.
     */
    boolean hasAssociationWithGym(Long clientId, String gymId);
}
