package com.spottrack.platform.profiles.application.internal.commandservices;

import com.spottrack.platform.profiles.application.commandservices.BusinessProfileCommandService;
import com.spottrack.platform.profiles.domain.model.aggregates.BusinessProfile;
import com.spottrack.platform.profiles.domain.model.commands.CreateBusinessProfileCommand;
import com.spottrack.platform.profiles.domain.repositories.BusinessProfileRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class BusinessProfileCommandServiceImpl implements BusinessProfileCommandService {

    private final BusinessProfileRepository businessProfileRepository;

    public BusinessProfileCommandServiceImpl(BusinessProfileRepository businessProfileRepository) {
        this.businessProfileRepository = businessProfileRepository;
    }

    @Override
    public Result<BusinessProfile, ApplicationError> handle(CreateBusinessProfileCommand command) {
        try {
            if (businessProfileRepository.existsByUserId(command.userId())) {
                return Result.failure(ApplicationError.conflict(
                        "BusinessProfile",
                        "A business profile for user '%d' already exists".formatted(command.userId())
                ));
            }
            var profile = new BusinessProfile(command);
            var saved = businessProfileRepository.save(profile);
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("BusinessProfile", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("BusinessProfile creation", e.getMessage()));
        }
    }
}
