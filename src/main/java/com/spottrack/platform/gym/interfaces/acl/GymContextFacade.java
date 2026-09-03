package com.spottrack.platform.gym.interfaces.acl;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;

import java.util.List;
import java.util.Optional;

public interface GymContextFacade {

    void updateEquipmentStatus(String equipmentId, EquipmentStatus status);

    Optional<Equipment> findEquipmentById(String equipmentId);

    /**
     * Available equipment of the same name/kind, excluding the given equipmentId.
     * Backs the Reservation context's "View Alternatives" read model.
     */
    List<Equipment> findAvailableAlternatives(String equipmentName, String excludeEquipmentId);

    /**
     * Returns the IAM userId of the Admin who owns the given gym.
     * Returns 0L if the gym does not exist.
     */
    Long fetchAdminUserIdByGymId(String gymId);

    boolean isDniWhitelistedForGym(String gymId, String dni);

    // Returns the gymId for the branch that owns the given zone, empty if zone or branch not found.
    Optional<String> resolveGymIdForZone(String zoneId);

    // Returns the gymId for the gym that owns the given equipment, empty if equipment, zone, or branch not found.
    Optional<String> resolveGymIdForEquipment(String equipmentId);

    // Returns true if the gym exists and its adminUserId matches the given adminUserId.
    boolean isGymOwnedByAdmin(String gymId, Long adminUserId);

    // Returns all Equipment aggregates belonging to any gym owned by the given admin.
    List<Equipment> findEquipmentsByAdminUserId(Long adminUserId);

    // Returns all Equipment aggregates belonging to the given gym (across its branches and zones).
    List<Equipment> findEquipmentsByGymId(String gymId);
}
