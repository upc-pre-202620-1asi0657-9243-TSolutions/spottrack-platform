package com.spottrack.platform.monitoring.interfaces.rest;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.spottrack.platform.iam.infrastructure.authorization.sfs.configuration.WebSecurityConfiguration;
import com.spottrack.platform.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import com.spottrack.platform.iam.infrastructure.authorization.sfs.pipeline.UnauthorizedRequestHandlerEntryPoint;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService;
import com.spottrack.platform.monitoring.application.commandServices.SensorPairingCommandService;
import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.application.ports.QrCodeRenderer;
import com.spottrack.platform.monitoring.application.queryServices.MotionSensorQueryService;
import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.commands.IssueSensorPairingCommand;
import com.spottrack.platform.monitoring.domain.model.commands.RegisterMotionSensorCommand;
import com.spottrack.platform.monitoring.domain.model.queries.GetAllMotionSensorsQuery;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.IssuedSensorPairing;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SensorPairingQrPayload;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.reservation.interfaces.acl.ReservationContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@Import({SensorPairingController.class, MotionSensorController.class,
        WebSecurityConfiguration.class, UnauthorizedRequestHandlerEntryPoint.class, BearerAuthorizationRequestFilter.class})
class SensorPairingControllerSecurityTest {

    /** Keeps PlatformApplication (and its @EnableJpaAuditing) out of this web slice. */
    @SpringBootConfiguration
    static class WebSliceConfiguration {
    }

    private static final String SENSOR_UUID = "066dd222-31ed-496a-bddf-e4711b0ee957";
    private static final String PAIRING_URL = "/api/v1/monitoring/motion-sensors/" + SENSOR_UUID + "/pairing";
    private static final String SECRET = "Zk3Qm-_9aB1cD2eF3gH4iJ5kL6mN7oP8";
    private static final String CA = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};

    @Autowired
    private WebApplicationContext context;
    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    // MockMvc would also register the bearer filter bean on its own, ahead of the security chain,
    // and the chain would then discard its authentication. Only the chain's copy must run, as in production.
    private MockMvc mockMvc;

    @MockitoBean private TokenService tokenService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private SensorPairingCommandService sensorPairingCommandService;
    @MockitoBean private QrCodeRenderer qrCodeRenderer;
    @MockitoBean private MotionSensorCommandService motionSensorCommandService;
    @MockitoBean private MotionSensorQueryService motionSensorQueryService;
    @MockitoBean private SessionTrackerCommandService sessionTrackerCommandService;
    @MockitoBean private GymContextFacade gymContextFacade;
    @MockitoBean private ReservationContextFacade reservationContextFacade;
    @MockitoBean private ProfilesContextFacade profilesContextFacade;
    @MockitoBean private IamContextFacade iamContextFacade;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();

        when(tokenService.validateToken("admin-token")).thenReturn(true);
        when(tokenService.getUsernameFromToken("admin-token")).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin"))
                .thenReturn(User.withUsername("admin").password("x").roles("ADMIN").build());
        when(tokenService.validateToken("client-token")).thenReturn(true);
        when(tokenService.getUsernameFromToken("client-token")).thenReturn("client");
        when(userDetailsService.loadUserByUsername("client"))
                .thenReturn(User.withUsername("client").password("x").roles("CLIENT").build());

        var payload = new SensorPairingQrPayload(1, SENSOR_UUID, SECRET, "34.123.45.67", 8883, CA, 1790000000L);
        when(sensorPairingCommandService.handle(new IssueSensorPairingCommand(SENSOR_UUID)))
                .thenReturn(Result.success(new IssuedSensorPairing(payload, LocalDateTime.of(2026, 11, 15, 12, 15))));
        when(qrCodeRenderer.renderPng(anyString(), anyInt())).thenReturn(PNG);
    }

    // --- pairing endpoint: security ---

    @Test
    void withoutTokenIsUnauthorizedAndNothingIsIssued() throws Exception {
        mockMvc.perform(post(PAIRING_URL)).andExpect(status().isUnauthorized());

        verifyNoInteractions(sensorPairingCommandService);
    }

    @Test
    void withAnInvalidTokenIsUnauthorized() throws Exception {
        mockMvc.perform(post(PAIRING_URL).header("Authorization", "Bearer nope")).andExpect(status().isUnauthorized());

        verifyNoInteractions(sensorPairingCommandService);
    }

    @Test
    void withoutTheAdminRoleIsForbiddenAndNothingIsIssued() throws Exception {
        mockMvc.perform(post(PAIRING_URL).header("Authorization", "Bearer client-token")).andExpect(status().isForbidden());

        verifyNoInteractions(sensorPairingCommandService);
    }

    @Test
    void theTrailingSlashVariantIsAlsoProtected() throws Exception {
        mockMvc.perform(post(PAIRING_URL + "/")).andExpect(status().isUnauthorized());
    }

    // --- pairing endpoint: response ---

    @Test
    void adminGetsTheQrPayloadAndThePng() throws Exception {
        mockMvc.perform(post(PAIRING_URL).header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.motionSensorId").value(SENSOR_UUID))
                .andExpect(jsonPath("$.expiresAt").value("2026-11-15T12:15:00"))
                .andExpect(jsonPath("$.payload.v").value(1))
                .andExpect(jsonPath("$.payload.deviceId").value(SENSOR_UUID))
                .andExpect(jsonPath("$.payload.secret").value(SECRET))
                .andExpect(jsonPath("$.payload.host").value("34.123.45.67"))
                .andExpect(jsonPath("$.payload.port").value(8883))
                .andExpect(jsonPath("$.payload.caSha256").value(CA))
                .andExpect(jsonPath("$.payload.exp").value(1790000000))
                .andExpect(jsonPath("$.qrPngBase64").value(Base64.getEncoder().encodeToString(PNG)));
    }

    @Test
    void payloadJsonIsTheCompactV1FormatInTheDocumentedFieldOrder() throws Exception {
        var expected = "{\"v\":1,\"deviceId\":\"" + SENSOR_UUID + "\",\"secret\":\"" + SECRET
                + "\",\"host\":\"34.123.45.67\",\"port\":8883,\"caSha256\":\"" + CA + "\",\"exp\":1790000000}";

        mockMvc.perform(post(PAIRING_URL).header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payloadJson").value(expected));

        verify(qrCodeRenderer).renderPng(expected, 320);
    }

    @Test
    void successAndErrorResponsesAreNotCacheable() throws Exception {
        mockMvc.perform(post(PAIRING_URL).header("Authorization", "Bearer admin-token"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")));

        when(sensorPairingCommandService.handle(new IssueSensorPairingCommand(SENSOR_UUID)))
                .thenReturn(Result.failure(ApplicationError.notFound("MotionSensor", SENSOR_UUID)));
        mockMvc.perform(post(PAIRING_URL).header("Authorization", "Bearer admin-token"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")));
    }

    @Test
    void unknownSensorIs404() throws Exception {
        when(sensorPairingCommandService.handle(new IssueSensorPairingCommand(SENSOR_UUID)))
                .thenReturn(Result.failure(ApplicationError.notFound("MotionSensor", SENSOR_UUID)));

        mockMvc.perform(post(PAIRING_URL).header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MOTIONSENSOR_NOT_FOUND"));
    }

    @Test
    void missingBrokerConfigurationIs422WithTheReason() throws Exception {
        when(sensorPairingCommandService.handle(new IssueSensorPairingCommand(SENSOR_UUID)))
                .thenReturn(Result.failure(ApplicationError.businessRuleViolation(
                        "Sensor pairing is configured", "mqtt.public-endpoint is not configured (expected host:port)")));

        mockMvc.perform(post(PAIRING_URL).header("Authorization", "Bearer admin-token"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.details").value("mqtt.public-endpoint is not configured (expected host:port)"));
    }

    @Test
    void provisioningFailureIs500() throws Exception {
        when(sensorPairingCommandService.handle(new IssueSensorPairingCommand(SENSOR_UUID)))
                .thenReturn(Result.failure(ApplicationError.unexpected("Broker credential provisioning", "broker 2 unreachable")));

        mockMvc.perform(post(PAIRING_URL).header("Authorization", "Bearer admin-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("UNEXPECTED_ERROR"));
    }

    @Test
    void blankSensorIdIs400() throws Exception {
        var blank = new IssueSensorPairingCommand(" ");
        when(sensorPairingCommandService.handle(blank))
                .thenReturn(Result.failure(ApplicationError.validationError("MotionSensor", "blank")));

        mockMvc.perform(post("/api/v1/monitoring/motion-sensors/%20/pairing").header("Authorization", "Bearer admin-token"))
                .andExpect(status().isBadRequest());
    }

    // --- the rest of /motion-sensors/** keeps behaving as before ---

    @Test
    void registeringASensorStillWorksWithoutToken() throws Exception {
        var now = LocalDateTime.now();
        var sensor = new MotionSensor(1L, new MotionSensorId(SENSOR_UUID), new EquipmentId("eq-1"), now, false, now, null, null);
        when(motionSensorCommandService.handle(any(RegisterMotionSensorCommand.class))).thenReturn(Result.success(sensor));

        mockMvc.perform(post("/api/v1/monitoring/motion-sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"equipmentId\":\"eq-1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.motionSensorId").value(SENSOR_UUID));
    }

    @Test
    void listingDevicesStillWorksWithoutToken() throws Exception {
        var now = LocalDateTime.now();
        var sensor = new MotionSensor(1L, new MotionSensorId(SENSOR_UUID), new EquipmentId("eq-1"), now, false, now, null, null);
        when(motionSensorQueryService.handle(any(GetAllMotionSensorsQuery.class))).thenReturn(List.of(sensor));

        mockMvc.perform(get("/api/v1/monitoring/motion-sensors/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value(SENSOR_UUID));
    }

    @Test
    void listingSensorsStillWorksWithoutToken() throws Exception {
        when(motionSensorQueryService.handle(any(GetAllMotionSensorsQuery.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/monitoring/motion-sensors")).andExpect(status().isOk());
    }

    @Test
    void otherPostsUnderMotionSensorsAreNotCaughtByThePairingRule() throws Exception {
        mockMvc.perform(post("/api/v1/monitoring/motion-sensors/" + SENSOR_UUID + "/other"))
                .andExpect(status().isNotFound());
    }
}
