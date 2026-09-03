package com.spottrack.platform.profiles.application.internal.commandservices;

import com.spottrack.platform.profiles.application.commandservices.AdminCommandService;
import com.spottrack.platform.profiles.domain.model.aggregates.Admin;
import com.spottrack.platform.profiles.domain.model.commands.CreateAdminCommand;
import com.spottrack.platform.profiles.domain.model.commands.UpdateAdminProfileCommand;
import com.spottrack.platform.profiles.domain.repositories.AdminRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;


@Service
public class AdminCommandServiceImpl implements AdminCommandService {
    private final AdminRepository adminRepository;

    public AdminCommandServiceImpl(AdminRepository adminRepository){
        this.adminRepository = adminRepository;
    }

    @Override
    public Result<Admin, ApplicationError> handle(CreateAdminCommand command){
        try {
            if (adminRepository.existsByEmailAddress(command.emailAddress())){
                return Result.failure(ApplicationError.conflict(
                        "Admin",
                        "A admin with email address '%s' already exists".formatted(command.emailAddress())
                ));
            }

            var admin = new Admin(command);
            var savedAdmin = adminRepository.save(admin);
            return Result.success(savedAdmin);
        } catch (IllegalArgumentException e){
            return Result.failure(ApplicationError.validationError("Admin", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected(
                    "Admin creation",
                    e.getMessage()
            ));
        }
    }

    @Override
    public Result<Admin, ApplicationError> handle(UpdateAdminProfileCommand command){
        try {
            var admin = adminRepository.findById(command.adminId());

            if (admin.isEmpty()){
                return Result.failure(ApplicationError.notFound(
                        "Admin",
                        command.adminId().adminId().toString()
                ));
            }

            admin.get().updateProfile(command);
            var savedAdmin = adminRepository.save(admin.get());

            return Result.success(savedAdmin);
        } catch (IllegalArgumentException e){
            return Result.failure(ApplicationError.validationError("Admin", e.getMessage()));
        } catch (Exception e){
            return Result.failure(ApplicationError.unexpected("Admin update", e.getMessage()));
        }
    }
}
