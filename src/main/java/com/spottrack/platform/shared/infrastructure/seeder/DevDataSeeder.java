package com.spottrack.platform.shared.infrastructure.seeder;

import com.spottrack.platform.analytics.application.commandservices
        .ActivityReportCommandService;
import com.spottrack.platform.analytics.application.commandservices.MaintenanceQuoteCommandService;
import com.spottrack.platform.analytics.application.commandservices.ROIProjectionCommandService;
import com.spottrack.platform.analytics.application.queryservices.MaintenanceQuoteQueryService;
import com.spottrack.platform.analytics.application.queryservices.ROIProjectionQueryService;
import com.spottrack.platform.analytics.domain.model.commands.RequestADetailedMaintenanceQuoteCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestActivityAnalysisCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestDowntimeCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestEarningsCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestPreventiveCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestRoiCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestSparePartsCommand;
import com.spottrack.platform.analytics.domain.model.queries.GetAllMaintenanceQuotesQuery;
import com.spottrack.platform.analytics.domain.model.queries.GetAllROIProjectionsQuery;
import com.spottrack.platform.analytics.domain.repositories.ActivityReportRepository;
import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.application.commandServices.GymCommandService;
import com.spottrack.platform.gym.domain.model.commands.AddBranchCommand;
import com.spottrack.platform.gym.domain.model.commands.AddDniToWhitelistCommand;
import com.spottrack.platform.gym.domain.model.commands.AddZoneCommand;
import com.spottrack.platform.gym.domain.model.commands.CreateGym;
import com.spottrack.platform.gym.domain.model.commands.RegisterEquipment;
import com.spottrack.platform.gym.domain.model.valueobjects.BranchId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.ManufacturerId;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.EquipmentPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.GymPersistenceRepository;
import com.spottrack.platform.iam.application.commandservices.RoleCommandService;
import com.spottrack.platform.iam.application.commandservices.UserCommandService;
import com.spottrack.platform.iam.domain.model.commands.SeedRolesCommand;
import com.spottrack.platform.iam.domain.model.commands.SignUpCommand;
import com.spottrack.platform.iam.domain.model.entities.Role;
import com.spottrack.platform.iam.domain.model.valueobjects.Roles;
import com.spottrack.platform.iam.domain.repositories.RoleRepository;
import com.spottrack.platform.iam.domain.repositories.UserRepository;
import com.spottrack.platform.maintenance.application.commandServices.MaintenanceCommandService;
import com.spottrack.platform.maintenance.application.queryservices.TechnicianQueryService;
import com.spottrack.platform.maintenance.domain.model.commands.AssignTechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.commands.CreateTechnician;
import com.spottrack.platform.maintenance.domain.model.commands.CreateTechnicalTicketCommand;
import com.spottrack.platform.maintenance.domain.model.commands.RegisterMaintenanceCompletion;
import com.spottrack.platform.maintenance.domain.model.commands.RequestMaintenance;
import com.spottrack.platform.maintenance.domain.model.queries.GetAllTechniciansQuery;
import com.spottrack.platform.maintenance.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicalTicketId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketPriority;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketType;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories.TechnicalTicketJpaRepository;
import com.spottrack.platform.membership.application.commandservices.MembershipCommandService;
import com.spottrack.platform.membership.domain.model.commands.ActivateMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.CreateMembershipCommand;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipStatus;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.membership.domain.repositories.MembershipRepository;
import com.spottrack.platform.profiles.application.commandservices.AdminCommandService;
import com.spottrack.platform.profiles.application.commandservices.ClientCommandService;
import com.spottrack.platform.profiles.application.queryservices.AdminQueryService;
import com.spottrack.platform.profiles.application.queryservices.ClientQueryService;
import com.spottrack.platform.profiles.domain.model.commands.AssociateClientWithGymCommand;
import com.spottrack.platform.profiles.domain.model.commands.UpdateAdminProfileCommand;
import com.spottrack.platform.profiles.domain.model.commands.UpdateClientProfileCommand;
import com.spottrack.platform.profiles.domain.model.queries.GetAdminByUserIdQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetClientByUserIdQuery;
import com.spottrack.platform.profiles.domain.model.valueobjects.AdminId;
import com.spottrack.platform.profiles.domain.model.valueobjects.ClientId;
import com.spottrack.platform.profiles.domain.model.valueobjects.PhoneNumber;
import com.spottrack.platform.reservation.application.commandServices.ReservationCommandService;
import com.spottrack.platform.reservation.application.queryservices.ReservationQueryService;
import com.spottrack.platform.reservation.domain.model.commands.InitiateExpressReservation;
import com.spottrack.platform.reservation.domain.model.queries.GetReservationsByClientIdQuery;
import com.spottrack.platform.reservation.domain.model.valueobjects.TimeInterval;
import com.spottrack.platform.routine.application.commandservices.RoutineCommandService;
import com.spottrack.platform.routine.application.commandservices.RoutineSessionCommandService;
import com.spottrack.platform.routine.application.queryservices.RoutineSessionQueryService;
import com.spottrack.platform.routine.domain.model.commands.AddExerciseBlockCommand;
import com.spottrack.platform.routine.domain.model.commands.CompleteRoutineCommand;
import com.spottrack.platform.routine.domain.model.commands.CreateRoutineCommand;
import com.spottrack.platform.routine.domain.model.commands.StartRoutineCommand;
import com.spottrack.platform.routine.domain.model.queries.GetAllRoutineSessionsByClientIdQuery;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseName;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseType;
import com.spottrack.platform.routine.domain.model.valueobjects.RoutineName;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

@Component
@Profile("dev")
@Slf4j
public class DevDataSeeder {

    private static final String ADMIN_EMAIL = "seedadmin@spottrack.com";
    private static final String CLIENT_EMAIL = "seedclient@spottrack.com";
    private static final String SEED_PASSWORD = "seed123";
    private static final String SEED_DNI = "87654321";
    private static final String ADMIN_DNI = "12345678";
    private static final String SEED_GYM_NAME = "Seed Gym";
    private static final String MANUFACTURER_ID = "00000000-0000-0000-0000-000000000001";
    private static final String DEBUG_CLIENT_EMAIL = "debugclient@spottrack.com";
    private static final String DEBUG_CLIENT_DNI = "11223344";

    private final RoleCommandService roleCommandService;
    private final UserCommandService userCommandService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AdminQueryService adminQueryService;
    private final AdminCommandService adminCommandService;
    private final ClientQueryService clientQueryService;
    private final ClientCommandService clientCommandService;
    private final GymCommandService gymCommandService;
    private final EquipmentCommandService equipmentCommandService;
    private final GymPersistenceRepository gymPersistenceRepository;
    private final EquipmentPersistenceRepository equipmentPersistenceRepository;
    private final MembershipCommandService membershipCommandService;
    private final MembershipRepository membershipRepository;
    private final ActivityReportRepository activityReportRepository;
    private final ActivityReportCommandService activityReportCommandService;
    private final MaintenanceQuoteCommandService maintenanceQuoteCommandService;
    private final MaintenanceQuoteQueryService maintenanceQuoteQueryService;
    private final ROIProjectionCommandService roiProjectionCommandService;
    private final ROIProjectionQueryService roiProjectionQueryService;
    private final MaintenanceCommandService maintenanceCommandService;
    private final TechnicalTicketJpaRepository technicalTicketJpaRepository;
    private final TechnicianQueryService technicianQueryService;
    private final com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService motionSensorCommandService;
    private final com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository motionSensorRepository;
    private final com.spottrack.platform.monitoring.application.commandServices.AnomalyCommandService anomalyCommandService;
    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;
    private final RoutineCommandService routineCommandService;
    private final RoutineSessionCommandService routineSessionCommandService;
    private final RoutineSessionQueryService routineSessionQueryService;

    public DevDataSeeder(
            RoleCommandService roleCommandService,
            UserCommandService userCommandService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            AdminQueryService adminQueryService,
            AdminCommandService adminCommandService,
            ClientQueryService clientQueryService,
            ClientCommandService clientCommandService,
            GymCommandService gymCommandService,
            EquipmentCommandService equipmentCommandService,
            GymPersistenceRepository gymPersistenceRepository,
            EquipmentPersistenceRepository equipmentPersistenceRepository,
            MembershipCommandService membershipCommandService,
            MembershipRepository membershipRepository,
            ActivityReportRepository activityReportRepository,
            ActivityReportCommandService activityReportCommandService,
            MaintenanceQuoteCommandService maintenanceQuoteCommandService,
            MaintenanceQuoteQueryService maintenanceQuoteQueryService,
            ROIProjectionCommandService roiProjectionCommandService,
            ROIProjectionQueryService roiProjectionQueryService,
            MaintenanceCommandService maintenanceCommandService,
            TechnicalTicketJpaRepository technicalTicketJpaRepository,
            TechnicianQueryService technicianQueryService,
            com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService motionSensorCommandService,
            com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository motionSensorRepository,
            com.spottrack.platform.monitoring.application.commandServices.AnomalyCommandService anomalyCommandService,
            ReservationCommandService reservationCommandService,
            ReservationQueryService reservationQueryService,
            RoutineCommandService routineCommandService,
            RoutineSessionCommandService routineSessionCommandService,
            RoutineSessionQueryService routineSessionQueryService) {
        this.roleCommandService = roleCommandService;
        this.userCommandService = userCommandService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.adminQueryService = adminQueryService;
        this.adminCommandService = adminCommandService;
        this.clientQueryService = clientQueryService;
        this.clientCommandService = clientCommandService;
        this.gymCommandService = gymCommandService;
        this.equipmentCommandService = equipmentCommandService;
        this.gymPersistenceRepository = gymPersistenceRepository;
        this.equipmentPersistenceRepository = equipmentPersistenceRepository;
        this.membershipCommandService = membershipCommandService;
        this.membershipRepository = membershipRepository;
        this.activityReportRepository = activityReportRepository;
        this.activityReportCommandService = activityReportCommandService;
        this.maintenanceQuoteCommandService = maintenanceQuoteCommandService;
        this.maintenanceQuoteQueryService = maintenanceQuoteQueryService;
        this.roiProjectionCommandService = roiProjectionCommandService;
        this.roiProjectionQueryService = roiProjectionQueryService;
        this.maintenanceCommandService = maintenanceCommandService;
        this.technicalTicketJpaRepository = technicalTicketJpaRepository;
        this.technicianQueryService = technicianQueryService;
        this.motionSensorCommandService = motionSensorCommandService;
        this.motionSensorRepository = motionSensorRepository;
        this.anomalyCommandService = anomalyCommandService;

        this.reservationCommandService = reservationCommandService;
        this.reservationQueryService = reservationQueryService;
        this.routineCommandService = routineCommandService;
        this.routineSessionCommandService = routineSessionCommandService;
        this.routineSessionQueryService = routineSessionQueryService;
    }

    private record GymSeedResult(String gymId, String equipmentId) {}

    @EventListener(ApplicationReadyEvent.class)
    @Order(10)
    public void seed(ApplicationReadyEvent event) {
        log.info("[DevDataSeeder] Starting dev seed...");

        roleCommandService.handle(new SeedRolesCommand());

        var adminUserId = seedAdminUser();
        seedMembership(adminUserId);
        var gymSeed = seedGym(adminUserId);
        seedWhitelist(gymSeed.gymId());
        var firstClientId = seedClientUser(gymSeed.gymId());
        var secondClientId = seedSecondClient(gymSeed.gymId());
        seedActivityReport(gymSeed.equipmentId());
        seedMaintenanceQuote(gymSeed.equipmentId());
        seedMotionSensor(gymSeed.equipmentId());
        seedRoiProjection();
        var technicianId = seedTechnician();
        seedMaintenanceLog(gymSeed.equipmentId(), technicianId);
        seedMaintenanceThreshold(gymSeed.equipmentId());
        seedMonthOfUsage(gymSeed.equipmentId());
        seedReservableEquipment(gymSeed.equipmentId(), firstClientId, secondClientId);
        seedRoutine(firstClientId);

        log.info("[DevDataSeeder] Dev seed complete.");
    }

    /** Adds a second and third piece of equipment with a month's worth of usage history, tickets in every
     *  kanban state, and a second, anomaly-triggered alert — so the app doesn't look like it was seeded five minutes ago. */
    private void seedMonthOfUsage(String primaryEquipmentId) {
        if (equipmentPersistenceRepository.findByEquipmentName("Bicicleta Seed").isPresent()) {
            log.info("[DevDataSeeder] Month-of-usage data already seeded, skipping.");
            return;
        }
        var zoneId = equipmentPersistenceRepository.findByEquipmentId(primaryEquipmentId)
                .map(e -> e.getZoneId())
                .orElse(null);
        if (zoneId == null) {
            log.warn("[DevDataSeeder] Could not resolve zone for primary equipment, skipping month-of-usage seed.");
            return;
        }

        var bikeId = seedNamedEquipment("Bicicleta Seed", zoneId);
        var benchId = seedNamedEquipment("Press Banca Seed", zoneId);

        seedActivityReport(bikeId, 220, 25, "Uso intensivo durante el mes", 12.0);
        seedActivityReport(benchId, 90, 8, "Uso ligero durante el mes", -3.0);

        var secondTechnicianId = seedSecondTechnician();

        seedInProgressTicket(bikeId, secondTechnicianId);
        seedOpenTicket(benchId);

        seedMaintenanceThreshold(bikeId);
        seedAnomalyAlert(benchId, zoneId);

        log.info("[DevDataSeeder] Month-of-usage data seeded.");
    }

    /** Two pieces of equipment that no maintenance ticket ever touches, kept AVAILABLE at all times,
     *  dedicated to the seeded reservations — so a client testing the app doesn't land on gear
     *  that's already tied up in the kanban/maintenance demo data. */
    private void seedReservableEquipment(String primaryEquipmentId, Long firstClientId, Long secondClientId) {
        var zoneId = equipmentPersistenceRepository.findByEquipmentId(primaryEquipmentId)
                .map(e -> e.getZoneId())
                .orElse(null);
        if (zoneId == null) {
            log.warn("[DevDataSeeder] Could not resolve zone for primary equipment, skipping reservation seeding.");
            return;
        }

        var reservableEquipmentId1 = seedNamedEquipment("Elíptica Seed", zoneId);
        var reservableEquipmentId2 = seedNamedEquipment("Multigimnasio Seed", zoneId);
        seedReservations(reservableEquipmentId1, reservableEquipmentId2, firstClientId, secondClientId);
    }

    private String seedNamedEquipment(String name, String zoneId) {
        var existing = equipmentPersistenceRepository.findByEquipmentName(name);
        if (existing.isPresent()) {
            return existing.get().getEquipmentId();
        }
        var result = equipmentCommandService.handle(new RegisterEquipment(
                name,
                EquipmentStatus.AVAILABLE,
                "Model-X",
                new ManufacturerId(MANUFACTURER_ID),
                new ZoneId(zoneId),
                new Money(BigDecimal.valueOf(500), "USD"),
                null
        ));
        if (result instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to create equipment {}: {}", name, f.error());
            throw new IllegalStateException("Dev seed failed at equipment creation: " + name);
        }
        var equipmentId = ((Result.Success<com.spottrack.platform.gym.domain.model.aggregates.Equipment, ?>) result).value().getId().uuid();
        log.info("[DevDataSeeder] Equipment {} created, equipmentId={}", name, equipmentId);
        return equipmentId;
    }

    private String seedSecondTechnician() {
        var existing = technicianQueryService.handle(new GetAllTechniciansQuery()).stream()
                .filter(t -> "Maria Seed".equals(t.getName()))
                .findFirst();
        if (existing.isPresent()) {
            return existing.get().getTechnicianId().uuid();
        }
        var result = maintenanceCommandService.handle(new CreateTechnician("Maria Seed"));
        if (result instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to create second technician: {}", f.error());
            throw new IllegalStateException("Dev seed failed at second technician creation");
        }
        var technicianId = ((Result.Success<com.spottrack.platform.maintenance.domain.model.aggregates.Technician, ?>) result).value().getTechnicianId().uuid();
        log.info("[DevDataSeeder] Second technician created, technicianId={}", technicianId);
        return technicianId;
    }

    /** A ticket that's assigned but not yet completed, so the "in progress" kanban column has real data. */
    private void seedInProgressTicket(String equipmentId, String technicianId) {
        var maintenanceResult = maintenanceCommandService.handle(new RequestMaintenance(
                new EquipmentId(equipmentId), "SYSTEM", "Ruido inusual durante el uso"));
        if (maintenanceResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to request maintenance for in-progress ticket: {}", f.error());
            return;
        }
        var maintenanceId = ((Result.Success<com.spottrack.platform.maintenance.domain.model.aggregates.Maintenance, ?>) maintenanceResult).value().getId().uuid();

        var ticketResult = maintenanceCommandService.handle(new CreateTechnicalTicketCommand(
                maintenanceId, TicketPriority.MEDIUM, TicketType.CORRECTIVE));
        if (ticketResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to create in-progress ticket: {}", f.error());
            return;
        }
        var ticket = ((Result.Success<com.spottrack.platform.maintenance.domain.model.aggregates.TechnicalTicket, ?>) ticketResult).value();

        var assignResult = maintenanceCommandService.handle(new AssignTechnicalTicket(ticket.getTicketId(), technicianId));
        if (assignResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to assign in-progress ticket: {}", f.error());
            return;
        }
        log.info("[DevDataSeeder] In-progress ticket seeded for equipment {}.", equipmentId);
    }

    /** A ticket that's just been opened, unassigned, so the "pending" kanban column has real data. */
    private void seedOpenTicket(String equipmentId) {
        var maintenanceResult = maintenanceCommandService.handle(new RequestMaintenance(
                new EquipmentId(equipmentId), "SYSTEM", "Revisión programada mensual"));
        if (maintenanceResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to request maintenance for open ticket: {}", f.error());
            return;
        }
        var maintenanceId = ((Result.Success<com.spottrack.platform.maintenance.domain.model.aggregates.Maintenance, ?>) maintenanceResult).value().getId().uuid();

        var ticketResult = maintenanceCommandService.handle(new CreateTechnicalTicketCommand(
                maintenanceId, TicketPriority.LOW, TicketType.PREVENTIVE));
        if (ticketResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to create open ticket: {}", f.error());
            return;
        }
        log.info("[DevDataSeeder] Open ticket seeded for equipment {}.", equipmentId);
    }

    /** Triggers a real CRITICAL alert via the anomaly-report event chain, distinct from the WARNING alerts raised by maintenance thresholds. */
    private void seedAnomalyAlert(String equipmentId, String zoneId) {
        var result = anomalyCommandService.handle(new com.spottrack.platform.monitoring.domain.model.commands.ReportAnomalyCommand(
                "SEED-RESERVATION-" + equipmentId,
                equipmentId,
                zoneId,
                "Vibración inusual detectada durante el uso"
        ));
        if (result instanceof Result.Failure<?, ?> f) {
            log.warn("[DevDataSeeder] Failed to seed anomaly alert: {}", f.error());
            return;
        }
        log.info("[DevDataSeeder] Anomaly reported for equipment {}; a CRITICAL alert should follow shortly.", equipmentId);
    }

    /** Sets a past-due threshold so the scheduler raises a real alert on its next tick, instead of leaving the seeded admin's alert inbox empty. */
    private void seedMaintenanceThreshold(String equipmentId) {
        if (equipmentId == null) {
            log.warn("[DevDataSeeder] Equipment ID not available, skipping maintenance threshold seeding.");
            return;
        }
        // Always (re)force a clearly past-due date, even if ticket completion already set one to
        // today's date as a side effect — "today" is too fragile against the hourly scheduler tick.
        var equipment = equipmentPersistenceRepository.findByEquipmentId(equipmentId).orElse(null);
        if (equipment != null && equipment.getMaintenanceThreshold() != null
                && equipment.getMaintenanceThreshold().isBefore(LocalDate.now())) {
            log.info("[DevDataSeeder] Maintenance threshold already past-due for equipment {}, skipping.", equipmentId);
            return;
        }
        var result = equipmentCommandService.handle(new com.spottrack.platform.gym.domain.model.commands.DefineMaintenanceThresholdCommand(
                new com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId(equipmentId),
                LocalDate.now().minusDays(1)
        ));
        if (result instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to define maintenance threshold: {}", f.error());
            return;
        }
        log.info("[DevDataSeeder] Maintenance threshold set in the past for equipment {}; scheduler will raise an alert shortly.", equipmentId);
    }

    private String seedTechnician() {
        var existing = technicianQueryService.handle(new GetAllTechniciansQuery());
        if (!existing.isEmpty()) {
            log.info("[DevDataSeeder] Technician already exists, skipping creation.");
            return existing.get(0).getTechnicianId().uuid();
        }
        var result = maintenanceCommandService.handle(new CreateTechnician("Carlos Seed"));
        if (result instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to create technician: {}", f.error());
            throw new IllegalStateException("Dev seed failed at technician creation");
        }
        var technicianId = ((Result.Success<com.spottrack.platform.maintenance.domain.model.aggregates.Technician, ?>) result).value().getTechnicianId().uuid();
        log.info("[DevDataSeeder] Technician created, technicianId={}", technicianId);
        return technicianId;
    }

    private Long seedAdminUser() {
        if (userRepository.existsByUsername(ADMIN_EMAIL)) {
            log.info("[DevDataSeeder] Admin user already exists, skipping creation.");
            var user = userRepository.findByUsername(ADMIN_EMAIL).orElseThrow();
            return user.getId();
        }

        var adminRole = roleRepository.findByName(Roles.ROLE_ADMIN)
                .orElseGet(() -> new Role(Roles.ROLE_ADMIN));
        var result = userCommandService.handle(new SignUpCommand(ADMIN_EMAIL, SEED_PASSWORD, List.of(adminRole)));
        if (result instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to create admin user: {}", f.error());
            throw new IllegalStateException("Dev seed failed at admin user creation");
        }
        var adminUserId = ((Result.Success<com.spottrack.platform.iam.domain.model.aggregates.User, ?>) result).value().getId();
        log.info("[DevDataSeeder] Admin user created, userId={}", adminUserId);

        var admin = adminQueryService.handle(new GetAdminByUserIdQuery(adminUserId))
                .orElseThrow(() -> new IllegalStateException("Admin profile not found after sign-up"));
        if (!admin.isProfileComplete()) {
            adminCommandService.handle(new UpdateAdminProfileCommand(
                    new AdminId(admin.getId()),
                    "Seed",
                    "Admin",
                    new PhoneNumber("999111111"),
                    new com.spottrack.platform.profiles.domain.model.valueobjects.Dni(ADMIN_DNI)
            ));
            log.info("[DevDataSeeder] Admin profile updated.");
        }
        return adminUserId;
    }

    private GymSeedResult seedGym(Long adminUserId) {
        var existing = gymPersistenceRepository.findByAdminUserId(adminUserId);
        if (!existing.isEmpty()) {
            var gymId = existing.get(0).getGymId();
            var equipmentId = equipmentPersistenceRepository.findByEquipmentName("Cinta Seed")
                    .map(e -> e.getEquipmentId()).orElse(null);
            if (equipmentId != null) {
                log.info("[DevDataSeeder] Gym already exists gymId={}, skipping creation.", gymId);
                return new GymSeedResult(gymId, equipmentId);
            }
            log.warn("[DevDataSeeder] Gym exists gymId={} but seed equipment is missing, recreating it.", gymId);
            return new GymSeedResult(gymId, seedEquipment(gymId));
        }

        var gymResult = gymCommandService.handle(new CreateGym(SEED_GYM_NAME, adminUserId));
        if (gymResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to create gym: {}", f.error());
            throw new IllegalStateException("Dev seed failed at gym creation");
        }
        var gym = ((Result.Success<com.spottrack.platform.gym.domain.model.aggregates.Gym, ?>) gymResult).value();
        var gymId = gym.getId().uuid();
        log.info("[DevDataSeeder] Gym created gymId={}", gymId);

        return new GymSeedResult(gymId, seedEquipment(gymId));
    }

    private String seedEquipment(String gymId) {
        var branchResult = gymCommandService.handle(new AddBranchCommand(gymId, "Sede Central", "Av. Seed 123"));
        if (branchResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to create branch: {}", f.error());
            throw new IllegalStateException("Dev seed failed at branch creation");
        }
        var branch = ((Result.Success<com.spottrack.platform.gym.domain.model.entities.Branch, ?>) branchResult).value();
        var branchId = branch.getId().uuid();
        log.info("[DevDataSeeder] Branch created branchId={}", branchId);

        var zoneResult = gymCommandService.handle(new AddZoneCommand("Zona A", 20, new BranchId(branchId)));
        if (zoneResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to create zone: {}", f.error());
            throw new IllegalStateException("Dev seed failed at zone creation");
        }
        var zone = ((Result.Success<com.spottrack.platform.gym.domain.model.entities.Zone, ?>) zoneResult).value();
        var zoneId = zone.getId().uuid();
        log.info("[DevDataSeeder] Zone created zoneId={}", zoneId);

        var equipResult = equipmentCommandService.handle(new RegisterEquipment(
                "Cinta Seed",
                EquipmentStatus.AVAILABLE,
                "Model-X",
                new ManufacturerId(MANUFACTURER_ID),
                new ZoneId(zoneId),
                new Money(BigDecimal.valueOf(500), "USD"),
                null
        ));
        if (equipResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to create equipment: {}", f.error());
            throw new IllegalStateException("Dev seed failed at equipment creation");
        }
        var equipmentId = ((Result.Success<com.spottrack.platform.gym.domain.model.aggregates.Equipment, ?>) equipResult).value().getId().uuid();
        log.info("[DevDataSeeder] Equipment created equipmentId={}", equipmentId);
        return equipmentId;
    }

    private void seedMembership(Long adminUserId) {
        var memberships = membershipRepository.findByClientId(adminUserId);
        boolean hasActive = memberships.stream()
                .anyMatch(m -> m.getStatus() == MembershipStatus.ACTIVE);
        if (hasActive) {
            log.info("[DevDataSeeder] Active membership already exists, skipping.");
            return;
        }

        var today = LocalDate.now();
        var createResult = membershipCommandService.handle(new CreateMembershipCommand(
                adminUserId,
                MembershipTier.PLATINUM,
                MembershipTier.PLATINUM.toMoney(),
                today,
                today.plusDays(30)
        ));
        if (createResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to create membership: {}", f.error());
            throw new IllegalStateException("Dev seed failed at membership creation");
        }
        var membership = ((Result.Success<com.spottrack.platform.membership.domain.model.aggregates.Membership, ?>) createResult).value();

        var activateResult = membershipCommandService.handle(new ActivateMembershipCommand(membership.getMembershipId()));
        if (activateResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to activate membership: {}", f.error());
            throw new IllegalStateException("Dev seed failed at membership activation");
        }
        log.info("[DevDataSeeder] Membership created and activated.");
    }

    private void seedWhitelist(String gymId) {
        var dniResult = gymCommandService.handle(new AddDniToWhitelistCommand(
                gymId,
                new com.spottrack.platform.gym.domain.model.valueobjects.Dni(SEED_DNI)
        ));
        if (dniResult instanceof Result.Failure<?, ?> f) {
            log.info("[DevDataSeeder] DNI {} already in whitelist ({}), skipping.", SEED_DNI, f.error());
        } else {
            log.info("[DevDataSeeder] DNI {} added to whitelist.", SEED_DNI);
        }
    }

    private Long seedClientUser(String gymId) {
        return seedClient(gymId, CLIENT_EMAIL, SEED_DNI, "Seed", "Client", "999222222");
    }

    /** A second, distinct client account for debugging the client-facing app, separate from the primary seed client. */
    private Long seedSecondClient(String gymId) {
        var dniResult = gymCommandService.handle(new AddDniToWhitelistCommand(
                gymId,
                new com.spottrack.platform.gym.domain.model.valueobjects.Dni(DEBUG_CLIENT_DNI)
        ));
        if (dniResult instanceof Result.Failure<?, ?> f) {
            log.info("[DevDataSeeder] DNI {} already in whitelist ({}), skipping.", DEBUG_CLIENT_DNI, f.error());
        } else {
            log.info("[DevDataSeeder] DNI {} added to whitelist.", DEBUG_CLIENT_DNI);
        }
        return seedClient(gymId, DEBUG_CLIENT_EMAIL, DEBUG_CLIENT_DNI, "Debug", "Client", "999333333");
    }

    private Long seedClient(String gymId, String email, String dni, String firstName, String lastName, String phone) {
        Long clientUserId;
        if (userRepository.existsByUsername(email)) {
            log.info("[DevDataSeeder] Client user {} already exists, skipping creation.", email);
            clientUserId = userRepository.findByUsername(email).orElseThrow().getId();
        } else {
            var clientRole = roleRepository.findByName(Roles.ROLE_CLIENT)
                    .orElseGet(() -> new Role(Roles.ROLE_CLIENT));
            var result = userCommandService.handle(new SignUpCommand(email, SEED_PASSWORD, List.of(clientRole)));
            if (result instanceof Result.Failure<?, ?> f) {
                log.error("[DevDataSeeder] Failed to create client user {}: {}", email, f.error());
                throw new IllegalStateException("Dev seed failed at client user creation: " + email);
            }
            clientUserId = ((Result.Success<com.spottrack.platform.iam.domain.model.aggregates.User, ?>) result).value().getId();
            log.info("[DevDataSeeder] Client user {} created, userId={}", email, clientUserId);
        }

        var client = clientQueryService.handle(new GetClientByUserIdQuery(clientUserId))
                .orElseThrow(() -> new IllegalStateException("Client profile not found after sign-up"));

        if (!client.isProfileComplete()) {
            clientCommandService.handle(new UpdateClientProfileCommand(
                    new ClientId(client.getId()),
                    firstName,
                    lastName,
                    new PhoneNumber(phone),
                    new com.spottrack.platform.profiles.domain.model.valueobjects.Dni(dni)
            ));
            log.info("[DevDataSeeder] Client profile updated for {}.", email);
        }

        var assocResult = clientCommandService.handle(
                new AssociateClientWithGymCommand(client.getId(), gymId));
        if (assocResult instanceof Result.Failure<?, ?> f) {
            log.info("[DevDataSeeder] Client-gym association already exists ({}), skipping.", f.error());
        } else {
            log.info("[DevDataSeeder] Client associated with gym gymId={}.", gymId);
        }
        return client.getId();
    }

    /**
     * One express reservation per client, so a reservation dropdown has real data to show.
     * A client can only hold one ACTIVE reservation at a time, so each seed client gets a
     * different piece of equipment rather than competing for the same one.
     */
    private void seedReservations(String equipmentId1, String equipmentId2, Long firstClientId, Long secondClientId) {
        if (!reservationQueryService.handle(new GetReservationsByClientIdQuery(firstClientId)).isEmpty()) {
            log.info("[DevDataSeeder] Reservations already seeded, skipping.");
            return;
        }
        seedReservation(firstClientId, equipmentId1, "08:00:00", "09:00:00");
        seedReservation(secondClientId, equipmentId2, "10:00:00", "11:00:00");
        log.info("[DevDataSeeder] Reservations seeded.");
    }

    private void seedReservation(Long clientId, String equipmentId, String start, String end) {
        var result = reservationCommandService.handle(new InitiateExpressReservation(
                new com.spottrack.platform.reservation.domain.model.valueobjects.ClientId(clientId),
                new com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId(equipmentId),
                new TimeInterval(Time.valueOf(start), Time.valueOf(end))
        ));
        if (result instanceof Result.Failure<?, ?> f) {
            log.warn("[DevDataSeeder] Failed to seed reservation for client {} on equipment {}: {}", clientId, equipmentId, f.error());
        }
    }

    private void seedActivityReport(String equipmentId) {
        seedActivityReport(equipmentId, 45, 10, "Cinta atascada", 8.0);
    }

    private void seedActivityReport(String equipmentId, int minutesActive, int minutesInactive, String downtimeReason, double percentageChange) {
        if (equipmentId == null) {
            log.warn("[DevDataSeeder] Equipment ID not available, skipping activity report seeding.");
            return;
        }
        if (activityReportRepository.findByEquipmentId(equipmentId).isPresent()) {
            log.info("[DevDataSeeder] Activity report already exists for equipment {}, skipping.", equipmentId);
            return;
        }
        activityReportCommandService.handle(new RequestActivityAnalysisCommand(
                equipmentId, minutesActive, minutesInactive, downtimeReason, percentageChange));
        log.info("[DevDataSeeder] Activity report seeded for equipment {}.", equipmentId);
    }

    private void seedMaintenanceQuote(String equipmentId) {
        if (equipmentId == null) {
            log.warn("[DevDataSeeder] Equipment ID not available, skipping maintenance quote seeding.");
            return;
        }
        if (!maintenanceQuoteQueryService.handle(new GetAllMaintenanceQuotesQuery()).isEmpty()) {
            log.info("[DevDataSeeder] Maintenance quote already seeded, skipping.");
            return;
        }
        var quoteResult = maintenanceQuoteCommandService.handle(new RequestADetailedMaintenanceQuoteCommand(
                equipmentId, 120.0, "USD", "CORRECTIVE", "n/a", 1, 0.0));
        if (quoteResult.isEmpty()) {
            log.warn("[DevDataSeeder] Failed to seed maintenance quote.");
            return;
        }
        var quoteId = quoteResult.get().getId();
        maintenanceQuoteCommandService.handle(quoteId, new RequestSparePartsCommand("Belt", 3, 25.0));
        maintenanceQuoteCommandService.handle(quoteId, new RequestPreventiveCostCommand(40.0, "USD"));
        log.info("[DevDataSeeder] Maintenance quote seeded, id={}.", quoteId);
    }

    private void seedMotionSensor(String equipmentId) {
        if (equipmentId == null) {
            log.warn("[DevDataSeeder] Equipment ID not available, skipping motion sensor seeding.");
            return;
        }
        if (motionSensorRepository.existsByEquipmentId(new com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId(equipmentId))) {
            log.info("[DevDataSeeder] Motion sensor already exists for equipment {}, skipping.", equipmentId);
            return;
        }
        var result = motionSensorCommandService.handle(
                new com.spottrack.platform.monitoring.domain.model.commands.RegisterMotionSensorCommand(equipmentId));
        if (result instanceof Result.Failure<?, ?> f) {
            log.warn("[DevDataSeeder] Failed to seed motion sensor: {}", f.error());
            return;
        }
        log.info("[DevDataSeeder] Motion sensor seeded for equipment {}.", equipmentId);
    }

    private void seedRoiProjection() {
        if (!roiProjectionQueryService.handle(new GetAllROIProjectionsQuery()).isEmpty()) {
            log.info("[DevDataSeeder] ROI projection already seeded, skipping.");
            return;
        }
        var roiResult = roiProjectionCommandService.handle(new RequestRoiCommand(12.5));
        if (roiResult.isEmpty()) {
            log.warn("[DevDataSeeder] Failed to seed ROI projection.");
            return;
        }
        var roiId = roiResult.get().getId();
        roiProjectionCommandService.handle(roiId, new RequestDowntimeCostCommand(20, "sensor jam"));
        roiProjectionCommandService.handle(roiId, new RequestEarningsCommand(600.0));
        log.info("[DevDataSeeder] ROI projection seeded, id={}.", roiId);
    }

    private void seedMaintenanceLog(String equipmentId, String technicianId) {
        if (equipmentId == null) {
            log.warn("[DevDataSeeder] Equipment ID not available, skipping maintenance log seeding.");
            return;
        }
        boolean alreadyExists = technicalTicketJpaRepository.findAll().stream()
                .anyMatch(t -> equipmentId.equals(t.getEquipmentId()));
        if (alreadyExists) {
            log.info("[DevDataSeeder] Maintenance ticket already exists for equipment {}, skipping.", equipmentId);
            return;
        }
        var maintenanceResult = maintenanceCommandService.handle(new RequestMaintenance(
                new EquipmentId(equipmentId), "SYSTEM", "Rutina de mantenimiento preventivo"));
        if (maintenanceResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to request maintenance: {}", f.error());
            return;
        }
        var maintenanceId = ((Result.Success<com.spottrack.platform.maintenance.domain.model.aggregates.Maintenance, ?>) maintenanceResult).value().getId().uuid();

        var ticketResult = maintenanceCommandService.handle(new CreateTechnicalTicketCommand(
                maintenanceId,
                TicketPriority.LOW,
                TicketType.PREVENTIVE
        ));
        if (ticketResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to create maintenance ticket: {}", f.error());
            return;
        }
        var ticket = ((Result.Success<com.spottrack.platform.maintenance.domain.model.aggregates.TechnicalTicket, ?>) ticketResult).value();

        var assignResult = maintenanceCommandService.handle(new AssignTechnicalTicket(ticket.getTicketId(), technicianId));
        if (assignResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to assign technician to ticket: {}", f.error());
            return;
        }
        log.info("[DevDataSeeder] Technician {} assigned to ticket {}.", technicianId, ticket.getTicketId().uuid());

        var completionResult = maintenanceCommandService.handle(new RegisterMaintenanceCompletion(
                new TechnicalTicketId(ticket.getTicketId().uuid()),
                new MaintenanceId(ticket.getMaintenanceId()),
                "Lubricación y ajuste de banda completados",
                BigDecimal.valueOf(35.00)
        ));
        if (completionResult instanceof Result.Failure<?, ?> f) {
            log.error("[DevDataSeeder] Failed to register maintenance completion: {}", f.error());
            return;
        }
        log.info("[DevDataSeeder] Maintenance ticket and completion log seeded for equipment {}.", equipmentId);
    }

    /** A routine with exercise blocks and one completed session, so a client testing the app has
     *  real routine history to look at instead of an empty routines tab. */
    private void seedRoutine(Long clientId) {
        var routineClientId = new com.spottrack.platform.routine.domain.model.valueobjects.ClientId(clientId);
        if (!routineSessionQueryService.handle(new GetAllRoutineSessionsByClientIdQuery(routineClientId)).isEmpty()) {
            log.info("[DevDataSeeder] Routine session already seeded, skipping.");
            return;
        }

        var routineResult = routineCommandService.handle(new CreateRoutineCommand(
                new RoutineName("Rutina Seed"), routineClientId));
        if (routineResult instanceof Result.Failure<?, ?> f) {
            log.warn("[DevDataSeeder] Failed to seed routine: {}", f.error());
            return;
        }
        var routine = ((Result.Success<com.spottrack.platform.routine.domain.model.aggregates.Routine, ?>) routineResult).value();
        var routineId = routine.getId();

        routineCommandService.handle(new AddExerciseBlockCommand(routineId, new ExerciseName("Carrera en cinta"), ExerciseType.CARDIO, 1, 1, 10));
        routineCommandService.handle(new AddExerciseBlockCommand(routineId, new ExerciseName("Sentadillas"), ExerciseType.STRENGTH, 2, 3, 12));

        var sessionResult = routineSessionCommandService.handle(new StartRoutineCommand(routineId, routineClientId));
        if (sessionResult instanceof Result.Failure<?, ?> f) {
            log.warn("[DevDataSeeder] Failed to start routine session: {}", f.error());
            return;
        }
        var session = ((Result.Success<com.spottrack.platform.routine.domain.model.aggregates.RoutineSession, ?>) sessionResult).value();
        routineSessionCommandService.handle(new CompleteRoutineCommand(session.getId()));
        log.info("[DevDataSeeder] Routine and completed session seeded for client {}.", clientId);
    }
}
