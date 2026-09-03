package com.spottrack.platform.shared.infrastructure.seeder;

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
import com.spottrack.platform.maintenance.domain.model.commands.CreateTechnicalTicketCommand;
import com.spottrack.platform.maintenance.domain.model.commands.RequestMaintenance;
import com.spottrack.platform.maintenance.domain.model.valueobjects.EquipmentId;
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
import com.spottrack.platform.profiles.application.commandservices.BusinessProfileCommandService;
import com.spottrack.platform.profiles.domain.model.commands.CreateBusinessProfileCommand;
import com.spottrack.platform.profiles.domain.model.valueobjects.AdminId;
import com.spottrack.platform.profiles.domain.model.valueobjects.BusinessInfo;
import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.domain.model.valueobjects.PhoneNumber;
import com.spottrack.platform.profiles.domain.repositories.BusinessProfileRepository;
import com.spottrack.platform.reservation.application.commandServices.ReservationCommandService;
import com.spottrack.platform.reservation.domain.model.commands.EndReservation;
import com.spottrack.platform.reservation.domain.model.commands.InitiateExpressReservation;
import com.spottrack.platform.reservation.domain.model.commands.StartReservationTimer;
import com.spottrack.platform.reservation.domain.model.valueobjects.TimeInterval;
import com.spottrack.platform.reservation.domain.repositories.ReservationRepository;
import com.spottrack.platform.routine.application.commandservices.RoutineCommandService;
import com.spottrack.platform.routine.application.commandservices.RoutineSessionCommandService;
import com.spottrack.platform.routine.domain.model.commands.AddExerciseBlockCommand;
import com.spottrack.platform.routine.domain.model.commands.CompleteRoutineCommand;
import com.spottrack.platform.routine.domain.model.commands.CreateRoutineCommand;
import com.spottrack.platform.routine.domain.model.commands.StartRoutineCommand;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseName;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseType;
import com.spottrack.platform.routine.domain.model.valueobjects.RoutineName;
import com.spottrack.platform.routine.domain.repositories.RoutineRepository;
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
@Profile("prod")
@Slf4j
public class DemoDataSeeder {

    private static final String ADMIN_EMAIL    = "admin@spottrack.com";
    private static final String CLIENT_EMAIL   = "cliente@email.com";
    private static final String CLIENT2_EMAIL  = "cliente2@demo.com";
    private static final String CLIENT3_EMAIL  = "cliente3@demo.com";
    private static final String DEMO_PASSWORD  = "demo1234";

    private static final String ADMIN_DNI   = "00000001";
    private static final String CLIENT_DNI  = "00000002";
    private static final String CLIENT2_DNI = "00000003";
    private static final String CLIENT3_DNI = "00000004";

    private static final String GYM_NAME        = "SpotTrack Demo";
    private static final String MANUFACTURER_ID = "00000000-0000-0000-0000-000000000001";

    private static final String ADMIN2_EMAIL = "admin2@fitzone.pe";
    private static final String ADMIN2_DNI   = "00000005";
    private static final String GYM2_NAME    = "FitZone Lima";

    // Stable names used to recover IDs on idempotent restarts
    private static final String EQUIP_A_NAME   = "Cinta de Correr Pro";   // used for ACTIVE reservation
    private static final String EQUIP_B_NAME   = "Elíptica X200";          // used for ENDED reservation
    private static final String EQUIP_OOS_NAME = "Bicicleta Estática";    // used for maintenance ticket

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
    private final ReservationCommandService reservationCommandService;
    private final ReservationRepository reservationRepository;
    private final RoutineCommandService routineCommandService;
    private final RoutineRepository routineRepository;
    private final RoutineSessionCommandService routineSessionCommandService;
    private final MaintenanceCommandService maintenanceCommandService;
    private final TechnicalTicketJpaRepository technicalTicketJpaRepository;
    private final BusinessProfileCommandService businessProfileCommandService;
    private final BusinessProfileRepository businessProfileRepository;
    private final com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService motionSensorCommandService;
    private final com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository motionSensorRepository;

    public DemoDataSeeder(
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
            ReservationCommandService reservationCommandService,
            ReservationRepository reservationRepository,
            RoutineCommandService routineCommandService,
            RoutineRepository routineRepository,
            RoutineSessionCommandService routineSessionCommandService,
            MaintenanceCommandService maintenanceCommandService,
            TechnicalTicketJpaRepository technicalTicketJpaRepository,
            BusinessProfileCommandService businessProfileCommandService,
            BusinessProfileRepository businessProfileRepository,
            com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService motionSensorCommandService,
            com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository motionSensorRepository) {
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
        this.reservationCommandService = reservationCommandService;
        this.reservationRepository = reservationRepository;
        this.routineCommandService = routineCommandService;
        this.routineRepository = routineRepository;
        this.routineSessionCommandService = routineSessionCommandService;
        this.maintenanceCommandService = maintenanceCommandService;
        this.technicalTicketJpaRepository = technicalTicketJpaRepository;
        this.businessProfileCommandService = businessProfileCommandService;
        this.businessProfileRepository = businessProfileRepository;
        this.motionSensorCommandService = motionSensorCommandService;
        this.motionSensorRepository = motionSensorRepository;
    }

    private record GymSeedResult(String gymId, String equipA, String equipB, String equipOOS) {}

    @EventListener(ApplicationReadyEvent.class)
    @Order(10)
    public void seed(ApplicationReadyEvent event) {
        log.info("[DemoDataSeeder] Starting demo seed...");

        roleCommandService.handle(new SeedRolesCommand());

        var adminUserId   = seedAdminUser();
        seedBusinessProfile(adminUserId,
                "SpotTrack Fitness S.A.C.", "20123456781",
                "Av. Larco 1234", "Lima", "Miraflores",
                "999000001", ADMIN_EMAIL);
        seedMembership(adminUserId);
        var gymSeed       = seedGym(adminUserId);
        seedWhitelist(gymSeed.gymId());
        var clientProfileId = seedClientUser(gymSeed.gymId());
        seedAdditionalClients(gymSeed.gymId());
        seedReservations(clientProfileId, gymSeed);
        seedRoutines(clientProfileId);
        seedMaintenanceTicket(gymSeed.equipOOS());
        seedMotionSensor(gymSeed.equipA());

        var admin2UserId = seedSecondAdmin();
        seedMembership(admin2UserId);
        var gym2Id = seedSecondGym(admin2UserId);
        seedWhitelistForClient(gym2Id);
        associateClientWithSecondGym(clientProfileId, gym2Id);

        log.info("[DemoDataSeeder] Demo seed complete.");
    }

    // ─── Business profile ────────────────────────────────────────────────────

    private void seedBusinessProfile(Long userId, String companyName, String ruc,
                                     String streetAddress, String city, String district,
                                     String phone, String email) {
        if (businessProfileRepository.existsByUserId(userId)) {
            log.info("[DemoDataSeeder] BusinessProfile already exists for userId={}, skipping.", userId);
            return;
        }
        var businessInfo = new BusinessInfo(
                companyName, ruc, "S.A.C.",
                new PhoneNumber(phone),
                new EmailAddress(email),
                streetAddress, city, district
        );
        var result = businessProfileCommandService.handle(new CreateBusinessProfileCommand(userId, businessInfo));
        if (result instanceof Result.Failure<?, ?> f)
            log.error("[DemoDataSeeder] Failed to create BusinessProfile for userId={}: {}", userId, f.error());
        else
            log.info("[DemoDataSeeder] BusinessProfile created for userId={}.", userId);
    }

    // ─── Admin ───────────────────────────────────────────────────────────────

    private Long seedAdminUser() {
        if (userRepository.existsByUsername(ADMIN_EMAIL)) {
            log.info("[DemoDataSeeder] Admin user already exists, skipping.");
            return userRepository.findByUsername(ADMIN_EMAIL).orElseThrow().getId();
        }

        var adminRole = roleRepository.findByName(Roles.ROLE_ADMIN)
                .orElseGet(() -> new Role(Roles.ROLE_ADMIN));
        var result = userCommandService.handle(new SignUpCommand(ADMIN_EMAIL, DEMO_PASSWORD, List.of(adminRole)));
        if (result instanceof Result.Failure<?, ?> f) {
            log.error("[DemoDataSeeder] Failed to create admin user: {}", f.error());
            throw new IllegalStateException("Demo seed failed at admin user creation");
        }
        var adminUserId = ((Result.Success<com.spottrack.platform.iam.domain.model.aggregates.User, ?>) result).value().getId();

        var admin = adminQueryService.handle(new GetAdminByUserIdQuery(adminUserId))
                .orElseThrow(() -> new IllegalStateException("Admin profile not found after sign-up"));
        if (!admin.isProfileComplete()) {
            adminCommandService.handle(new UpdateAdminProfileCommand(
                    new AdminId(admin.getId()),
                    "Admin",
                    "SpotTrack",
                    new PhoneNumber("999000001"),
                    new com.spottrack.platform.profiles.domain.model.valueobjects.Dni(ADMIN_DNI)
            ));
        }
        log.info("[DemoDataSeeder] Admin user created, userId={}", adminUserId);
        return adminUserId;
    }

    // ─── Gym (2 branches × 2 zones × 4-6 equipment each) ────────────────────

    private GymSeedResult seedGym(Long adminUserId) {
        var existing = gymPersistenceRepository.findByAdminUserId(adminUserId);
        if (!existing.isEmpty()) {
            var gymId  = existing.get(0).getGymId();
            var equipA = equipmentPersistenceRepository.findByEquipmentName(EQUIP_A_NAME)
                    .map(e -> e.getEquipmentId()).orElse(null);
            var equipB = equipmentPersistenceRepository.findByEquipmentName(EQUIP_B_NAME)
                    .map(e -> e.getEquipmentId()).orElse(null);
            var equipOOS = equipmentPersistenceRepository.findByEquipmentName(EQUIP_OOS_NAME)
                    .map(e -> e.getEquipmentId()).orElse(null);
            log.info("[DemoDataSeeder] Gym already exists gymId={}, recovered equipment IDs.", gymId);
            return new GymSeedResult(gymId, equipA, equipB, equipOOS);
        }

        var gymResult = gymCommandService.handle(new CreateGym(GYM_NAME, adminUserId));
        if (gymResult instanceof Result.Failure<?, ?> f)
            throw new IllegalStateException("Demo seed failed at gym creation: " + f.error());
        var gymId = ((Result.Success<com.spottrack.platform.gym.domain.model.aggregates.Gym, ?>) gymResult).value().getId().uuid();
        log.info("[DemoDataSeeder] Gym created gymId={}", gymId);

        // Branch 1 – Miraflores
        var branch1 = addBranch(gymId, "Sede Miraflores", "Av. Larco 1234, Miraflores");
        var cardioId = addZone("Cardio", 15, branch1);
        var equipA   = addEquipment(EQUIP_A_NAME,           EquipmentStatus.AVAILABLE,      "Treadmill-Pro-2024",   cardioId, 1200.00);
        var equipB   = addEquipment(EQUIP_B_NAME,           EquipmentStatus.AVAILABLE,      "Elliptical-X200",      cardioId,  950.00);
        var equipOOS = addEquipment(EQUIP_OOS_NAME,         EquipmentStatus.OUT_OF_SERVICE, "Bike-Static-300",      cardioId,  600.00);
        var pesasId  = addZone("Pesas", 20, branch1);
        addEquipment("Mancuernas Ajustables 30kg", EquipmentStatus.AVAILABLE,      "Dumbbell-Adj-30",     pesasId, 250.00);
        addEquipment("Barra Olímpica 20kg",        EquipmentStatus.AVAILABLE,      "Barbell-Olympic-20",  pesasId, 180.00);
        addEquipment("Rack Multiuso",              EquipmentStatus.AVAILABLE,      "Rack-Multi-500",      pesasId, 800.00);

        // Branch 2 – San Isidro
        var branch2     = addBranch(gymId, "Sede San Isidro", "Av. República de Panamá 3500, San Isidro");
        var funcionalId = addZone("Funcional", 12, branch2);
        addEquipment("Cuerda para Saltar", EquipmentStatus.AVAILABLE, "Jump-Rope-Pro",     funcionalId,  50.00);
        addEquipment("Kettlebell 16kg",    EquipmentStatus.AVAILABLE, "Kettlebell-16kg",   funcionalId, 120.00);
        var maquinasId = addZone("Máquinas", 18, branch2);
        addEquipment("Prensa de Piernas", EquipmentStatus.AVAILABLE,      "Leg-Press-X3",      maquinasId, 1500.00);
        addEquipment("Polea Cable",       EquipmentStatus.AVAILABLE,      "Cable-Pulley-200",  maquinasId,  700.00);

        log.info("[DemoDataSeeder] Gym structure seeded: 2 branches, 4 zones, 10 equipment.");
        return new GymSeedResult(gymId, equipA, equipB, equipOOS);
    }

    private String addBranch(String gymId, String name, String address) {
        var result = gymCommandService.handle(new AddBranchCommand(gymId, name, address));
        if (result instanceof Result.Failure<?, ?> f)
            throw new IllegalStateException("Demo seed failed at branch '" + name + "': " + f.error());
        return ((Result.Success<com.spottrack.platform.gym.domain.model.entities.Branch, ?>) result).value().getId().uuid();
    }

    private String addZone(String name, int capacity, String branchId) {
        var result = gymCommandService.handle(new AddZoneCommand(name, capacity, new BranchId(branchId)));
        if (result instanceof Result.Failure<?, ?> f)
            throw new IllegalStateException("Demo seed failed at zone '" + name + "': " + f.error());
        return ((Result.Success<com.spottrack.platform.gym.domain.model.entities.Zone, ?>) result).value().getId().uuid();
    }

    private String addEquipment(String name, EquipmentStatus status, String model, String zoneId, double price) {
        var result = equipmentCommandService.handle(new RegisterEquipment(
                name, status, model,
                new ManufacturerId(MANUFACTURER_ID),
                new ZoneId(zoneId),
                new Money(BigDecimal.valueOf(price), "USD"),
                null
        ));
        if (result instanceof Result.Failure<?, ?> f)
            throw new IllegalStateException("Demo seed failed at equipment '" + name + "': " + f.error());
        var equipId = ((Result.Success<com.spottrack.platform.gym.domain.model.aggregates.Equipment, ?>) result).value().getId().uuid();
        log.info("[DemoDataSeeder] Equipment '{}' created id={} status={}", name, equipId, status);
        return equipId;
    }

    // ─── Membership ──────────────────────────────────────────────────────────

    private void seedMembership(Long adminUserId) {
        var memberships = membershipRepository.findByClientId(adminUserId);
        if (memberships.stream().anyMatch(m -> m.getStatus() == MembershipStatus.ACTIVE)) {
            log.info("[DemoDataSeeder] Active membership already exists, skipping.");
            return;
        }
        var today = LocalDate.now();
        var createResult = membershipCommandService.handle(new CreateMembershipCommand(
                adminUserId, MembershipTier.PLATINUM, MembershipTier.PLATINUM.toMoney(),
                today, today.plusDays(30)
        ));
        if (createResult instanceof Result.Failure<?, ?> f) {
            log.error("[DemoDataSeeder] Failed to create membership: {}", f.error());
            throw new IllegalStateException("Demo seed failed at membership creation");
        }
        var membership = ((Result.Success<com.spottrack.platform.membership.domain.model.aggregates.Membership, ?>) createResult).value();
        var activateResult = membershipCommandService.handle(new ActivateMembershipCommand(membership.getMembershipId()));
        if (activateResult instanceof Result.Failure<?, ?> f) {
            log.error("[DemoDataSeeder] Failed to activate membership: {}", f.error());
            throw new IllegalStateException("Demo seed failed at membership activation");
        }
        log.info("[DemoDataSeeder] Membership PLATINUM created and activated.");
    }

    // ─── Whitelist ───────────────────────────────────────────────────────────

    private void seedWhitelist(String gymId) {
        for (var dniValue : List.of(CLIENT_DNI, CLIENT2_DNI, CLIENT3_DNI, ADMIN_DNI)) {
            var result = gymCommandService.handle(new AddDniToWhitelistCommand(
                    gymId, new com.spottrack.platform.gym.domain.model.valueobjects.Dni(dniValue)));
            if (result instanceof Result.Failure<?, ?> f)
                log.info("[DemoDataSeeder] DNI {} already in whitelist ({}), skipping.", dniValue, f.error());
            else
                log.info("[DemoDataSeeder] DNI {} added to whitelist.", dniValue);
        }
    }

    // ─── Demo client ─────────────────────────────────────────────────────────

    private Long seedClientUser(String gymId) {
        return seedSingleClient(CLIENT_EMAIL, "Cliente", "Demo", "999000002", CLIENT_DNI, gymId);
    }

    private void seedAdditionalClients(String gymId) {
        seedSingleClient(CLIENT2_EMAIL, "Carlos",  "Mendoza", "999000003", CLIENT2_DNI, gymId);
        seedSingleClient(CLIENT3_EMAIL, "Valeria", "Torres",  "999000004", CLIENT3_DNI, gymId);
    }

    private Long seedSingleClient(String email, String firstName, String lastName, String phone, String dni, String gymId) {
        Long clientUserId;
        if (userRepository.existsByUsername(email)) {
            log.info("[DemoDataSeeder] Client user '{}' already exists, skipping creation.", email);
            clientUserId = userRepository.findByUsername(email).orElseThrow().getId();
        } else {
            var clientRole = roleRepository.findByName(Roles.ROLE_CLIENT)
                    .orElseGet(() -> new Role(Roles.ROLE_CLIENT));
            var result = userCommandService.handle(new SignUpCommand(email, DEMO_PASSWORD, List.of(clientRole)));
            if (result instanceof Result.Failure<?, ?> f) {
                log.error("[DemoDataSeeder] Failed to create client user '{}': {}", email, f.error());
                throw new IllegalStateException("Demo seed failed at client user creation: " + email);
            }
            clientUserId = ((Result.Success<com.spottrack.platform.iam.domain.model.aggregates.User, ?>) result).value().getId();
            log.info("[DemoDataSeeder] Client user '{}' created, userId={}", email, clientUserId);
        }

        var client = clientQueryService.handle(new GetClientByUserIdQuery(clientUserId))
                .orElseThrow(() -> new IllegalStateException("Client profile not found after sign-up: " + email));

        if (!client.isProfileComplete()) {
            clientCommandService.handle(new UpdateClientProfileCommand(
                    new com.spottrack.platform.profiles.domain.model.valueobjects.ClientId(client.getId()),
                    firstName,
                    lastName,
                    new PhoneNumber(phone),
                    new com.spottrack.platform.profiles.domain.model.valueobjects.Dni(dni)
            ));
            log.info("[DemoDataSeeder] Client profile updated for '{}'.", email);
        }

        var assocResult = clientCommandService.handle(new AssociateClientWithGymCommand(client.getId(), gymId));
        if (assocResult instanceof Result.Failure<?, ?> f)
            log.info("[DemoDataSeeder] Client '{}' already associated with gym ({}), skipping.", email, f.error());
        else
            log.info("[DemoDataSeeder] Client '{}' associated with gym gymId={}.", email, gymId);

        return client.getId();
    }

    // ─── Reservations ────────────────────────────────────────────────────────

    private void seedReservations(Long clientProfileId, GymSeedResult gymSeed) {
        if (!reservationRepository.findByClientId(clientProfileId).isEmpty()) {
            log.info("[DemoDataSeeder] Reservations already exist for clientProfileId={}, skipping.", clientProfileId);
            return;
        }
        if (gymSeed.equipA() == null || gymSeed.equipB() == null) {
            log.warn("[DemoDataSeeder] Equipment IDs not available, skipping reservation seeding.");
            return;
        }

        // Create a historical reservation (ENDED) first, before the active one,
        // since the service rejects a second ACTIVE reservation for the same client.
        var histResult = reservationCommandService.handle(new InitiateExpressReservation(
                new com.spottrack.platform.reservation.domain.model.valueobjects.ClientId(clientProfileId),
                new com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId(gymSeed.equipB()),
                new TimeInterval(Time.valueOf("09:00:00"), Time.valueOf("10:00:00"))
        ));
        if (histResult instanceof Result.Failure<?, ?> f) {
            log.error("[DemoDataSeeder] Failed to create historical reservation: {}", f.error());
        } else {
            var histReservation = ((Result.Success<com.spottrack.platform.reservation.domain.model.aggregates.Reservation, ?>) histResult).value();
            var endResult = reservationCommandService.handle(new EndReservation(histReservation.getId()));
            if (endResult instanceof Result.Failure<?, ?> f)
                log.warn("[DemoDataSeeder] Failed to end historical reservation: {}", f.error());
            else
                log.info("[DemoDataSeeder] Historical reservation created and ended, uuid={}.", histReservation.getId().uuid());
        }

        // Create the current ACTIVE reservation
        var activeResult = reservationCommandService.handle(new InitiateExpressReservation(
                new com.spottrack.platform.reservation.domain.model.valueobjects.ClientId(clientProfileId),
                new com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId(gymSeed.equipA()),
                new TimeInterval(Time.valueOf("14:00:00"), Time.valueOf("15:00:00"))
        ));
        if (activeResult instanceof Result.Failure<?, ?> f) {
            log.error("[DemoDataSeeder] Failed to create active reservation: {}", f.error());
            return;
        }
        var activeReservation = ((Result.Success<com.spottrack.platform.reservation.domain.model.aggregates.Reservation, ?>) activeResult).value();
        reservationCommandService.handle(new StartReservationTimer(activeReservation.getId(), 60));
        log.info("[DemoDataSeeder] Active reservation created with 60-min timer, uuid={}.", activeReservation.getId().uuid());
    }

    // ─── Routines ────────────────────────────────────────────────────────────

    private void seedRoutines(Long clientProfileId) {
        var routineClientId = new com.spottrack.platform.routine.domain.model.valueobjects.ClientId(clientProfileId);
        if (!routineRepository.findAllByClientId(routineClientId).isEmpty()) {
            log.info("[DemoDataSeeder] Routines already exist for clientProfileId={}, skipping.", clientProfileId);
            return;
        }

        var routineResult = routineCommandService.handle(new CreateRoutineCommand(
                new RoutineName("Rutina de Iniciación"),
                routineClientId
        ));
        if (routineResult instanceof Result.Failure<?, ?> f) {
            log.error("[DemoDataSeeder] Failed to create routine: {}", f.error());
            return;
        }
        var routine = ((Result.Success<com.spottrack.platform.routine.domain.model.aggregates.Routine, ?>) routineResult).value();
        var routineId = routine.getId();

        routineCommandService.handle(new AddExerciseBlockCommand(routineId, new ExerciseName("Carrera en cinta"),  ExerciseType.CARDIO,      1, 1, 10));
        routineCommandService.handle(new AddExerciseBlockCommand(routineId, new ExerciseName("Sentadillas"),       ExerciseType.STRENGTH,    2, 3, 12));
        routineCommandService.handle(new AddExerciseBlockCommand(routineId, new ExerciseName("Estiramientos"),     ExerciseType.FLEXIBILITY, 3, 1, 1));
        log.info("[DemoDataSeeder] Routine 'Rutina de Iniciación' created with 3 exercise blocks, routineId={}.", routineId);

        var sessionResult = routineSessionCommandService.handle(new StartRoutineCommand(routineId, routineClientId));
        if (sessionResult instanceof Result.Failure<?, ?> f) {
            log.warn("[DemoDataSeeder] Failed to start routine session: {}", f.error());
            return;
        }
        var session = ((Result.Success<com.spottrack.platform.routine.domain.model.aggregates.RoutineSession, ?>) sessionResult).value();
        routineSessionCommandService.handle(new CompleteRoutineCommand(session.getId()));
        log.info("[DemoDataSeeder] Routine session completed for clientProfileId={}.", clientProfileId);
    }

    // ─── Maintenance ticket ──────────────────────────────────────────────────

    private void seedMaintenanceTicket(String outOfServiceEquipmentId) {
        if (outOfServiceEquipmentId == null) {
            log.warn("[DemoDataSeeder] OUT_OF_SERVICE equipment ID not available, skipping maintenance ticket.");
            return;
        }
        boolean alreadyExists = technicalTicketJpaRepository.findAll().stream()
                .anyMatch(t -> outOfServiceEquipmentId.equals(t.getEquipmentId()));
        if (alreadyExists) {
            log.info("[DemoDataSeeder] Maintenance ticket already exists for equipment {}, skipping.", outOfServiceEquipmentId);
            return;
        }
        var maintenanceResult = maintenanceCommandService.handle(new RequestMaintenance(
                new EquipmentId(outOfServiceEquipmentId), "SYSTEM",
                "Mantenimiento correctivo requerido — fallo detectado en sistema de freno."));
        if (maintenanceResult instanceof Result.Failure<?, ?> f) {
            log.error("[DemoDataSeeder] Failed to request maintenance: {}", f.error());
            return;
        }
        var maintenanceId = ((Result.Success<com.spottrack.platform.maintenance.domain.model.aggregates.Maintenance, ?>) maintenanceResult).value().getId().uuid();

        var result = maintenanceCommandService.handle(new CreateTechnicalTicketCommand(
                maintenanceId,
                TicketPriority.HIGH,
                TicketType.CORRECTIVE
        ));
        if (result instanceof Result.Failure<?, ?> f)
            log.error("[DemoDataSeeder] Failed to create maintenance ticket: {}", f.error());
        else
            log.info("[DemoDataSeeder] Maintenance ticket (HIGH/CORRECTIVE) created for equipment {}.", outOfServiceEquipmentId);
    }

    // ─── Client association with second gym ──────────────────────────────────

    private void seedWhitelistForClient(String gymId) {
        var result = gymCommandService.handle(new AddDniToWhitelistCommand(
                gymId, new com.spottrack.platform.gym.domain.model.valueobjects.Dni(CLIENT_DNI)));
        if (result instanceof Result.Failure<?, ?> f)
            log.info("[DemoDataSeeder] CLIENT_DNI already in whitelist for gym2 ({}), skipping.", f.error());
        else
            log.info("[DemoDataSeeder] CLIENT_DNI added to whitelist of gym2={}.", gymId);
    }

    private void associateClientWithSecondGym(Long clientProfileId, String gym2Id) {
        var result = clientCommandService.handle(new AssociateClientWithGymCommand(clientProfileId, gym2Id));
        if (result instanceof Result.Failure<?, ?> f)
            log.info("[DemoDataSeeder] Client already associated with gym2 ({}), skipping.", f.error());
        else
            log.info("[DemoDataSeeder] Client {} associated with FitZone Lima gymId={}.", clientProfileId, gym2Id);
    }

    // ─── Second gym (FitZone Lima) ───────────────────────────────────────────

    private String seedSecondGym(Long admin2UserId) {
        var existing = gymPersistenceRepository.findByAdminUserId(admin2UserId);
        if (!existing.isEmpty()) {
            log.info("[DemoDataSeeder] FitZone Lima gym already exists gymId={}, skipping.", existing.get(0).getGymId());
            return existing.get(0).getGymId();
        }

        var gymResult = gymCommandService.handle(new CreateGym(GYM2_NAME, admin2UserId));
        if (gymResult instanceof Result.Failure<?, ?> f)
            throw new IllegalStateException("Demo seed failed at second gym creation: " + f.error());
        var gym2Id = ((Result.Success<com.spottrack.platform.gym.domain.model.aggregates.Gym, ?>) gymResult).value().getId().uuid();
        log.info("[DemoDataSeeder] FitZone Lima gym created gymId={}.", gym2Id);

        // Branch 1 — Barranco
        var branch1  = addBranch(gym2Id, "Sede Barranco", "Av. Grau 345, Barranco");
        var cardio2  = addZone("Cardio", 10, branch1);
        addEquipment("Cinta Pro FZ",       EquipmentStatus.AVAILABLE, "FZ-Treadmill-500",  cardio2, 1100.00);
        addEquipment("Bicicleta Spinner",  EquipmentStatus.AVAILABLE, "FZ-Spinner-200",    cardio2,  750.00);
        var pesas2   = addZone("Pesas Libres", 15, branch1);
        addEquipment("Mancuernas 20kg",   EquipmentStatus.AVAILABLE, "FZ-Dumbbell-20",    pesas2,  200.00);
        addEquipment("Barra Curl",        EquipmentStatus.AVAILABLE, "FZ-CurlBar-15",     pesas2,  130.00);

        // Branch 2 — Surco
        var branch2  = addBranch(gym2Id, "Sede Surco", "Av. Caminos del Inca 890, Surco");
        var funcFZ   = addZone("Funcional", 12, branch2);
        addEquipment("TRX Profesional",  EquipmentStatus.AVAILABLE, "FZ-TRX-Pro",        funcFZ,  300.00);
        addEquipment("Cuerda Battle",    EquipmentStatus.AVAILABLE, "FZ-BattleRope-15m", funcFZ,  180.00);
        var maqFZ    = addZone("Máquinas", 16, branch2);
        addEquipment("Extensora de Piernas", EquipmentStatus.AVAILABLE, "FZ-LegExt-X1",  maqFZ,  900.00);
        addEquipment("Chest Press",          EquipmentStatus.AVAILABLE, "FZ-ChestP-500",  maqFZ, 1050.00);

        log.info("[DemoDataSeeder] FitZone Lima seeded: 2 branches, 4 zones, 8 equipment.");
        return gym2Id;
    }

    // ─── Second admin (FitZone Lima) ─────────────────────────────────────────

    private Long seedSecondAdmin() {
        if (userRepository.existsByUsername(ADMIN2_EMAIL)) {
            log.info("[DemoDataSeeder] Second admin user already exists, skipping.");
            return userRepository.findByUsername(ADMIN2_EMAIL).orElseThrow().getId();
        }
        var adminRole = roleRepository.findByName(Roles.ROLE_ADMIN)
                .orElseGet(() -> new Role(Roles.ROLE_ADMIN));
        var result = userCommandService.handle(new SignUpCommand(ADMIN2_EMAIL, DEMO_PASSWORD, List.of(adminRole)));
        if (result instanceof Result.Failure<?, ?> f)
            throw new IllegalStateException("Demo seed failed at second admin user creation: " + f.error());
        var admin2UserId = ((Result.Success<com.spottrack.platform.iam.domain.model.aggregates.User, ?>) result).value().getId();

        var admin = adminQueryService.handle(new GetAdminByUserIdQuery(admin2UserId))
                .orElseThrow(() -> new IllegalStateException("Second admin profile not found after sign-up"));
        if (!admin.isProfileComplete()) {
            adminCommandService.handle(new UpdateAdminProfileCommand(
                    new AdminId(admin.getId()),
                    "Marco",
                    "Quispe",
                    new PhoneNumber("999000006"),
                    new com.spottrack.platform.profiles.domain.model.valueobjects.Dni(ADMIN2_DNI)
            ));
        }
        seedBusinessProfile(admin2UserId,
                "FitZone Lima S.A.C.", "20987654321",
                "Av. El Sol 567", "Lima", "Barranco",
                "999000006", ADMIN2_EMAIL);
        log.info("[DemoDataSeeder] Second admin (Marco Quispe) created, userId={}.", admin2UserId);
        return admin2UserId;
    }

    private void seedMotionSensor(String equipmentId) {
        if (equipmentId == null) {
            log.warn("[DemoDataSeeder] Equipment ID not available, skipping motion sensor seeding.");
            return;
        }
        if (motionSensorRepository.existsByEquipmentId(new com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId(equipmentId))) {
            log.info("[DemoDataSeeder] Motion sensor already exists for equipment {}, skipping.", equipmentId);
            return;
        }
        var result = motionSensorCommandService.handle(
                new com.spottrack.platform.monitoring.domain.model.commands.RegisterMotionSensorCommand(equipmentId));
        if (result instanceof Result.Failure<?, ?> f) {
            log.warn("[DemoDataSeeder] Failed to seed motion sensor: {}", f.error());
            return;
        }
        log.info("[DemoDataSeeder] Motion sensor seeded for equipment {}.", equipmentId);
    }
}
