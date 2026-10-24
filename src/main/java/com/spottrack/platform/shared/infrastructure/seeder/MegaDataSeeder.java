package com.spottrack.platform.shared.infrastructure.seeder;

import com.spottrack.platform.analytics.application.commandservices.ActivityReportCommandService;
import com.spottrack.platform.analytics.application.commandservices.MaintenanceQuoteCommandService;
import com.spottrack.platform.analytics.application.commandservices.ROIProjectionCommandService;
import com.spottrack.platform.analytics.domain.model.commands.DetectHighDemandSlotCommand;
import com.spottrack.platform.analytics.domain.model.commands.DetectLowDemandSlotCommand;
import com.spottrack.platform.analytics.domain.model.commands.RecommendTransferCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestADetailedMaintenanceQuoteCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestDowntimeCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestEarningsCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestPercentageComparisonCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestPreventiveCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestRoiCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestSparePartsCommand;
import com.spottrack.platform.analytics.domain.repositories.ActivityReportRepository;
import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.application.commandServices.GymCommandService;
import com.spottrack.platform.gym.domain.model.commands.AddBranchCommand;
import com.spottrack.platform.gym.domain.model.commands.AddDniToWhitelistCommand;
import com.spottrack.platform.gym.domain.model.commands.AddZoneCommand;
import com.spottrack.platform.gym.domain.model.commands.CreateGym;
import com.spottrack.platform.gym.domain.model.commands.DecomissionEquipment;
import com.spottrack.platform.gym.domain.model.commands.DefineMaintenanceThresholdCommand;
import com.spottrack.platform.gym.domain.model.commands.RegisterEquipment;
import com.spottrack.platform.gym.domain.model.valueobjects.BranchId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.ManufacturerId;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;
import com.spottrack.platform.iam.application.commandservices.RoleCommandService;
import com.spottrack.platform.iam.application.commandservices.UserCommandService;
import com.spottrack.platform.iam.domain.model.commands.SeedRolesCommand;
import com.spottrack.platform.iam.domain.model.commands.SignUpCommand;
import com.spottrack.platform.iam.domain.model.entities.Role;
import com.spottrack.platform.iam.domain.model.valueobjects.Roles;
import com.spottrack.platform.iam.domain.repositories.RoleRepository;
import com.spottrack.platform.iam.domain.repositories.UserRepository;
import com.spottrack.platform.maintenance.application.commandServices.MaintenanceCommandService;
import com.spottrack.platform.maintenance.domain.model.commands.AssignTechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.commands.CompleteMaintenance;
import com.spottrack.platform.maintenance.domain.model.commands.CreateTechnician;
import com.spottrack.platform.maintenance.domain.model.commands.CreateTechnicalTicketCommand;
import com.spottrack.platform.maintenance.domain.model.commands.RegisterMaintenanceCompletion;
import com.spottrack.platform.maintenance.domain.model.commands.RequestMaintenance;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicalTicketId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketPriority;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketType;
import com.spottrack.platform.membership.application.commandservices.MembershipCommandService;
import com.spottrack.platform.membership.domain.model.aggregates.Membership;
import com.spottrack.platform.membership.domain.model.aggregates.Payment;
import com.spottrack.platform.membership.domain.model.commands.ActivateMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.CancelMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.CreateMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.RequestDowngradePlanCommand;
import com.spottrack.platform.membership.domain.model.commands.SuspendMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.UpgradeMembershipPlanCommand;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.membership.domain.model.valueobjects.PaymentId;
import com.spottrack.platform.membership.domain.model.valueobjects.PaymentPurpose;
import com.spottrack.platform.membership.domain.model.valueobjects.PaymentStatus;
import com.spottrack.platform.membership.domain.repositories.MembershipRepository;
import com.spottrack.platform.membership.domain.repositories.PaymentRepository;
import com.spottrack.platform.monitoring.application.commandServices.AnomalyCommandService;
import com.spottrack.platform.monitoring.application.commandServices.CameraSensorCommandService;
import com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService;
import com.spottrack.platform.monitoring.domain.model.commands.MarkMotionSensorDisconnectedCommand;
import com.spottrack.platform.monitoring.domain.model.commands.RegisterCameraSensorCommand;
import com.spottrack.platform.monitoring.domain.model.commands.RegisterMotionSensorCommand;
import com.spottrack.platform.monitoring.domain.model.commands.ReportAnomalyCommand;
import com.spottrack.platform.monitoring.interfaces.events.SessionTimeCalculatedAnalyticsIntegrationEvent;
import com.spottrack.platform.profiles.application.commandservices.AdminCommandService;
import com.spottrack.platform.profiles.application.commandservices.BusinessProfileCommandService;
import com.spottrack.platform.profiles.application.commandservices.ClientCommandService;
import com.spottrack.platform.profiles.application.queryservices.AdminQueryService;
import com.spottrack.platform.profiles.application.queryservices.ClientQueryService;
import com.spottrack.platform.profiles.domain.model.commands.AssociateClientWithGymCommand;
import com.spottrack.platform.profiles.domain.model.commands.CreateBusinessProfileCommand;
import com.spottrack.platform.profiles.domain.model.commands.UpdateAdminProfileCommand;
import com.spottrack.platform.profiles.domain.model.commands.UpdateClientProfileCommand;
import com.spottrack.platform.profiles.domain.model.queries.GetAdminByUserIdQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetClientByUserIdQuery;
import com.spottrack.platform.profiles.domain.model.valueobjects.AdminId;
import com.spottrack.platform.profiles.domain.model.valueobjects.BusinessInfo;
import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.domain.model.valueobjects.PhoneNumber;
import com.spottrack.platform.reservation.application.commandServices.ReservationCommandService;
import com.spottrack.platform.reservation.domain.model.commands.CancelReservation;
import com.spottrack.platform.reservation.domain.model.commands.EndReservation;
import com.spottrack.platform.reservation.domain.model.commands.InitiateExpressReservation;
import com.spottrack.platform.reservation.domain.model.commands.StartReservationTimer;
import com.spottrack.platform.reservation.domain.model.valueobjects.TimeInterval;
import com.spottrack.platform.routine.application.commandservices.RoutineCommandService;
import com.spottrack.platform.routine.application.commandservices.RoutineSessionCommandService;
import com.spottrack.platform.routine.domain.model.commands.AddExerciseBlockCommand;
import com.spottrack.platform.routine.domain.model.commands.CompleteRoutineCommand;
import com.spottrack.platform.routine.domain.model.commands.CreateRoutineCommand;
import com.spottrack.platform.routine.domain.model.commands.MarkRoutineMissedCommand;
import com.spottrack.platform.routine.domain.model.commands.SetExerciseBlockCompletionCommand;
import com.spottrack.platform.routine.domain.model.commands.StartRoutineCommand;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseName;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseType;
import com.spottrack.platform.routine.domain.model.valueobjects.RoutineName;
import com.spottrack.platform.shared.application.commandservices.AlertCommandService;
import com.spottrack.platform.shared.application.queryservices.AlertQueryService;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.domain.model.commands.ResolveAlertCommand;
import com.spottrack.platform.shared.domain.model.queries.GetAlertsByAdminUserIdQuery;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Time;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Opt-in "mega" seeder (profile "seed-mega") that fills every bounded context with a large,
 * varied and deterministic data set for local development: six gym chains whose admins sit in
 * every membership state (active, upgraded, cancellation scheduled, downgrade pending, suspended,
 * expired), ~150 pieces of equipment with motion/camera sensors and usage history, maintenance
 * tickets in every kanban column, alerts (open and resolved), ~120 clients spread across gyms,
 * reservation history plus live reservations, and routines with completed, missed and in-progress
 * sessions.
 *
 * Everything goes through the real command services and integration events, so the side effects
 * (activity reports, equipment status changes, session trackers, alerts) are the ones the app
 * produces itself. Payment history is written straight to the repository so no Stripe call is made.
 *
 * Run with: SPRING_PROFILES_ACTIVE=dev,seed-mega (on an empty or existing local database).
 * Every account uses the password "mega123". The seeder skips itself if mega@spottrack.com exists.
 */
@Component
@Profile("seed-mega")
@Slf4j
public class MegaDataSeeder {

    private static final String MARKER_EMAIL = "mega@spottrack.com";
    private static final String PASSWORD = "mega123";
    private static final String MANUFACTURER_ID = "00000000-0000-0000-0000-000000000001";
    private static final long RANDOM_SEED = 20261024L;

    // ── Catalog ──────────────────────────────────────────────────────────────

    private enum PlanState { ACTIVE, UPGRADED_THEN_CANCELLING, DOWNGRADE_PENDING, SUSPENDED, EXPIRED }

    private record AdminSpec(String email, String firstName, String lastName, String phone, String dni,
                             String company, String ruc, String street, String city, String district,
                             MembershipTier tier, PlanState plan, String gymName, List<BranchSpec> branches,
                             int clients) {}

    private record BranchSpec(String name, String address, double size) {}

    private record Machine(String name, String zone, String model, double price, String partName, double partPrice,
                           ExerciseType exerciseType) {}

    private static final Map<String, Integer> ZONE_CAPACITY = Map.of(
            "Cardio", 30, "Fuerza", 25, "Funcional", 15, "Peso Libre", 20);

    private static final List<Machine> CATALOG = List.of(
            new Machine("Cinta de Correr", "Cardio", "Life Fitness T5", 4800, "Banda de rodamiento", 180, ExerciseType.CARDIO),
            new Machine("Elíptica", "Cardio", "Precor EFX 835", 3200, "Rodamiento de pedal", 65, ExerciseType.CARDIO),
            new Machine("Bicicleta Estática", "Cardio", "Keiser M3i", 1900, "Correa de transmisión", 45, ExerciseType.CARDIO),
            new Machine("Remo", "Cardio", "Concept2 RowErg", 1600, "Cadena", 55, ExerciseType.CARDIO),
            new Machine("Escaladora", "Cardio", "StairMaster 8Gx", 5500, "Motor de escalones", 420, ExerciseType.CARDIO),
            new Machine("Prensa de Piernas", "Fuerza", "Hammer Strength PL", 2800, "Rodamiento lineal", 90, ExerciseType.STRENGTH),
            new Machine("Polea Cruzada", "Fuerza", "Matrix Aura G3", 3900, "Cable de acero", 60, ExerciseType.STRENGTH),
            new Machine("Máquina Smith", "Fuerza", "Life Fitness Signature", 2400, "Rodamiento lineal", 90, ExerciseType.STRENGTH),
            new Machine("Extensora de Piernas", "Fuerza", "Technogym Selection", 2100, "Pasador de selector", 25, ExerciseType.STRENGTH),
            new Machine("Jalón al Pecho", "Fuerza", "Cybex Prestige", 2300, "Cable de acero", 60, ExerciseType.STRENGTH),
            new Machine("Multiestación Funcional", "Funcional", "Technogym Omnia", 6000, "Anclaje de suspensión", 85, ExerciseType.STRENGTH),
            new Machine("Assault Bike", "Funcional", "Rogue Echo", 1200, "Ventilador", 75, ExerciseType.CARDIO),
            new Machine("Sled de Empuje", "Funcional", "Rogue Dog Sled", 650, "Patín", 40, ExerciseType.STRENGTH),
            new Machine("Rack de Sentadillas", "Peso Libre", "Rogue RML-490", 1500, "Seguro de barra", 30, ExerciseType.STRENGTH),
            new Machine("Banco Press", "Peso Libre", "Hammer Strength Flat", 900, "Tapizado", 70, ExerciseType.STRENGTH),
            new Machine("Banco Ajustable", "Peso Libre", "Rogue Adjustable 3.0", 750, "Pin de ajuste", 20, ExerciseType.FLEXIBILITY));

    private static final List<AdminSpec> ADMINS = List.of(
            new AdminSpec(MARKER_EMAIL, "Carolina", "Vargas", "981000001", "70000001",
                    "Mega Power Gym S.A.C.", "20600000011", "Av. Larco 1150", "Lima", "Miraflores",
                    MembershipTier.PLATINUM, PlanState.ACTIVE, "Mega Power Gym", List.of(
                    new BranchSpec("Mega Power Miraflores", "Av. Larco 1150, Miraflores", 1.4),
                    new BranchSpec("Mega Power San Isidro", "Av. Camino Real 1200, San Isidro", 1.2),
                    new BranchSpec("Mega Power Surco", "Av. Primavera 1450, Surco", 1.0),
                    new BranchSpec("Mega Power La Molina", "Av. La Molina 980, La Molina", 0.7)), 45),
            new AdminSpec("fitlife@spottrack.com", "Renato", "Castillo", "981000002", "70000002",
                    "FitLife Perú S.A.C.", "20600000022", "Av. Brasil 2100", "Lima", "Jesús María",
                    MembershipTier.BASIC, PlanState.UPGRADED_THEN_CANCELLING, "FitLife Perú", List.of(
                    new BranchSpec("FitLife Jesús María", "Av. Brasil 2100, Jesús María", 1.0),
                    new BranchSpec("FitLife Lince", "Av. Arequipa 2450, Lince", 0.8),
                    new BranchSpec("FitLife Pueblo Libre", "Av. Sucre 520, Pueblo Libre", 0.6)), 25),
            new AdminSpec("ironbox@spottrack.com", "Fiorella", "Medina", "981000003", "70000003",
                    "Iron Box Crossfit E.I.R.L.", "20600000033", "Av. Grau 640", "Lima", "Barranco",
                    MembershipTier.MID, PlanState.DOWNGRADE_PENDING, "Iron Box Crossfit", List.of(
                    new BranchSpec("Iron Box Barranco", "Av. Grau 640, Barranco", 0.9),
                    new BranchSpec("Iron Box Chorrillos", "Av. Huaylas 1300, Chorrillos", 0.6)), 20),
            new AdminSpec("barrio@spottrack.com", "Julio", "Ccori", "981000004", "70000004",
                    "Gimnasio El Barrio E.I.R.L.", "20600000044", "Av. Próceres 3200", "Lima", "San Juan de Lurigancho",
                    MembershipTier.BASIC, PlanState.ACTIVE, "Gimnasio El Barrio", List.of(
                    new BranchSpec("El Barrio SJL", "Av. Próceres de la Independencia 3200, SJL", 0.8)), 12),
            new AdminSpec("arequipafit@spottrack.com", "Milagros", "Zegarra", "981000005", "70000005",
                    "Arequipa Fit S.A.C.", "20600000055", "Av. Ejército 710", "Arequipa", "Cayma",
                    MembershipTier.MID, PlanState.SUSPENDED, "Arequipa Fit", List.of(
                    new BranchSpec("Arequipa Fit Cayma", "Av. Ejército 710, Cayma", 0.8),
                    new BranchSpec("Arequipa Fit Yanahuara", "Av. Bolognesi 120, Yanahuara", 0.5)), 13),
            new AdminSpec("exgym@spottrack.com", "Hugo", "Paredes", "981000006", "70000006",
                    "Ex Gym Trujillo S.A.C.", "20600000066", "Av. España 1500", "Trujillo", "Trujillo",
                    MembershipTier.MID, PlanState.EXPIRED, null, List.of(), 0));

    private static final List<String> FIRST_NAMES = List.of(
            "Andrea", "Luis", "María", "José", "Valeria", "Diego", "Camila", "Sebastián", "Lucía", "Mateo",
            "Daniela", "Alonso", "Fernanda", "Gabriel", "Ximena", "Rodrigo", "Paola", "Joaquín", "Mariana", "Bruno",
            "Rocío", "Gonzalo", "Natalia", "Piero", "Claudia", "Franco", "Alessandra", "Martín", "Sofía", "Kevin");

    private static final List<String> LAST_NAMES = List.of(
            "Quispe", "Flores", "Rodríguez", "Sánchez", "García", "Rojas", "Huamán", "Chávez", "Torres", "Ramírez",
            "Mendoza", "Díaz", "Castro", "Vásquez", "Gutiérrez", "Salazar", "Córdova", "Paredes", "Ríos", "Espinoza");

    private record RoutineTemplate(String name, List<Object[]> blocks) {}

    private static final List<RoutineTemplate> ROUTINES = List.of(
            new RoutineTemplate("Full Body Principiante", List.of(
                    new Object[]{"Caminata en cinta", ExerciseType.CARDIO, 1, 10},
                    new Object[]{"Sentadilla goblet", ExerciseType.STRENGTH, 3, 12},
                    new Object[]{"Press de pecho en máquina", ExerciseType.STRENGTH, 3, 10},
                    new Object[]{"Estiramiento general", ExerciseType.FLEXIBILITY, 1, 1})),
            new RoutineTemplate("Pierna y Glúteo", List.of(
                    new Object[]{"Prensa de piernas", ExerciseType.STRENGTH, 4, 12},
                    new Object[]{"Extensión de cuádriceps", ExerciseType.STRENGTH, 3, 15},
                    new Object[]{"Hip thrust", ExerciseType.STRENGTH, 4, 10},
                    new Object[]{"Escaladora", ExerciseType.CARDIO, 1, 15})),
            new RoutineTemplate("Torso Hipertrofia", List.of(
                    new Object[]{"Press banca", ExerciseType.STRENGTH, 4, 8},
                    new Object[]{"Jalón al pecho", ExerciseType.STRENGTH, 4, 10},
                    new Object[]{"Cruce de poleas", ExerciseType.STRENGTH, 3, 12},
                    new Object[]{"Movilidad de hombros", ExerciseType.FLEXIBILITY, 2, 10})),
            new RoutineTemplate("Cardio HIIT 30'", List.of(
                    new Object[]{"Assault bike sprints", ExerciseType.CARDIO, 8, 1},
                    new Object[]{"Remo intervalos", ExerciseType.CARDIO, 6, 1},
                    new Object[]{"Burpees", ExerciseType.CARDIO, 4, 15})),
            new RoutineTemplate("Movilidad y Core", List.of(
                    new Object[]{"Plancha", ExerciseType.STRENGTH, 3, 1},
                    new Object[]{"Dead bug", ExerciseType.STRENGTH, 3, 12},
                    new Object[]{"Yoga flow", ExerciseType.FLEXIBILITY, 1, 1})));

    // ── Seed-time state ──────────────────────────────────────────────────────

    private static final class SeededEquipment {
        String id;
        String name;
        String zoneId;
        String branchName;
        Machine machine;
        double usageMinutes;
        int downtimeMinutes;
        boolean touchedByMaintenance;
    }

    private static final class SeededGym {
        Long adminUserId;
        String gymId;
        final List<SeededEquipment> equipment = new ArrayList<>();
        final List<Long> clientIds = new ArrayList<>();
    }

    private final Random random = new Random(RANDOM_SEED);
    private final Set<String> usedEmails = new HashSet<>();
    private int dniCounter = 71000000;
    private final Map<String, Integer> stats = new LinkedHashMap<>();

    private final RoleCommandService roleCommandService;
    private final UserCommandService userCommandService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AdminQueryService adminQueryService;
    private final AdminCommandService adminCommandService;
    private final ClientQueryService clientQueryService;
    private final ClientCommandService clientCommandService;
    private final BusinessProfileCommandService businessProfileCommandService;
    private final MembershipCommandService membershipCommandService;
    private final MembershipRepository membershipRepository;
    private final PaymentRepository paymentRepository;
    private final GymCommandService gymCommandService;
    private final EquipmentCommandService equipmentCommandService;
    private final MotionSensorCommandService motionSensorCommandService;
    private final CameraSensorCommandService cameraSensorCommandService;
    private final AnomalyCommandService anomalyCommandService;
    private final MaintenanceCommandService maintenanceCommandService;
    private final MaintenanceQuoteCommandService maintenanceQuoteCommandService;
    private final ActivityReportCommandService activityReportCommandService;
    private final ActivityReportRepository activityReportRepository;
    private final ROIProjectionCommandService roiProjectionCommandService;
    private final AlertCommandService alertCommandService;
    private final AlertQueryService alertQueryService;
    private final ReservationCommandService reservationCommandService;
    private final RoutineCommandService routineCommandService;
    private final RoutineSessionCommandService routineSessionCommandService;
    private final ApplicationEventPublisher eventPublisher;

    public MegaDataSeeder(RoleCommandService roleCommandService,
                          UserCommandService userCommandService,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          AdminQueryService adminQueryService,
                          AdminCommandService adminCommandService,
                          ClientQueryService clientQueryService,
                          ClientCommandService clientCommandService,
                          BusinessProfileCommandService businessProfileCommandService,
                          MembershipCommandService membershipCommandService,
                          MembershipRepository membershipRepository,
                          PaymentRepository paymentRepository,
                          GymCommandService gymCommandService,
                          EquipmentCommandService equipmentCommandService,
                          MotionSensorCommandService motionSensorCommandService,
                          CameraSensorCommandService cameraSensorCommandService,
                          AnomalyCommandService anomalyCommandService,
                          MaintenanceCommandService maintenanceCommandService,
                          MaintenanceQuoteCommandService maintenanceQuoteCommandService,
                          ActivityReportCommandService activityReportCommandService,
                          ActivityReportRepository activityReportRepository,
                          ROIProjectionCommandService roiProjectionCommandService,
                          AlertCommandService alertCommandService,
                          AlertQueryService alertQueryService,
                          ReservationCommandService reservationCommandService,
                          RoutineCommandService routineCommandService,
                          RoutineSessionCommandService routineSessionCommandService,
                          ApplicationEventPublisher eventPublisher) {
        this.roleCommandService = roleCommandService;
        this.userCommandService = userCommandService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.adminQueryService = adminQueryService;
        this.adminCommandService = adminCommandService;
        this.clientQueryService = clientQueryService;
        this.clientCommandService = clientCommandService;
        this.businessProfileCommandService = businessProfileCommandService;
        this.membershipCommandService = membershipCommandService;
        this.membershipRepository = membershipRepository;
        this.paymentRepository = paymentRepository;
        this.gymCommandService = gymCommandService;
        this.equipmentCommandService = equipmentCommandService;
        this.motionSensorCommandService = motionSensorCommandService;
        this.cameraSensorCommandService = cameraSensorCommandService;
        this.anomalyCommandService = anomalyCommandService;
        this.maintenanceCommandService = maintenanceCommandService;
        this.maintenanceQuoteCommandService = maintenanceQuoteCommandService;
        this.activityReportCommandService = activityReportCommandService;
        this.activityReportRepository = activityReportRepository;
        this.roiProjectionCommandService = roiProjectionCommandService;
        this.alertCommandService = alertCommandService;
        this.alertQueryService = alertQueryService;
        this.reservationCommandService = reservationCommandService;
        this.routineCommandService = routineCommandService;
        this.routineSessionCommandService = routineSessionCommandService;
        this.eventPublisher = eventPublisher;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(30)
    public void seed(ApplicationReadyEvent event) {
        if (userRepository.existsByUsername(MARKER_EMAIL)) {
            log.info("[MegaDataSeeder] {} already exists, skipping. Drop the database to re-seed.", MARKER_EMAIL);
            return;
        }
        long started = System.currentTimeMillis();
        log.info("[MegaDataSeeder] Starting mega seed...");
        roleCommandService.handle(new SeedRolesCommand());

        var technicianIds = createTechnicians();
        var gyms = new ArrayList<SeededGym>();
        for (var spec : ADMINS) {
            var gym = seedAdminAndGym(spec);
            if (gym != null) gyms.add(gym);
        }

        for (int g = 0; g < gyms.size(); g++) {
            var gym = gyms.get(g);
            var spec = ADMINS.get(g);
            for (var equipment : gym.equipment) {
                seedSensors(equipment);
                seedUsage(equipment);
                seedMaintenance(equipment, technicianIds);
            }
            for (var equipment : gym.equipment) adjustActivityReport(equipment);
            seedRoiProjections(gym, spec);
            seedIncidents(gym);
            seedClients(gym, spec);
        }
        seedCrossGymClients(gyms);

        for (var gym : gyms) {
            seedReservations(gym);
            for (var clientId : gym.clientIds) seedRoutines(clientId);
            resolveSomeAlerts(gym);
        }

        log.info("[MegaDataSeeder] Done in {}s: {}", (System.currentTimeMillis() - started) / 1000, stats);
        log.info("[MegaDataSeeder] Admin logins (password '{}'): {}", PASSWORD,
                ADMINS.stream().map(a -> a.email() + " [" + a.tier() + "/" + a.plan() + "]").toList());
        log.info("[MegaDataSeeder] Featured client login: cliente.mega@spottrack.com / {}", PASSWORD);
    }

    // ── Admins, memberships, payments, gyms ──────────────────────────────────

    private SeededGym seedAdminAndGym(AdminSpec spec) {
        var userId = signUp(spec.email(), Roles.ROLE_ADMIN);
        var admin = adminQueryService.handle(new GetAdminByUserIdQuery(userId))
                .orElseThrow(() -> new IllegalStateException("Admin profile not found after sign-up: " + spec.email()));
        adminCommandService.handle(new UpdateAdminProfileCommand(new AdminId(admin.getId()), spec.firstName(), spec.lastName(),
                new PhoneNumber(spec.phone()), new com.spottrack.platform.profiles.domain.model.valueobjects.Dni(spec.dni())));
        soft("business profile " + spec.company(), () -> businessProfileCommandService.handle(new CreateBusinessProfileCommand(userId,
                new BusinessInfo(spec.company(), spec.ruc(), spec.company().endsWith("E.I.R.L.") ? "E.I.R.L." : "S.A.C.",
                        new PhoneNumber(spec.phone()), new EmailAddress(spec.email()), spec.street(), spec.city(), spec.district()))));
        count("admins");

        var today = LocalDate.now();
        // MembershipPeriod rejects end dates before today, also when a row is loaded back from the
        // database, so no membership here ends in the past: history lives in the payments instead.
        if (spec.plan() == PlanState.EXPIRED) {
            var lapsed = createActiveMembership(userId, spec.tier(), today.minusDays(365), today.plusDays(30));
            expire(lapsed);
            savePayment(userId, lapsed, spec.tier(), PaymentPurpose.NEW_MEMBERSHIP, PaymentStatus.CONFIRMED);
            savePayment(userId, null, spec.tier(), PaymentPurpose.RESUBSCRIPTION, PaymentStatus.FAILED);
            savePayment(userId, null, spec.tier(), PaymentPurpose.RESUBSCRIPTION, PaymentStatus.PENDING);
            return null;
        }

        var current = createActiveMembership(userId, spec.tier(), today.minusDays(20 + random.nextInt(10)), today.plusDays(30 + random.nextInt(30)));
        savePayment(userId, current, spec.tier(), PaymentPurpose.NEW_MEMBERSHIP, PaymentStatus.CONFIRMED);
        savePayment(userId, current, spec.tier(), PaymentPurpose.DEBT_RENEWAL, PaymentStatus.CONFIRMED);
        if (spec.plan() == PlanState.UPGRADED_THEN_CANCELLING) {
            // BASIC → MID so the 3 branches fit, then the admin asks to cancel at period end.
            require(membershipCommandService.handle(new UpgradeMembershipPlanCommand(current.getMembershipId(), MembershipTier.MID)), "upgrade");
            savePayment(userId, current, MembershipTier.MID, PaymentPurpose.PLAN_UPGRADE, PaymentStatus.CONFIRMED);
        }

        var gym = new SeededGym();
        gym.adminUserId = userId;
        gym.gymId = require(gymCommandService.handle(new CreateGym(spec.gymName(), userId)), "gym " + spec.gymName()).getId().uuid();
        for (var branch : spec.branches()) {
            seedBranch(gym, branch);
        }

        switch (spec.plan()) {
            case UPGRADED_THEN_CANCELLING -> soft("cancel", () -> membershipCommandService.handle(new CancelMembershipCommand(current.getMembershipId())));
            case DOWNGRADE_PENDING -> soft("downgrade", () -> membershipCommandService.handle(new RequestDowngradePlanCommand(current.getMembershipId(), MembershipTier.BASIC)));
            case SUSPENDED -> {
                savePayment(userId, current, spec.tier(), PaymentPurpose.DEBT_RENEWAL, PaymentStatus.FAILED);
                soft("suspend", () -> membershipCommandService.handle(new SuspendMembershipCommand(current.getMembershipId())));
            }
            default -> { }
        }
        count("gyms");
        return gym;
    }

    private Membership createActiveMembership(Long userId, MembershipTier tier, LocalDate start, LocalDate end) {
        var membership = require(membershipCommandService.handle(new CreateMembershipCommand(userId, tier, tier.toMoney(), start, end)), "membership");
        require(membershipCommandService.handle(new ActivateMembershipCommand(membership.getMembershipId())), "membership activation");
        count("memberships");
        return membership;
    }

    private void expire(Membership membership) {
        var stored = membershipRepository.findByMembershipId(membership.getMembershipId()).orElseThrow();
        stored.expire();
        membershipRepository.save(stored);
    }

    /** Written straight to the repository: going through PaymentCommandService would open a Stripe checkout. */
    private void savePayment(Long userId, Membership membership, MembershipTier tier, PaymentPurpose purpose, PaymentStatus status) {
        var transactionId = status == PaymentStatus.PENDING ? null : "seed_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        paymentRepository.save(new Payment(null, new PaymentId(), userId, null,
                membership == null ? null : membership.getMembershipId().uuid(), tier, tier.toMoney(), status, transactionId, purpose));
        count("payments");
    }

    private void seedBranch(SeededGym gym, BranchSpec spec) {
        var branchId = require(gymCommandService.handle(new AddBranchCommand(gym.gymId, spec.name(), spec.address())), "branch " + spec.name())
                .getId().uuid();
        count("branches");
        var zoneIds = new LinkedHashMap<String, String>();
        for (var zone : ZONE_CAPACITY.keySet().stream().sorted().toList()) {
            zoneIds.put(zone, require(gymCommandService.handle(new AddZoneCommand(zone,
                    (int) Math.round(ZONE_CAPACITY.get(zone) * spec.size()), new BranchId(branchId))), "zone " + zone).getId().uuid());
            count("zones");
        }
        for (var machine : CATALOG) {
            // Not every branch carries every machine.
            int units = random.nextDouble() < 0.2 ? 0 : Math.max(1, (int) Math.round(spec.size() * (1 + random.nextInt(2))));
            for (int i = 1; i <= units; i++) {
                var equipment = new SeededEquipment();
                equipment.machine = machine;
                equipment.branchName = spec.name();
                equipment.zoneId = zoneIds.get(machine.zone());
                equipment.name = "%s %02d · %s".formatted(machine.name(), i, spec.name());
                equipment.id = require(equipmentCommandService.handle(new RegisterEquipment(equipment.name, EquipmentStatus.AVAILABLE,
                        machine.model(), new ManufacturerId(MANUFACTURER_ID), new ZoneId(equipment.zoneId),
                        new Money(BigDecimal.valueOf(machine.price()), "USD"), LocalDate.now().plusDays(30 + random.nextInt(90)))),
                        "equipment " + equipment.name).getId().uuid();
                gym.equipment.add(equipment);
                count("equipment");
            }
        }
    }

    // ── Sensors, usage, maintenance, analytics ───────────────────────────────

    private void seedSensors(SeededEquipment e) {
        soft("motion sensor", () -> motionSensorCommandService.handle(new RegisterMotionSensorCommand(e.id)));
        count("motionSensors");
        if (e.machine.zone().equals("Cardio") || e.machine.zone().equals("Funcional")) {
            soft("camera sensor", () -> cameraSensorCommandService.handle(new RegisterCameraSensorCommand(e.id)));
            count("cameraSensors");
        }
    }

    /** Same integration event monitoring publishes when a session ends; analytics builds the activity report from it. */
    private void seedUsage(SeededEquipment e) {
        int sessions = 8 + random.nextInt(e.machine.zone().equals("Cardio") ? 40 : 25);
        double meanMinutes = e.machine.zone().equals("Cardio") ? 26 : 15;
        for (int s = 0; s < sessions; s++) {
            long seconds = (long) Math.max(180, (meanMinutes + random.nextGaussian() * meanMinutes * 0.4) * 60);
            e.usageMinutes += seconds / 60.0;
            eventPublisher.publishEvent(new SessionTimeCalculatedAnalyticsIntegrationEvent(
                    UUID.randomUUID().toString(), e.id, LocalTime.ofSecondOfDay(seconds)));
        }
        stats.merge("usageSessions", sessions, Integer::sum);
    }

    private void seedMaintenance(SeededEquipment e, List<String> technicianIds) {
        double roll = random.nextDouble();
        if (roll < 0.30) {
            maintenanceTicket(e, TicketType.PREVENTIVE, TicketPriority.LOW, "Mantenimiento preventivo: limpieza, lubricación y calibración",
                    technicianIds, Stage.COMPLETED);
        } else if (roll < 0.45) {
            maintenanceTicket(e, TicketType.CORRECTIVE, e.machine.partPrice() > 150 ? TicketPriority.HIGH : TicketPriority.MEDIUM,
                    "Falla reportada: " + e.machine.partName().toLowerCase() + " dañado(a)", technicianIds, Stage.COMPLETED);
        } else if (roll < 0.52) {
            maintenanceTicket(e, TicketType.CORRECTIVE, TicketPriority.HIGH, "Equipo no enciende tras corte de luz", technicianIds, Stage.IN_PROGRESS);
        } else if (roll < 0.59) {
            maintenanceTicket(e, TicketType.PREVENTIVE, TicketPriority.MEDIUM, "Revisión programada mensual", technicianIds, Stage.OPEN);
        } else if (roll < 0.62) {
            soft("decommission", () -> equipmentCommandService.handle(new DecomissionEquipment(
                    new com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId(e.id), EquipmentStatus.DECOMMISSIONED)));
            e.touchedByMaintenance = true;
            e.downtimeMinutes += 7 * 16 * 60;
            count("decommissioned");
        }
    }

    private enum Stage { OPEN, IN_PROGRESS, COMPLETED }

    private void maintenanceTicket(SeededEquipment e, TicketType type, TicketPriority priority, String description,
                                   List<String> technicianIds, Stage stage) {
        e.touchedByMaintenance = true;
        var maintenance = require(maintenanceCommandService.handle(new RequestMaintenance(
                new com.spottrack.platform.maintenance.domain.model.valueobjects.EquipmentId(e.id), "SYSTEM", description)), "maintenance request");
        var ticket = require(maintenanceCommandService.handle(new CreateTechnicalTicketCommand(maintenance.getId().uuid(), priority, type)), "ticket");
        count("tickets" + stage);

        double labor = Math.round((type == TicketType.PREVENTIVE ? 40 + random.nextDouble() * 35 : 80 + random.nextDouble() * 160) * 100) / 100.0;
        if (type == TicketType.CORRECTIVE) {
            int quantity = 1 + random.nextInt(e.machine.partPrice() > 150 ? 1 : 3);
            maintenanceQuoteCommandService.handle(new RequestADetailedMaintenanceQuoteCommand(
                    e.id, labor, "USD", "CORRECTIVE", e.machine.partName(), quantity, e.machine.partPrice()))
                    .ifPresent(q -> maintenanceQuoteCommandService.handle(q.getId(),
                            new RequestSparePartsCommand(e.machine.partName(), quantity, e.machine.partPrice())));
            e.downtimeMinutes += 240 + random.nextInt(2400);
            count("quotes");
        } else {
            maintenanceQuoteCommandService.handle(new RequestADetailedMaintenanceQuoteCommand(e.id, labor, "USD", "PREVENTIVE", "n/a", 1, 0.0))
                    .ifPresent(q -> maintenanceQuoteCommandService.handle(q.getId(), new RequestPreventiveCostCommand(labor, "USD")));
            e.downtimeMinutes += 120;
            count("quotes");
        }

        if (stage == Stage.OPEN) return;
        var technicianId = technicianIds.get(random.nextInt(technicianIds.size()));
        require(maintenanceCommandService.handle(new AssignTechnicalTicket(ticket.getTicketId(), technicianId)), "ticket assignment");
        if (stage == Stage.IN_PROGRESS) return;

        require(maintenanceCommandService.handle(new RegisterMaintenanceCompletion(
                new TechnicalTicketId(ticket.getTicketId().uuid()), new MaintenanceId(ticket.getMaintenanceId()),
                type == TicketType.PREVENTIVE ? "Preventivo completado sin observaciones"
                        : "Reemplazo de " + e.machine.partName().toLowerCase() + " y prueba de funcionamiento",
                BigDecimal.valueOf(labor))), "maintenance completion");
        require(maintenanceCommandService.handle(new CompleteMaintenance(ticket.getTicketId())), "ticket resolution");
    }

    private void adjustActivityReport(SeededEquipment e) {
        var report = activityReportRepository.findByEquipmentId(e.id);
        if (report.isEmpty()) return;
        var reportId = report.get().getId();
        if (e.downtimeMinutes > 0) {
            activityReportCommandService.handle(reportId, new RequestDowntimeCostCommand(e.downtimeMinutes,
                    e.touchedByMaintenance ? "Fallas y mantenimiento" : "Sin incidencias"));
        }
        activityReportCommandService.handle(reportId, new RequestPercentageComparisonCommand(
                Math.round((random.nextGaussian() * 15) * 10) / 10.0));
    }

    private void seedRoiProjections(SeededGym gym, AdminSpec spec) {
        for (var branch : spec.branches()) {
            var units = gym.equipment.stream().filter(e -> e.branchName.equals(branch.name())).toList();
            double usageHours = units.stream().mapToDouble(e -> e.usageMinutes).sum() / 60.0;
            double earnings = Math.round(usageHours * 1.8 * 30 * 100) / 100.0;
            double investment = units.stream().mapToDouble(e -> e.machine.price()).sum();
            double roi = Math.round((earnings - investment * 0.02) / investment * 10000) / 100.0;
            var projection = roiProjectionCommandService.handle(new RequestRoiCommand(roi));
            if (projection.isEmpty()) continue;
            var id = projection.get().getId();
            roiProjectionCommandService.handle(id, new RequestDowntimeCostCommand(units.stream().mapToInt(e -> e.downtimeMinutes).sum(), branch.name()));
            roiProjectionCommandService.handle(id, new RequestEarningsCommand(earnings));
            if (branch.size() >= 1.0) {
                roiProjectionCommandService.handle(id, new DetectHighDemandSlotCommand(branch.name() + " 18:00-21:00", (int) (40 * branch.size())));
            } else {
                roiProjectionCommandService.handle(id, new DetectLowDemandSlotCommand(branch.name() + " 10:00-16:00", (int) (12 * branch.size())));
                units.stream().min((a, b) -> Double.compare(a.usageMinutes, b.usageMinutes)).ifPresent(idle ->
                        roiProjectionCommandService.handle(id, new RecommendTransferCommand("Trasladar " + idle.name, spec.branches().get(0).name())));
            }
            count("roiProjections");
        }
    }

    /** Anomaly reports raise CRITICAL alerts, sensor disconnections and past-due thresholds raise WARNING ones. */
    private void seedIncidents(SeededGym gym) {
        var candidates = new ArrayList<>(gym.equipment.stream().filter(e -> !e.touchedByMaintenance).toList());
        java.util.Collections.shuffle(candidates, random);
        int anomalies = Math.max(1, candidates.size() / 12);
        for (int i = 0; i < anomalies && i < candidates.size(); i++) {
            var e = candidates.get(i);
            e.touchedByMaintenance = true;
            soft("anomaly", () -> anomalyCommandService.handle(new ReportAnomalyCommand("SEED-MEGA-" + UUID.randomUUID(), e.id, e.zoneId,
                    List.of("Vibración inusual detectada durante el uso", "Ruido metálico en el mecanismo",
                            "Pantalla no responde", "Olor a quemado en el motor").get(random.nextInt(4)))));
            count("anomalies");
        }
        int disconnections = Math.max(1, candidates.size() / 15);
        for (int i = anomalies; i < anomalies + disconnections && i < candidates.size(); i++) {
            var e = candidates.get(i);
            e.touchedByMaintenance = true;
            var sensor = motionSensorCommandService.handle(new RegisterMotionSensorCommand(e.id));
            // Registration fails when the sensor already exists; the repository lookup isn't needed then.
            if (sensor instanceof Result.Success<?, ?> success) {
                var motionSensor = (com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor) success.value();
                soft("disconnect", () -> motionSensorCommandService.handle(new MarkMotionSensorDisconnectedCommand(motionSensor.getId())));
            }
            count("sensorDisconnections");
        }
        for (int i = anomalies + disconnections; i < anomalies + disconnections + 2 && i < candidates.size(); i++) {
            var e = candidates.get(i);
            soft("threshold", () -> equipmentCommandService.handle(new DefineMaintenanceThresholdCommand(
                    new com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId(e.id), LocalDate.now().minusDays(1 + random.nextInt(5)))));
            count("pastDueThresholds");
        }
    }

    private void resolveSomeAlerts(SeededGym gym) {
        for (var alert : alertQueryService.handle(new GetAlertsByAdminUserIdQuery(gym.adminUserId))) {
            if (!alert.isResolved() && random.nextDouble() < 0.4) {
                soft("resolve alert", () -> alertCommandService.handle(new ResolveAlertCommand(alert.getId(), gym.adminUserId)));
                count("resolvedAlerts");
            }
        }
    }

    private List<String> createTechnicians() {
        var ids = new ArrayList<String>();
        for (var name : List.of("Jorge Huamán", "Rosa Quispe", "Diego Salazar", "Ana Torres",
                "Pedro Mamani", "Lourdes Condori", "Iván Navarro", "Gisela Pinto")) {
            ids.add(require(maintenanceCommandService.handle(new CreateTechnician(name)), "technician " + name).getTechnicianId().uuid());
            count("technicians");
        }
        return ids;
    }

    // ── Clients, reservations, routines ──────────────────────────────────────

    private void seedClients(SeededGym gym, AdminSpec spec) {
        for (int i = 0; i < spec.clients(); i++) {
            var first = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
            var last = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
            var email = gym.clientIds.isEmpty() && spec.email().equals(MARKER_EMAIL) ? "cliente.mega@spottrack.com" : uniqueEmail(first, last);
            gym.clientIds.add(seedClient(gym.gymId, email, first, last));
        }
        // DNIs the gym already registered whose owners haven't signed up yet.
        for (int i = 0; i < 4; i++) {
            whitelist(gym.gymId, nextDni());
            count("pendingWhitelistDnis");
        }
    }

    private Long seedClient(String gymId, String email, String firstName, String lastName) {
        var dni = nextDni();
        whitelist(gymId, dni);
        var userId = signUp(email, Roles.ROLE_CLIENT);
        var client = clientQueryService.handle(new GetClientByUserIdQuery(userId))
                .orElseThrow(() -> new IllegalStateException("Client profile not found after sign-up: " + email));
        clientCommandService.handle(new UpdateClientProfileCommand(new com.spottrack.platform.profiles.domain.model.valueobjects.ClientId(client.getId()),
                firstName, lastName, new PhoneNumber("9" + (10000000 + random.nextInt(89999999))),
                new com.spottrack.platform.profiles.domain.model.valueobjects.Dni(dni)));
        soft("associate " + email, () -> clientCommandService.handle(new AssociateClientWithGymCommand(client.getId(), gymId)));
        count("clients");
        return client.getId();
    }

    /** Some members train at two chains, so the gym switcher has something to switch between. */
    private void seedCrossGymClients(List<SeededGym> gyms) {
        for (int g = 0; g < gyms.size(); g++) {
            var from = gyms.get(g);
            var to = gyms.get((g + 1) % gyms.size());
            for (int i = 0; i < Math.min(3, from.clientIds.size()); i++) {
                var clientId = from.clientIds.get(i);
                var client = clientQueryService.handle(new com.spottrack.platform.profiles.domain.model.queries.GetClientByIdQuery(
                        new com.spottrack.platform.profiles.domain.model.valueobjects.ClientId(clientId)));
                if (client.isEmpty()) continue;
                whitelist(to.gymId, client.get().getPersonInfo().dni().getDNI());
                soft("cross-gym association", () -> clientCommandService.handle(new AssociateClientWithGymCommand(clientId, to.gymId)));
                count("crossGymMembers");
            }
        }
    }

    /**
     * Historical reservations (ended or cancelled) for most members, then one live reservation for a
     * handful of them. Only equipment no maintenance or incident ever touched is used, so it's AVAILABLE.
     */
    private void seedReservations(SeededGym gym) {
        var reservable = gym.equipment.stream().filter(e -> !e.touchedByMaintenance).toList();
        if (reservable.isEmpty()) return;
        for (var clientId : gym.clientIds) {
            int history = random.nextInt(5);
            for (int r = 0; r < history; r++) {
                var e = reservable.get(random.nextInt(reservable.size()));
                var reservation = reserve(clientId, e.id);
                if (reservation == null) continue;
                if (random.nextDouble() < 0.8) {
                    soft("end reservation", () -> reservationCommandService.handle(new EndReservation(reservation.getId())));
                    count("reservationsEnded");
                } else {
                    soft("cancel reservation", () -> reservationCommandService.handle(new CancelReservation(reservation.getId())));
                    count("reservationsCancelled");
                }
            }
        }
        int live = Math.min(Math.max(1, gym.clientIds.size() / 5), reservable.size());
        for (int i = 0; i < live; i++) {
            var reservation = reserve(gym.clientIds.get(i), reservable.get(i).id);
            if (reservation == null) continue;
            soft("start timer", () -> reservationCommandService.handle(new StartReservationTimer(reservation.getId(), 30 + 15 * random.nextInt(3))));
            count("reservationsActive");
        }
    }

    private com.spottrack.platform.reservation.domain.model.aggregates.Reservation reserve(Long clientId, String equipmentId) {
        int startHour = 6 + random.nextInt(15);
        var result = reservationCommandService.handle(new InitiateExpressReservation(
                new com.spottrack.platform.reservation.domain.model.valueobjects.ClientId(clientId),
                new com.spottrack.platform.reservation.domain.model.valueobjects.EquipmentId(equipmentId),
                new TimeInterval(Time.valueOf(LocalTime.of(startHour, 0)), Time.valueOf(LocalTime.of(startHour + 1, 0)))));
        if (result instanceof Result.Success<com.spottrack.platform.reservation.domain.model.aggregates.Reservation, ?> success) {
            return success.value();
        }
        log.debug("[MegaDataSeeder] Reservation skipped: {}", ((Result.Failure<?, ?>) result).error());
        return null;
    }

    private void seedRoutines(Long clientId) {
        if (random.nextDouble() < 0.35) return;
        var routineClientId = new com.spottrack.platform.routine.domain.model.valueobjects.ClientId(clientId);
        int routines = 1 + random.nextInt(2);
        for (int r = 0; r < routines; r++) {
            var template = ROUTINES.get(random.nextInt(ROUTINES.size()));
            var routineResult = routineCommandService.handle(new CreateRoutineCommand(new RoutineName(template.name()), routineClientId));
            if (!(routineResult instanceof Result.Success<com.spottrack.platform.routine.domain.model.aggregates.Routine, ?> created)) continue;
            var routineId = created.value().getId();
            var blockIds = new ArrayList<Long>();
            for (int b = 0; b < template.blocks().size(); b++) {
                var block = template.blocks().get(b);
                var blockResult = routineCommandService.handle(new AddExerciseBlockCommand(routineId, new ExerciseName((String) block[0]),
                        (ExerciseType) block[1], b + 1, (int) block[2], (int) block[3]));
                if (blockResult instanceof Result.Success<com.spottrack.platform.routine.domain.model.entities.ExerciseBlock, ?> added) {
                    blockIds.add(added.value().getId());
                }
            }
            count("routines");

            int sessions = 2 + random.nextInt(6);
            for (int s = 0; s < sessions; s++) {
                var sessionResult = routineSessionCommandService.handle(new StartRoutineCommand(routineId, routineClientId));
                if (!(sessionResult instanceof Result.Success<com.spottrack.platform.routine.domain.model.aggregates.RoutineSession, ?> started)) break;
                var sessionId = started.value().getId();
                boolean last = s == sessions - 1;
                if (last && random.nextDouble() < 0.4) {
                    // Left in progress, half the blocks ticked off.
                    for (int b = 0; b < blockIds.size() / 2; b++) {
                        var blockId = blockIds.get(b);
                        soft("block completion", () -> routineSessionCommandService.handle(new SetExerciseBlockCompletionCommand(sessionId, blockId, true)));
                    }
                    count("routineSessionsInProgress");
                } else if (random.nextDouble() < 0.2) {
                    soft("missed", () -> routineSessionCommandService.handle(new MarkRoutineMissedCommand(sessionId)));
                    count("routineSessionsMissed");
                } else {
                    for (var blockId : blockIds) {
                        soft("block completion", () -> routineSessionCommandService.handle(new SetExerciseBlockCompletionCommand(sessionId, blockId, true)));
                    }
                    soft("complete routine", () -> routineSessionCommandService.handle(new CompleteRoutineCommand(sessionId)));
                    count("routineSessionsCompleted");
                }
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Long signUp(String email, Roles roleName) {
        var role = roleRepository.findByName(roleName).orElseGet(() -> new Role(roleName));
        return require(userCommandService.handle(new SignUpCommand(email, PASSWORD, List.of(role))), "sign-up " + email).getId();
    }

    private void whitelist(String gymId, String dni) {
        gymCommandService.handle(new AddDniToWhitelistCommand(gymId, new com.spottrack.platform.gym.domain.model.valueobjects.Dni(dni)));
    }

    private String nextDni() {
        return String.valueOf(dniCounter++);
    }

    private String uniqueEmail(String first, String last) {
        var base = (stripAccents(first) + "." + stripAccents(last)).toLowerCase();
        var email = base + "@mail.com";
        for (int n = 2; usedEmails.contains(email) || userRepository.existsByUsername(email); n++) {
            email = base + n + "@mail.com";
        }
        usedEmails.add(email);
        return email;
    }

    private static String stripAccents(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private void count(String key) {
        stats.merge(key, 1, Integer::sum);
    }

    /** For steps whose failure shouldn't abort the whole seed (duplicates, state-machine guards). */
    private void soft(String step, Supplier<?> action) {
        try {
            var result = action.get();
            if (result instanceof Result.Failure<?, ?> failure) {
                log.debug("[MegaDataSeeder] Step '{}' skipped: {}", step, failure.error());
            }
        } catch (RuntimeException ex) {
            log.warn("[MegaDataSeeder] Step '{}' failed: {}", step, ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T require(Result<T, ?> result, String step) {
        if (result instanceof Result.Success<T, ?> success) {
            return success.value();
        }
        throw new IllegalStateException("[MegaDataSeeder] Failed at " + step + ": " + ((Result.Failure<T, ?>) result).error());
    }
}
