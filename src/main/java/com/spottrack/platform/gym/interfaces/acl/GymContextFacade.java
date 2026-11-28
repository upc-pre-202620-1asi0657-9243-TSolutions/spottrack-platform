package com.spottrack.platform.gym.interfaces.acl;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.entities.Branch;
import com.spottrack.platform.shared.domain.model.valueobjects.EquipmentStatus;

import java.util.List;
import java.util.Optional;

public interface GymContextFacade {

    void updateEquipmentStatus(String equipmentId, EquipmentStatus status);

    /** @deprecated Exposes the gym domain model; use {@link #findEquipmentSnapshotById(String)} instead. Kept until analytics and notification migrate (RF-6). */
    @Deprecated
    Optional<Equipment> findEquipmentById(String equipmentId);

    /**
     * Available equipment of the same name/kind, excluding the given equipmentId.
     * Backs the Reservation context's "View Alternatives" read model.
     */
    /** @deprecated Exposes the gym domain model; use {@link #findAvailableAlternativeSnapshots(String, String)} instead. Kept until analytics and notification migrate (RF-6). */
    @Deprecated
    List<Equipment> findAvailableAlternatives(String equipmentName, String excludeEquipmentId);

    /**
     * Returns the IAM userId of the Admin who owns the given gym.
     * Returns 0L if the gym does not exist.
     */
    Long fetchAdminUserIdByGymId(String gymId);

    boolean isDniWhitelistedForGym(String gymId, String dni);

    /**
     * Creates a gym owned by the given admin. Idempotent: if the admin already owns a gym with the
     * same name (case-insensitive), nothing is created. Throws if the gym cannot be created.
     */
    void provisionGym(Long adminUserId, String gymName);

    // Returns the gymId for the branch that owns the given zone, empty if zone or branch not found.
    Optional<String> resolveGymIdForZone(String zoneId);

    // Returns the gymId for the gym that owns the given equipment, empty if equipment, zone, or branch not found.
    Optional<String> resolveGymIdForEquipment(String equipmentId);

    // Returns true if the gym exists and its adminUserId matches the given adminUserId.
    boolean isGymOwnedByAdmin(String gymId, Long adminUserId);

    // Returns all Equipment aggregates belonging to any gym owned by the given admin.
    /** @deprecated Exposes the gym domain model; use {@link #findEquipmentSnapshotsByAdminUserId(Long)} instead. Kept until analytics and notification migrate (RF-6). */
    @Deprecated
    List<Equipment> findEquipmentsByAdminUserId(Long adminUserId);

    // Returns all Equipment aggregates belonging to the given gym (across its branches and zones).
    /** @deprecated Exposes the gym domain model; use {@link #findEquipmentSnapshotsByGymId(String)} instead. Kept until analytics and notification migrate (RF-6). */
    @Deprecated
    List<Equipment> findEquipmentsByGymId(String gymId);

    // Returns the gymId that owns the given branch, empty if the branch is not found.
    Optional<String> resolveGymIdForBranch(String branchId);

    // Returns all Equipment aggregates placed in any zone of the given branch.
    /** @deprecated Exposes the gym domain model; use {@link #findEquipmentSnapshotsByBranchId(String)} instead. Kept until analytics and notification migrate (RF-6). */
    @Deprecated
    List<Equipment> findEquipmentsByBranchId(String branchId);

    // Returns the name of the given gym, empty if the gym is not found.
    Optional<String> findGymName(String gymId);

    // Returns the name of the given branch, empty if the branch is not found.
    Optional<String> findBranchName(String branchId);

    // Returns the branches of the given gym, empty if the gym has none or does not exist.
    /** @deprecated Exposes the gym domain model; use {@link #findBranchSnapshotsByGymId(String)} instead. Kept until analytics and notification migrate (RF-6). */
    @Deprecated
    List<Branch> findBranchesByGymId(String gymId);

    // ─── Snapshot queries: ACL views that do not expose the gym domain model ───

    // Returns the equipment with the given UUID, empty if it does not exist.
    Optional<EquipmentSnapshot> findEquipmentSnapshotById(String equipmentId);

    // Available equipment with the same name, excluding the given equipmentId.
    List<EquipmentSnapshot> findAvailableAlternativeSnapshots(String equipmentName, String excludeEquipmentId);

    // Returns the equipment of every gym owned by the given admin.
    List<EquipmentSnapshot> findEquipmentSnapshotsByAdminUserId(Long adminUserId);

    // Returns the equipment of the given gym (across its branches and zones).
    List<EquipmentSnapshot> findEquipmentSnapshotsByGymId(String gymId);

    // Returns the equipment placed in any zone of the given branch.
    List<EquipmentSnapshot> findEquipmentSnapshotsByBranchId(String branchId);

    // Returns the branches of the given gym, empty if the gym has none or does not exist.
    List<BranchSnapshot> findBranchSnapshotsByGymId(String gymId);
}
