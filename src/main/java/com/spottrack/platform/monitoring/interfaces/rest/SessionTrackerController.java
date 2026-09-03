package com.spottrack.platform.monitoring.interfaces.rest;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.application.queryServices.SessionTrackerQueryService;
import com.spottrack.platform.monitoring.domain.model.aggregates.SessionTracker;
import com.spottrack.platform.monitoring.domain.model.commands.EndUsageSessionCommand;
import com.spottrack.platform.monitoring.domain.model.commands.VerifyUsageSessionCommand;
import com.spottrack.platform.monitoring.domain.model.queries.GetAllSessionTrackersQuery;
import com.spottrack.platform.monitoring.domain.model.queries.GetSessionTrackerByIdQuery;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.assemblers.SessionTrackerPersistenceAssembler;
import com.spottrack.platform.monitoring.interfaces.rest.resources.CreateSessionTrackerResource;
import com.spottrack.platform.monitoring.interfaces.rest.resources.SessionTrackerResource;
import com.spottrack.platform.monitoring.interfaces.rest.transform.CreateSessionTrackerCommandFromResource;
import com.spottrack.platform.monitoring.interfaces.rest.transform.SessionTrackerResourceFromEntity;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.reservation.interfaces.acl.ReservationContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/monitoring/session-trackers")
@Tag(name="SessionTracker")
public class SessionTrackerController {
    private final SessionTrackerCommandService sessionTrackerCommandService;
    private final SessionTrackerQueryService sessionTrackerQueryService;
    private final GymContextFacade gymContextFacade;
    private final ReservationContextFacade reservationContextFacade;
    private final ProfilesContextFacade profilesContextFacade;
    private final IamContextFacade iamContextFacade;

    public SessionTrackerController(SessionTrackerCommandService sessionTrackerCommandService,
                                     SessionTrackerQueryService sessionTrackerQueryService,
                                     GymContextFacade gymContextFacade,
                                     ReservationContextFacade reservationContextFacade,
                                     ProfilesContextFacade profilesContextFacade,
                                     IamContextFacade iamContextFacade){
        this.sessionTrackerCommandService = sessionTrackerCommandService;
        this.sessionTrackerQueryService = sessionTrackerQueryService;
        this.gymContextFacade = gymContextFacade;
        this.reservationContextFacade = reservationContextFacade;
        this.profilesContextFacade = profilesContextFacade;
        this.iamContextFacade = iamContextFacade;
    }

    @GetMapping
    public List<SessionTrackerResource> getAllSessionTrackers() {
        return sessionTrackerQueryService.handle(new GetAllSessionTrackersQuery()).stream()
                .map(this::toEnrichedResource)
                .toList();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SessionTrackerResource> getMySessionTrackers(Authentication authentication) {
        var adminUserId = iamContextFacade.fetchUserIdByUsername(authentication.getName()).orElse(0L);
        Set<String> myEquipmentIds = gymContextFacade.findEquipmentsByAdminUserId(adminUserId).stream()
                .map(equipment -> equipment.getId().uuid())
                .collect(Collectors.toSet());

        return sessionTrackerQueryService.handle(new GetAllSessionTrackersQuery()).stream()
                .filter(tracker -> myEquipmentIds.contains(tracker.getEquipmentId().uuid()))
                .map(this::toEnrichedResource)
                .toList();
    }

    @PostMapping("/create")
    public ResponseEntity createSessionTracker(@RequestBody CreateSessionTrackerResource resource){
            var command = CreateSessionTrackerCommandFromResource.toCommandFromResource(resource);
            var result = sessionTrackerCommandService.handle(command);
            return switch(result){
                case Result.Success< SessionTracker, ApplicationError > s->
                    ResponseEntity.status(HttpStatus.CREATED).body(SessionTrackerPersistenceAssembler.toPersistenceFromDomain(s.value()));

                case Result.Failure<SessionTracker, ApplicationError> f->
                    ResponseEntity.badRequest().body(f.error());
            };
    }

    @GetMapping("/{sessionTrackerId}/verify")
    public ResponseEntity verifySessionUsage(@PathVariable String sessionTrackerId) {
        var command = new VerifyUsageSessionCommand(new SessionTrackerId(sessionTrackerId));
        var result = sessionTrackerCommandService.handle(command);
        return switch (result) {
            case Result.Success<SessionTracker, ApplicationError> s ->
                ResponseEntity.ok(toEnrichedResource(s.value()));
            case Result.Failure<SessionTracker, ApplicationError> f ->
                ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
        };
    }

    @PatchMapping("/{sessionTrackerId}/end")
    public ResponseEntity endUsageSession(@PathVariable String sessionTrackerId){
        var command = new EndUsageSessionCommand(new SessionTrackerId(sessionTrackerId));
        var result = sessionTrackerCommandService.handle(command);
         return switch(result){
             case Result.Success<SessionTracker, ApplicationError> s ->
                 ResponseEntity.ok(toEnrichedResource(s.value()));
             case Result.Failure<SessionTracker, ApplicationError> f ->
                 ErrorResponseAssembler.toErrorResponseFromApplicationError(f.error());
         };
    }

    /**
     * Read-only preview of the session's current true activity. Does NOT end or
     * delete the tracker — that only happens once the session has actually ended
     * (see UsageSessionEndedEventHandler), which computes and reports the final
     * activity through the domain-event path instead of this endpoint.
     */
    @GetMapping("/{sessionTrackerId}/time")
    public ResponseEntity calculateSessionTime(@PathVariable String sessionTrackerId){
        var query = new GetSessionTrackerByIdQuery(new SessionTrackerId(sessionTrackerId));
        var entityOpt = sessionTrackerQueryService.handle(query);
        if (entityOpt.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("SessionTracker", sessionTrackerId));
        }
        var entity = entityOpt.get();
        var trueActivity = entity.peekTrueActivity();
        return ResponseEntity.ok(toEnrichedResource(entity, trueActivity));
    }

    private SessionTrackerResource toEnrichedResource(SessionTracker tracker) {
        return toEnrichedResource(tracker, null);
    }

    private SessionTrackerResource toEnrichedResource(SessionTracker tracker, java.time.LocalTime calculatedTrueActivity) {
        var equipmentName = gymContextFacade.findEquipmentById(tracker.getEquipmentId().uuid())
                .map(equipment -> equipment.getEquipmentName())
                .orElse(null);

        Long clientId = null;
        String clientName = null;
        if (tracker.getReservationId() != null) {
            var clientIdOpt = reservationContextFacade.fetchClientIdByReservationId(tracker.getReservationId().uuid());
            if (clientIdOpt.isPresent()) {
                clientId = clientIdOpt.get();
                clientName = profilesContextFacade.fetchClientNameById(clientId);
            }
        }

        return SessionTrackerResourceFromEntity.toResourceFromEntity(
                tracker, equipmentName, clientId, clientName, calculatedTrueActivity);
    }

}
