package com.spottrack.platform.profiles.application.acl;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.profiles.application.commandservices.AdminCommandService;
import com.spottrack.platform.profiles.application.commandservices.BusinessProfileCommandService;
import com.spottrack.platform.profiles.application.queryservices.AdminQueryService;
import com.spottrack.platform.profiles.application.queryservices.ClientQueryService;
import com.spottrack.platform.profiles.domain.model.commands.CreateAdminCommand;
import com.spottrack.platform.profiles.domain.model.commands.CreateBusinessProfileCommand;
import com.spottrack.platform.profiles.domain.model.queries.GetAdminByEmailQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetAdminByUserIdQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetClientByEmailQuery;
import com.spottrack.platform.profiles.domain.model.valueobjects.BusinessInfo;
import com.spottrack.platform.profiles.domain.model.valueobjects.ClientId;
import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.domain.model.valueobjects.PhoneNumber;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.repositories.ClientGymAssociationPersistenceRepository;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProfilesContextFacadeImpl implements ProfilesContextFacade {

    private final ClientQueryService clientQueryService;
    private final AdminQueryService adminQueryService;
    private final AdminCommandService adminCommandService;
    private final BusinessProfileCommandService businessProfileCommandService;
    private final ClientGymAssociationPersistenceRepository associationRepository;
    private final GymContextFacade gymContextFacade;

    public ProfilesContextFacadeImpl(ClientQueryService clientQueryService,
                                     AdminQueryService adminQueryService,
                                     AdminCommandService adminCommandService,
                                     BusinessProfileCommandService businessProfileCommandService,
                                     ClientGymAssociationPersistenceRepository associationRepository,
                                     GymContextFacade gymContextFacade) {
        this.clientQueryService = clientQueryService;
        this.adminQueryService = adminQueryService;
        this.adminCommandService = adminCommandService;
        this.businessProfileCommandService = businessProfileCommandService;
        this.associationRepository = associationRepository;
        this.gymContextFacade = gymContextFacade;
    }

    @Override
    public Long fetchClientIdByEmail(String email) {
        var query = new GetClientByEmailQuery(new EmailAddress(email));
        var client = clientQueryService.handle(query);
        return client.isEmpty() ? 0L : client.get().getId();
    }

    @Override
    public Long fetchAdminIdByEmail(String email) {
        var query = new GetAdminByEmailQuery(new EmailAddress(email));
        var admin = adminQueryService.handle(query);
        return admin.isEmpty() ? 0L : admin.get().getId();
    }

    @Override
    public void provisionAdminProfile(Long userId, String email,
                                      String firstName, String lastName,
                                      String phoneNumber, String dni) {
        var existing = adminQueryService.handle(new GetAdminByUserIdQuery(userId));
        if (existing.isPresent()) {
            log.info("Admin profile already exists for userId {}, skipping", userId);
            return;
        }
        var result = adminCommandService.handle(
                new CreateAdminCommand(userId, new EmailAddress(email), firstName, lastName, phoneNumber, dni));
        switch (result) {
            case Result.Success<?, ?> ignored ->
                    log.info("Admin profile provisioned for userId {}", userId);
            case Result.Failure<?, ApplicationError> f -> {
                log.error("Failed to provision Admin profile for userId {}: {}", userId, f.error());
                throw new RuntimeException("Admin provisioning failed: " + f.error().message());
            }
        }
    }

    @Override
    public void provisionBusinessProfile(Long userId, String companyName, String ruc,
                                         String legalStructure, String companyPhone, String companyEmail,
                                         String streetAddress, String city, String district) {
        var businessInfo = new BusinessInfo(
                companyName, ruc, legalStructure,
                new PhoneNumber(companyPhone),
                new EmailAddress(companyEmail),
                streetAddress, city, district
        );
        var result = businessProfileCommandService.handle(new CreateBusinessProfileCommand(userId, businessInfo));
        switch (result) {
            case Result.Success<?, ?> ignored ->
                    log.info("BusinessProfile provisioned for userId {}", userId);
            case Result.Failure<?, ApplicationError> f -> {
                if (f.error().code().endsWith("_CONFLICT")) {
                    log.info("BusinessProfile already exists for userId {}, skipping", userId);
                    return;
                }
                log.error("Failed to provision BusinessProfile for userId {}: {}", userId, f.error());
                throw new RuntimeException("BusinessProfile provisioning failed: " + f.error().message());
            }
        }
    }

    @Override
    public String fetchDniByEmail(String email) {
        var emailAddress = new EmailAddress(email);

        var clientOpt = clientQueryService.handle(new GetClientByEmailQuery(emailAddress));
        if (clientOpt.isPresent() && clientOpt.get().isProfileComplete()) {
            return clientOpt.get().getPersonInfo().dni().getDNI();
        }

        var adminOpt = adminQueryService.handle(new GetAdminByEmailQuery(emailAddress));
        if (adminOpt.isPresent() && adminOpt.get().isProfileComplete()) {
            return adminOpt.get().getPersonInfo().dni().getDNI();
        }

        return "";
    }

    @Override
    public String fetchActiveGymIdByClientId(Long clientId) {
        var activeOpt = associationRepository.findByClientIdAndActiveTrue(clientId);
        if (activeOpt.isEmpty()) return "";
        var active = activeOpt.get();
        var clientOpt = clientQueryService.handle(
                new com.spottrack.platform.profiles.domain.model.queries.GetClientByIdQuery(new ClientId(clientId)));
        if (clientOpt.isEmpty() || !clientOpt.get().isProfileComplete()) return "";
        var dni = clientOpt.get().getPersonInfo().dni().getDNI();
        return gymContextFacade.isDniWhitelistedForGym(active.getGymId(), dni) ? active.getGymId() : "";
    }

    @Override
    public String fetchClientNameById(Long clientId) {
        var clientOpt = clientQueryService.handle(
                new com.spottrack.platform.profiles.domain.model.queries.GetClientByIdQuery(new ClientId(clientId)));
        return clientOpt.filter(c -> c.isProfileComplete()).map(c -> c.getFullName()).orElse(null);
    }

    @Override
    public boolean hasActiveAssociationWithGym(Long clientId, String gymId) {
        return associationRepository.findByClientIdAndActiveTrue(clientId)
                .filter(a -> gymId.equals(a.getGymId()))
                .isPresent();
    }
}
