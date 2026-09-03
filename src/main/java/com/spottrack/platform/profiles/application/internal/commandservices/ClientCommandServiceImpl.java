package com.spottrack.platform.profiles.application.internal.commandservices;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.profiles.application.commandservices.ClientCommandService;
import com.spottrack.platform.profiles.domain.model.aggregates.Client;
import com.spottrack.platform.profiles.domain.model.commands.AssociateClientWithGymCommand;
import com.spottrack.platform.profiles.domain.model.commands.ChangeActiveGymCommand;
import com.spottrack.platform.profiles.domain.model.commands.CreateClientCommand;
import com.spottrack.platform.profiles.domain.model.commands.UpdateClientProfileCommand;
import com.spottrack.platform.profiles.domain.model.entities.ClientGymAssociation;
import com.spottrack.platform.profiles.domain.repositories.ClientRepository;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.entities.ClientGymAssociationPersistenceEntity;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.repositories.ClientGymAssociationPersistenceRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ClientCommandServiceImpl implements ClientCommandService {
    private final ClientRepository clientRepository;
    private final ClientGymAssociationPersistenceRepository associationRepository;
    private final GymContextFacade gymContextFacade;
    private final IamContextFacade iamContextFacade;

    public ClientCommandServiceImpl(ClientRepository clientRepository,
                                    ClientGymAssociationPersistenceRepository associationRepository,
                                    GymContextFacade gymContextFacade,
                                    IamContextFacade iamContextFacade) {
        this.clientRepository = clientRepository;
        this.associationRepository = associationRepository;
        this.gymContextFacade = gymContextFacade;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    public Result<Client, ApplicationError> handle(CreateClientCommand command){
        try {
            if (!iamContextFacade.existsUserById(command.userId())) {
                return Result.failure(ApplicationError.notFound("User", command.userId().toString()));
            }

            if (clientRepository.existsByEmailAddress(command.emailAddress())){
                return Result.failure(ApplicationError.conflict(
                        "Client",
                        "A client with email address '%s' already exists".formatted(command.emailAddress())
                ));
            }

            var client = new Client(command);
            var savedClient = clientRepository.save(client);
            return Result.success(savedClient);
        } catch (IllegalArgumentException e){
            return Result.failure(ApplicationError.validationError("Client", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected(
                    "Client creation",
                    e.getMessage()
            ));
        }
    }

    @Override
    public Result<Client, ApplicationError> handle(UpdateClientProfileCommand command){
        try {
            var client = clientRepository.findById(command.clientId());

            if (client.isEmpty()){
                return Result.failure(ApplicationError.notFound(
                        "Client",
                        command.clientId().clientId().toString()
                ));
            }

            client.get().updateProfile(command);
            var savedClient = clientRepository.save(client.get());

            return Result.success(savedClient);
        } catch (IllegalArgumentException e){
            return Result.failure(ApplicationError.validationError("Client", e.getMessage()));
        } catch (Exception e){
            return Result.failure(ApplicationError.unexpected("Client update", e.getMessage()));
        }
    }

    @Transactional
    @Override
    public Result<ClientGymAssociation, ApplicationError> handle(AssociateClientWithGymCommand command) {
        var clientOpt = clientRepository.findById(new com.spottrack.platform.profiles.domain.model.valueobjects.ClientId(command.clientId()));
        if (clientOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Client", command.clientId().toString()));
        }
        var client = clientOpt.get();
        if (!client.isProfileComplete()) {
            return Result.failure(ApplicationError.businessRuleViolation("Client", "profiles.error.gym.profileIncomplete"));
        }
        var dni = client.getPersonInfo().dni().getDNI();
        if (!gymContextFacade.isDniWhitelistedForGym(command.gymId(), dni)) {
            return Result.failure(ApplicationError.forbidden("GymWhitelist", "profiles.error.gym.notWhitelisted"));
        }
        if (associationRepository.existsByClientIdAndGymId(command.clientId(), command.gymId())) {
            return Result.failure(ApplicationError.conflict("ClientGymAssociation", "profiles.error.gym.alreadyAssociated"));
        }
        boolean isFirst = associationRepository.findByClientId(command.clientId()).isEmpty();
        var entity = new ClientGymAssociationPersistenceEntity(command.clientId(), command.gymId(), isFirst);
        var saved = associationRepository.save(entity);
        return Result.success(new ClientGymAssociation(saved.getId(), saved.getClientId(), saved.getGymId(), saved.isActive()));
    }

    @Transactional
    @Override
    public Result<ClientGymAssociation, ApplicationError> handle(ChangeActiveGymCommand command) {
        var targetOpt = associationRepository.findByClientIdAndGymId(command.clientId(), command.gymId());
        if (targetOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("ClientGymAssociation", "profiles.error.gym.notAssociated"));
        }
        var clientOpt = clientRepository.findById(new com.spottrack.platform.profiles.domain.model.valueobjects.ClientId(command.clientId()));
        if (clientOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Client", command.clientId().toString()));
        }
        var dni = clientOpt.get().getPersonInfo().dni().getDNI();
        if (!gymContextFacade.isDniWhitelistedForGym(command.gymId(), dni)) {
            return Result.failure(ApplicationError.forbidden("GymWhitelist", "profiles.error.gym.notWhitelisted"));
        }
        associationRepository.findAllByClientIdAndActiveTrue(command.clientId())
                .forEach(e -> { e.setActive(false); associationRepository.save(e); });
        var target = targetOpt.get();
        target.setActive(true);
        var saved = associationRepository.save(target);
        return Result.success(new ClientGymAssociation(saved.getId(), saved.getClientId(), saved.getGymId(), saved.isActive()));
    }
}
