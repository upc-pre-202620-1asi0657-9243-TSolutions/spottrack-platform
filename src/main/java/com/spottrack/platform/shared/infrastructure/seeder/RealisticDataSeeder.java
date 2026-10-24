package com.spottrack.platform.shared.infrastructure.seeder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import com.spottrack.platform.analytics.domain.model.commands.RequestRoiCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestSparePartsCommand;
import com.spottrack.platform.analytics.domain.repositories.ActivityReportRepository;
import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.application.commandServices.GymCommandService;
import com.spottrack.platform.gym.domain.model.commands.AddBranchCommand;
import com.spottrack.platform.gym.domain.model.commands.AddZoneCommand;
import com.spottrack.platform.gym.domain.model.commands.CreateGym;
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
import com.spottrack.platform.maintenance.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicalTicketId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketPriority;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketType;
import com.spottrack.platform.membership.application.commandservices.MembershipCommandService;
import com.spottrack.platform.membership.domain.model.commands.ActivateMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.CreateMembershipCommand;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipTier;
import com.spottrack.platform.monitoring.interfaces.events.SessionTimeCalculatedAnalyticsIntegrationEvent;
import com.spottrack.platform.profiles.application.commandservices.AdminCommandService;
import com.spottrack.platform.profiles.application.queryservices.AdminQueryService;
import com.spottrack.platform.profiles.domain.model.commands.UpdateAdminProfileCommand;
import com.spottrack.platform.profiles.domain.model.queries.GetAdminByUserIdQuery;
import com.spottrack.platform.profiles.domain.model.valueobjects.AdminId;
import com.spottrack.platform.profiles.domain.model.valueobjects.PhoneNumber;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Opt-in seeder (profile "seed-realistic") that simulates 90 days of real gym operation for a
 * separate admin account, so the analytics and financial-impact views can be checked against a
 * known ground truth.
 *
 * The simulation runs first, fully in memory and with a fixed random seed, and produces the
 * ground truth. Only then is it written through the real domain paths: usage arrives as
 * SessionTimeCalculatedAnalyticsIntegrationEvent (the same event monitoring publishes when a
 * session ends), maintenance goes through tickets, assignments and completion logs, and quotes
 * and ROI projections through their command services. The ground truth is written to
 * target/realistic-seed-ground-truth.json for comparison with what the API and UI report.
 *
 * Run with: SPRING_PROFILES_ACTIVE=dev,seed-realistic
 * Log in as analytics@spottrack.com / seed123.
 */
@Component
@Profile("seed-realistic")
@Slf4j
public class RealisticDataSeeder {

    private static final String ADMIN_EMAIL = "analytics@spottrack.com";
    private static final String ADMIN_PASSWORD = "seed123";
    private static final String ADMIN_DNI = "45678912";
    private static final String GYM_NAME = "SpotTrack Lima Fitness";
    private static final String MANUFACTURER_ID = "00000000-0000-0000-0000-000000000001";

    private static final long RANDOM_SEED = 20261024L;
    private static final int SIM_DAYS = 90;
    /** Opening hours 06:00-22:00. */
    private static final int OPEN_MINUTES_PER_DAY = 16 * 60;
    private static final int PREVENTIVE_DOWNTIME_MINUTES = 120;
    /** Revenue attributable to one hour of equipment use (membership revenue / total equipment-hours). */
    private static final double REVENUE_PER_USAGE_HOUR = 1.80;

    private record Branch(String code, String name, String address, double traffic, double trendOver90Days) {}

    private record Model(String name, String zone, String model, double price, double baseUtilization,
                         double failuresPer1000Hours, String partName, double partPrice) {}

    private record Placement(Model model, int count) {}

    private static final List<Branch> BRANCHES = List.of(
            new Branch("MF", "Sede Miraflores", "Av. Larco 345, Miraflores", 1.15, 0.06),
            new Branch("SI", "Sede San Isidro", "Av. Camino Real 1101, San Isidro", 1.00, 0.00),
            new Branch("SU", "Sede Surco", "Av. Primavera 890, Surco", 0.65, 0.18));

    private static final List<String> ZONES = List.of("Cardio", "Fuerza", "Funcional");
    private static final Map<String, Integer> ZONE_CAPACITY = Map.of("Cardio", 30, "Fuerza", 25, "Funcional", 15);

    private static final Model TREADMILL = new Model("Cinta de Correr", "Cardio", "Life Fitness T5", 4800, 0.62, 1.6, "Banda de rodamiento", 180);
    private static final Model ELLIPTICAL = new Model("Elíptica", "Cardio", "Precor EFX 835", 3200, 0.45, 1.0, "Rodamiento de pedal", 65);
    private static final Model BIKE = new Model("Bicicleta Estática", "Cardio", "Keiser M3i", 1900, 0.38, 0.8, "Correa de transmisión", 45);
    private static final Model ROWER = new Model("Remo", "Cardio", "Concept2 RowErg", 1600, 0.22, 0.6, "Cadena", 55);
    private static final Model STAIR = new Model("Escaladora", "Cardio", "StairMaster 8Gx", 5500, 0.30, 1.4, "Motor de escalones", 420);
    private static final Model LEG_PRESS = new Model("Prensa de Piernas", "Fuerza", "Hammer Strength PL", 2800, 0.35, 0.5, "Rodamiento lineal", 90);
    private static final Model SQUAT_RACK = new Model("Rack de Sentadillas", "Fuerza", "Rogue RML-490", 1500, 0.50, 0.3, "Seguro de barra", 30);
    private static final Model BENCH = new Model("Banco Press", "Fuerza", "Hammer Strength Flat", 900, 0.48, 0.3, "Tapizado", 70);
    private static final Model CABLE = new Model("Polea Cruzada", "Fuerza", "Matrix Aura G3", 3900, 0.40, 0.9, "Cable de acero", 60);
    private static final Model SMITH = new Model("Máquina Smith", "Fuerza", "Life Fitness Signature", 2400, 0.33, 0.4, "Rodamiento lineal", 90);
    private static final Model RIG = new Model("Multiestación Funcional", "Funcional", "Technogym Omnia", 6000, 0.25, 0.3, "Anclaje de suspensión", 85);
    private static final Model AIR_BIKE = new Model("Assault Bike", "Funcional", "Rogue Echo", 1200, 0.28, 0.7, "Ventilador", 75);

    private static final Map<String, List<Placement>> LAYOUT = Map.of(
            "MF", List.of(new Placement(TREADMILL, 4), new Placement(ELLIPTICAL, 3), new Placement(BIKE, 3),
                    new Placement(ROWER, 2), new Placement(STAIR, 1), new Placement(LEG_PRESS, 2),
                    new Placement(SQUAT_RACK, 3), new Placement(BENCH, 3), new Placement(CABLE, 2),
                    new Placement(SMITH, 1), new Placement(RIG, 1), new Placement(AIR_BIKE, 2)),
            "SI", List.of(new Placement(TREADMILL, 3), new Placement(ELLIPTICAL, 2), new Placement(BIKE, 3),
                    new Placement(ROWER, 1), new Placement(STAIR, 1), new Placement(LEG_PRESS, 2),
                    new Placement(SQUAT_RACK, 2), new Placement(BENCH, 2), new Placement(CABLE, 2),
                    new Placement(SMITH, 1), new Placement(RIG, 1), new Placement(AIR_BIKE, 2)),
            "SU", List.of(new Placement(TREADMILL, 2), new Placement(ELLIPTICAL, 2), new Placement(BIKE, 2),
                    new Placement(ROWER, 2), new Placement(STAIR, 1), new Placement(LEG_PRESS, 1),
                    new Placement(SQUAT_RACK, 1), new Placement(BENCH, 2), new Placement(CABLE, 1),
                    new Placement(RIG, 1)));

    /** Equipment that breaks far more often than its model should (a "lemon"). */
    private static final String LEMON_CODE = "SI-Cinta de Correr-02";

    private static final double[] WEEKDAY_FACTOR = {1.05, 1.00, 1.00, 0.98, 0.90, 0.75, 0.55}; // Mon..Sun

    // ── Simulation state ─────────────────────────────────────────────────────

    private static final class SimEquipment {
        final String code;
        final String displayName;
        final Branch branch;
        final Model model;
        final double utilizationFactor;
        final double failureFactor;
        final List<Double> sessionSeconds = new ArrayList<>();
        final double[] dailyUsageMinutes = new double[SIM_DAYS];
        double downtimeMinutes;
        double laborCost;
        double preventiveCost;
        double sparePartsCost;
        int correctiveCount;
        int preventiveCount;
        final List<MaintenanceEvent> events = new ArrayList<>();
        String equipmentId;

        SimEquipment(String code, String displayName, Branch branch, Model model, double utilizationFactor, double failureFactor) {
            this.code = code;
            this.displayName = displayName;
            this.branch = branch;
            this.model = model;
            this.utilizationFactor = utilizationFactor;
            this.failureFactor = failureFactor;
        }

        double usageMinutes() {
            double total = 0;
            for (double m : dailyUsageMinutes) total += m;
            return total;
        }

        double availableMinutes() {
            return SIM_DAYS * OPEN_MINUTES_PER_DAY - downtimeMinutes;
        }

        double percentageChange() {
            double last = 0, previous = 0;
            for (int d = SIM_DAYS - 30; d < SIM_DAYS; d++) last += dailyUsageMinutes[d];
            for (int d = SIM_DAYS - 60; d < SIM_DAYS - 30; d++) previous += dailyUsageMinutes[d];
            if (previous == 0) return 0.0;
            return Math.round((last - previous) / previous * 1000.0) / 10.0;
        }

        boolean hasOpenTicket() {
            return events.stream().anyMatch(e -> e.open);
        }
    }

    private record MaintenanceEvent(TicketType type, int day, String description, double laborCost,
                                    int partQuantity, double partUnitPrice, boolean open, boolean assigned) {}

    private final RoleCommandService roleCommandService;
    private final UserCommandService userCommandService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AdminQueryService adminQueryService;
    private final AdminCommandService adminCommandService;
    private final MembershipCommandService membershipCommandService;
    private final GymCommandService gymCommandService;
    private final EquipmentCommandService equipmentCommandService;
    private final MaintenanceCommandService maintenanceCommandService;
    private final MaintenanceQuoteCommandService maintenanceQuoteCommandService;
    private final ActivityReportCommandService activityReportCommandService;
    private final ActivityReportRepository activityReportRepository;
    private final ROIProjectionCommandService roiProjectionCommandService;
    private final ApplicationEventPublisher eventPublisher;

    public RealisticDataSeeder(RoleCommandService roleCommandService,
                               UserCommandService userCommandService,
                               UserRepository userRepository,
                               RoleRepository roleRepository,
                               AdminQueryService adminQueryService,
                               AdminCommandService adminCommandService,
                               MembershipCommandService membershipCommandService,
                               GymCommandService gymCommandService,
                               EquipmentCommandService equipmentCommandService,
                               MaintenanceCommandService maintenanceCommandService,
                               MaintenanceQuoteCommandService maintenanceQuoteCommandService,
                               ActivityReportCommandService activityReportCommandService,
                               ActivityReportRepository activityReportRepository,
                               ROIProjectionCommandService roiProjectionCommandService,
                               ApplicationEventPublisher eventPublisher) {
        this.roleCommandService = roleCommandService;
        this.userCommandService = userCommandService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.adminQueryService = adminQueryService;
        this.adminCommandService = adminCommandService;
        this.membershipCommandService = membershipCommandService;
        this.gymCommandService = gymCommandService;
        this.equipmentCommandService = equipmentCommandService;
        this.maintenanceCommandService = maintenanceCommandService;
        this.maintenanceQuoteCommandService = maintenanceQuoteCommandService;
        this.activityReportCommandService = activityReportCommandService;
        this.activityReportRepository = activityReportRepository;
        this.roiProjectionCommandService = roiProjectionCommandService;
        this.eventPublisher = eventPublisher;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(20)
    public void seed(ApplicationReadyEvent event) {
        if (userRepository.existsByUsername(ADMIN_EMAIL)) {
            log.info("[RealisticDataSeeder] {} already exists, skipping. Drop that user's data to re-seed.", ADMIN_EMAIL);
            return;
        }
        long started = System.currentTimeMillis();
        log.info("[RealisticDataSeeder] Simulating {} days of operation...", SIM_DAYS);
        var fleet = simulate();

        roleCommandService.handle(new SeedRolesCommand());
        var adminUserId = createAdmin();
        activatePlatinumMembership(adminUserId);
        var zoneIds = createGymBranchesAndZones(adminUserId);
        var technicianIds = createTechnicians();

        int sessions = 0;
        for (var equipment : fleet) {
            equipment.equipmentId = registerEquipment(equipment, zoneIds);
            sessions += publishUsage(equipment);
            persistMaintenance(equipment, technicianIds);
            persistReportAdjustments(equipment);
        }
        persistRoiProjections(fleet);
        writeGroundTruth(fleet);

        log.info("[RealisticDataSeeder] Done: {} equipment, {} sessions, {} maintenance tickets in {}s. Login: {} / {}",
                fleet.size(), sessions, fleet.stream().mapToInt(e -> e.events.size()).sum(),
                (System.currentTimeMillis() - started) / 1000, ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    // ── Phase 1: pure simulation (the ground truth) ──────────────────────────

    private List<SimEquipment> simulate() {
        var random = new Random(RANDOM_SEED);
        var fleet = new ArrayList<SimEquipment>();
        for (var branch : BRANCHES) {
            for (var placement : LAYOUT.get(branch.code())) {
                for (int i = 1; i <= placement.count(); i++) {
                    var model = placement.model();
                    var code = "%s-%s-%02d".formatted(branch.code(), model.name(), i);
                    var displayName = "%s %s-%02d".formatted(model.name(), branch.code(), i);
                    // Units of the same model are not used equally: the one by the door gets more traffic.
                    double utilizationFactor = 0.75 + random.nextDouble() * 0.5;
                    double failureFactor = code.equals(LEMON_CODE) ? 6.0 : 0.6 + random.nextDouble() * 0.8;
                    fleet.add(new SimEquipment(code, displayName, branch, model, utilizationFactor, failureFactor));
                }
            }
        }
        // One machine nobody wants: a rower tucked in a corner of the quietest branch.
        var idle = fleet.stream().filter(e -> e.code.equals("SU-Remo-02")).findFirst().orElseThrow();
        fleet.set(fleet.indexOf(idle), new SimEquipment(idle.code, idle.displayName, idle.branch, idle.model, 0.12, idle.failureFactor));

        var startDate = LocalDate.now().minusDays(SIM_DAYS);
        for (var equipment : fleet) {
            simulateEquipment(equipment, startDate, random);
        }
        return fleet;
    }

    private void simulateEquipment(SimEquipment e, LocalDate startDate, Random random) {
        int nextPreventiveDay = 20 + random.nextInt(15);
        int downUntilDay = -1;          // corrective repair blocks the machine until (and including) this day
        double carriedDowntimeToday = 0; // minutes lost on the day a failure happens

        for (int day = 0; day < SIM_DAYS; day++) {
            DayOfWeek dow = startDate.plusDays(day).getDayOfWeek();
            double available = OPEN_MINUTES_PER_DAY;

            if (day <= downUntilDay) {
                double lost = day == downUntilDay && carriedDowntimeToday > 0 ? carriedDowntimeToday : OPEN_MINUTES_PER_DAY;
                e.downtimeMinutes += lost;
                available -= lost;
                if (day == downUntilDay) carriedDowntimeToday = 0;
            }

            if (day == nextPreventiveDay && day > downUntilDay) {
                boolean open = day >= SIM_DAYS - 2;
                double labor = round2(40 + random.nextDouble() * 35);
                e.events.add(new MaintenanceEvent(TicketType.PREVENTIVE, day,
                        "Mantenimiento preventivo: limpieza, lubricación y calibración", labor, 0, 0, open, false));
                e.preventiveCount++;
                if (!open) e.preventiveCost += labor;
                e.downtimeMinutes += PREVENTIVE_DOWNTIME_MINUTES;
                available -= PREVENTIVE_DOWNTIME_MINUTES;
                nextPreventiveDay = day + 28 + random.nextInt(8);
            } else if (day == nextPreventiveDay) {
                nextPreventiveDay = downUntilDay + 1;
            }

            double trend = 1 + e.branch.trendOver90Days() * day / SIM_DAYS;
            double noise = 1 + random.nextGaussian() * 0.08;
            double utilization = e.model.baseUtilization() * e.utilizationFactor * e.branch.traffic()
                    * WEEKDAY_FACTOR[dow.getValue() - 1] * trend * noise;
            utilization = Math.max(0, Math.min(0.92, utilization));
            double usage = utilization * Math.max(0, available);

            // Wear drives failures: the more hours of use, the likelier a breakdown.
            if (day > downUntilDay && usage > 0) {
                double failureProbability = e.model.failuresPer1000Hours() * e.failureFactor * (usage / 60.0) / 1000.0;
                if (random.nextDouble() < failureProbability) {
                    double failsAtFraction = 0.2 + random.nextDouble() * 0.7;
                    usage *= failsAtFraction;
                    double lostToday = available * (1 - failsAtFraction);
                    e.downtimeMinutes += lostToday;
                    int repairDays = random.nextDouble() < 0.4 ? 0 : 1 + random.nextInt(3);
                    if (repairDays > 0) {
                        downUntilDay = day + repairDays;
                        // The last repair day the machine comes back at midday.
                        carriedDowntimeToday = OPEN_MINUTES_PER_DAY / 2.0;
                    }
                    boolean open = downUntilDay >= SIM_DAYS - 1 || (repairDays == 0 && day >= SIM_DAYS - 1);
                    int quantity = 1 + random.nextInt(e.model.partPrice() > 150 ? 1 : 3);
                    double labor = round2((80 + random.nextDouble() * 140) * (1 + repairDays * 0.3));
                    e.events.add(new MaintenanceEvent(TicketType.CORRECTIVE, day,
                            "Falla reportada: " + e.model.partName().toLowerCase() + " dañado(a)", labor,
                            quantity, e.model.partPrice(), open, random.nextBoolean()));
                    e.correctiveCount++;
                    e.sparePartsCost += quantity * e.model.partPrice();
                    if (!open) e.laborCost += labor;
                }
            }

            e.dailyUsageMinutes[day] = usage;
            splitIntoSessions(e, usage, random);
        }
    }

    /** People use a machine in sessions of a few minutes up to ~an hour. */
    private void splitIntoSessions(SimEquipment e, double usageMinutes, Random random) {
        double remainingSeconds = usageMinutes * 60;
        double meanMinutes = e.model.zone().equals("Cardio") ? 28 : 17;
        while (remainingSeconds > 0) {
            double sessionSeconds = Math.max(4 * 60, (meanMinutes + random.nextGaussian() * meanMinutes * 0.4) * 60);
            sessionSeconds = Math.min(sessionSeconds, remainingSeconds);
            e.sessionSeconds.add(Math.floor(sessionSeconds));
            remainingSeconds -= sessionSeconds;
        }
    }

    // ── Phase 2: write the simulation through the real domain paths ──────────

    private Long createAdmin() {
        var adminRole = roleRepository.findByName(Roles.ROLE_ADMIN).orElseGet(() -> new Role(Roles.ROLE_ADMIN));
        var result = userCommandService.handle(new SignUpCommand(ADMIN_EMAIL, ADMIN_PASSWORD, List.of(adminRole)));
        var user = require(result, "admin sign-up");
        var admin = adminQueryService.handle(new GetAdminByUserIdQuery(user.getId()))
                .orElseThrow(() -> new IllegalStateException("Admin profile not found after sign-up"));
        adminCommandService.handle(new UpdateAdminProfileCommand(new AdminId(admin.getId()), "Lucía", "Ramírez",
                new PhoneNumber("987654321"), new com.spottrack.platform.profiles.domain.model.valueobjects.Dni(ADMIN_DNI)));
        return user.getId();
    }

    private void activatePlatinumMembership(Long adminUserId) {
        var today = LocalDate.now();
        var membership = require(membershipCommandService.handle(new CreateMembershipCommand(
                adminUserId, MembershipTier.PLATINUM, MembershipTier.PLATINUM.toMoney(), today.minusDays(SIM_DAYS), today.plusDays(30))),
                "membership creation");
        require(membershipCommandService.handle(new ActivateMembershipCommand(membership.getMembershipId())), "membership activation");
    }

    /** Returns zone ids keyed by "branchCode/zoneName". */
    private Map<String, String> createGymBranchesAndZones(Long adminUserId) {
        var gym = require(gymCommandService.handle(new CreateGym(GYM_NAME, adminUserId)), "gym creation");
        var zoneIds = new LinkedHashMap<String, String>();
        for (var branch : BRANCHES) {
            var created = require(gymCommandService.handle(new AddBranchCommand(gym.getId().uuid(), branch.name(), branch.address())),
                    "branch " + branch.name());
            for (var zone : ZONES) {
                var createdZone = require(gymCommandService.handle(new AddZoneCommand(
                        zone, ZONE_CAPACITY.get(zone), new BranchId(created.getId().uuid()))), "zone " + zone);
                zoneIds.put(branch.code() + "/" + zone, createdZone.getId().uuid());
            }
        }
        return zoneIds;
    }

    private List<String> createTechnicians() {
        var ids = new ArrayList<String>();
        for (var name : List.of("Jorge Huamán", "Rosa Quispe", "Diego Salazar", "Ana Torres")) {
            ids.add(require(maintenanceCommandService.handle(new CreateTechnician(name)), "technician " + name)
                    .getTechnicianId().uuid());
        }
        return ids;
    }

    private String registerEquipment(SimEquipment e, Map<String, String> zoneIds) {
        var status = e.hasOpenTicket() && e.events.stream().anyMatch(m -> m.open && m.type == TicketType.CORRECTIVE)
                ? EquipmentStatus.MAINTENANCE : EquipmentStatus.AVAILABLE;
        var equipment = require(equipmentCommandService.handle(new RegisterEquipment(
                e.displayName, status, e.model.model(), new ManufacturerId(MANUFACTURER_ID),
                new ZoneId(zoneIds.get(e.branch.code() + "/" + e.model.zone())),
                new Money(BigDecimal.valueOf(e.model.price()), "USD"),
                LocalDate.now().plusDays(30))), "equipment " + e.displayName);
        return equipment.getId().uuid();
    }

    /** Each session goes through the same integration event monitoring publishes when a session ends. */
    private int publishUsage(SimEquipment e) {
        for (double seconds : e.sessionSeconds) {
            eventPublisher.publishEvent(new SessionTimeCalculatedAnalyticsIntegrationEvent(
                    UUID.randomUUID().toString(), e.equipmentId, LocalTime.ofSecondOfDay((long) seconds)));
        }
        return e.sessionSeconds.size();
    }

    private void persistMaintenance(SimEquipment e, List<String> technicianIds) {
        var random = new Random(e.code.hashCode());
        for (var event : e.events) {
            var maintenance = require(maintenanceCommandService.handle(new RequestMaintenance(
                    new EquipmentId(e.equipmentId), "SYSTEM", event.description())), "maintenance request");
            var priority = event.type() == TicketType.PREVENTIVE ? TicketPriority.LOW
                    : event.partUnitPrice() > 150 ? TicketPriority.HIGH : TicketPriority.MEDIUM;
            var ticket = require(maintenanceCommandService.handle(new CreateTechnicalTicketCommand(
                    maintenance.getId().uuid(), priority, event.type())), "ticket");

            if (event.type() == TicketType.CORRECTIVE) {
                var quote = maintenanceQuoteCommandService.handle(new RequestADetailedMaintenanceQuoteCommand(
                        e.equipmentId, event.laborCost(), "USD", "CORRECTIVE", e.model.partName(),
                        event.partQuantity(), event.partUnitPrice()));
                quote.ifPresent(q -> maintenanceQuoteCommandService.handle(q.getId(),
                        new RequestSparePartsCommand(e.model.partName(), event.partQuantity(), event.partUnitPrice())));
            }

            if (event.open() && !event.assigned()) continue;
            var technicianId = technicianIds.get(random.nextInt(technicianIds.size()));
            require(maintenanceCommandService.handle(new AssignTechnicalTicket(ticket.getTicketId(), technicianId)), "ticket assignment");
            if (event.open()) continue;

            require(maintenanceCommandService.handle(new RegisterMaintenanceCompletion(
                    new TechnicalTicketId(ticket.getTicketId().uuid()), new MaintenanceId(ticket.getMaintenanceId()),
                    event.type() == TicketType.PREVENTIVE ? "Preventivo completado sin observaciones"
                            : "Reemplazo de " + e.model.partName().toLowerCase() + " (x" + event.partQuantity() + ") y prueba de funcionamiento",
                    BigDecimal.valueOf(event.laborCost()).setScale(2, RoundingMode.HALF_UP))), "maintenance completion");
            require(maintenanceCommandService.handle(new CompleteMaintenance(ticket.getTicketId())), "ticket resolution");
        }
    }

    /** Usage accumulates through the events; downtime and the 30-vs-30-day trend are set on the same report. */
    private void persistReportAdjustments(SimEquipment e) {
        var report = activityReportRepository.findByEquipmentId(e.equipmentId);
        if (report.isEmpty()) {
            log.warn("[RealisticDataSeeder] No activity report was created for {} (no usage?)", e.displayName);
            return;
        }
        var reportId = report.get().getId();
        activityReportCommandService.handle(reportId, new RequestDowntimeCostCommand(
                (int) Math.round(e.downtimeMinutes), e.correctiveCount > 0 ? "Fallas y mantenimiento" : "Mantenimiento preventivo"));
        activityReportCommandService.handle(reportId, new RequestPercentageComparisonCommand(e.percentageChange()));
    }

    private void persistRoiProjections(List<SimEquipment> fleet) {
        for (var branch : BRANCHES) {
            var units = fleet.stream().filter(e -> e.branch == branch).toList();
            double usageHours = units.stream().mapToDouble(SimEquipment::usageMinutes).sum() / 60.0;
            double downtimeMinutes = units.stream().mapToDouble(e -> e.downtimeMinutes).sum();
            double earnings = usageHours * REVENUE_PER_USAGE_HOUR;
            double maintenance = units.stream().mapToDouble(e -> e.laborCost + e.preventiveCost + e.sparePartsCost).sum();
            double investment = units.stream().mapToDouble(e -> e.model.price()).sum();
            double roi = round2((earnings - maintenance) / investment * 100);

            var projection = roiProjectionCommandService.handle(new RequestRoiCommand(roi)).orElseThrow();
            var id = projection.getId();
            roiProjectionCommandService.handle(id, new RequestDowntimeCostCommand((int) Math.round(downtimeMinutes), branch.name()));
            roiProjectionCommandService.handle(id, new RequestEarningsCommand(round2(earnings)));

            double utilization = branchUtilization(units);
            int estimatedUsers = (int) Math.round(units.stream().mapToInt(e -> e.sessionSeconds.size()).sum() / (double) SIM_DAYS);
            if (utilization >= 0.40) {
                roiProjectionCommandService.handle(id, new DetectHighDemandSlotCommand(branch.name() + " 18:00-21:00", estimatedUsers));
            } else if (utilization < 0.25) {
                roiProjectionCommandService.handle(id, new DetectLowDemandSlotCommand(branch.name() + " 10:00-16:00", estimatedUsers));
            }
            units.stream()
                    .filter(e -> e.usageMinutes() / e.availableMinutes() < 0.10)
                    .forEach(e -> roiProjectionCommandService.handle(id, new RecommendTransferCommand(
                            "Trasladar " + e.displayName + " (uso " + Math.round(e.usageMinutes() / e.availableMinutes() * 100) + "%)",
                            "Sede Miraflores / " + e.model.zone())));
        }
    }

    private static double branchUtilization(List<SimEquipment> units) {
        double used = units.stream().mapToDouble(SimEquipment::usageMinutes).sum();
        double available = units.stream().mapToDouble(SimEquipment::availableMinutes).sum();
        return available == 0 ? 0 : used / available;
    }

    // ── Ground truth ─────────────────────────────────────────────────────────

    private void writeGroundTruth(List<SimEquipment> fleet) {
        var equipment = new ArrayList<Map<String, Object>>();
        for (var e : fleet) {
            var row = new LinkedHashMap<String, Object>();
            row.put("equipmentId", e.equipmentId);
            row.put("name", e.displayName);
            row.put("branch", e.branch.name());
            row.put("zone", e.model.zone());
            row.put("purchasePrice", e.model.price());
            row.put("sessions", e.sessionSeconds.size());
            row.put("usageMinutesExact", round2(e.usageMinutes()));
            row.put("usageMinutesAsBackendRoundsThem", e.sessionSeconds.stream().mapToLong(s -> Math.round(s / 60.0)).sum());
            row.put("downtimeMinutes", Math.round(e.downtimeMinutes));
            row.put("utilizationPct", round2(e.usageMinutes() / e.availableMinutes() * 100));
            row.put("last30VsPrev30Pct", e.percentageChange());
            row.put("correctiveTickets", e.correctiveCount);
            row.put("preventiveTickets", e.preventiveCount);
            row.put("openTickets", e.events.stream().filter(MaintenanceEvent::open).count());
            row.put("correctiveLaborCostCompleted", round2(e.laborCost));
            row.put("preventiveCostCompleted", round2(e.preventiveCost));
            row.put("sparePartsCostQuoted", round2(e.sparePartsCost));
            row.put("revenueAttributed", round2(e.usageMinutes() / 60.0 * REVENUE_PER_USAGE_HOUR));
            equipment.add(row);
        }
        var truth = new LinkedHashMap<String, Object>();
        truth.put("simulatedDays", SIM_DAYS);
        truth.put("openMinutesPerDay", OPEN_MINUTES_PER_DAY);
        truth.put("revenuePerUsageHour", REVENUE_PER_USAGE_HOUR);
        truth.put("randomSeed", RANDOM_SEED);
        truth.put("adminEmail", ADMIN_EMAIL);
        truth.put("equipment", equipment);
        try {
            var file = new File("target/realistic-seed-ground-truth.json");
            file.getParentFile().mkdirs();
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValue(file, truth);
            log.info("[RealisticDataSeeder] Ground truth written to {}", file.getAbsolutePath());
        } catch (Exception ex) {
            log.warn("[RealisticDataSeeder] Could not write ground truth file: {}", ex.getMessage());
        }
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @SuppressWarnings("unchecked")
    private static <T> T require(Result<T, ?> result, String step) {
        if (result instanceof Result.Success<T, ?> success) {
            return success.value();
        }
        throw new IllegalStateException("[RealisticDataSeeder] Failed at " + step + ": " + ((Result.Failure<T, ?>) result).error());
    }
}
