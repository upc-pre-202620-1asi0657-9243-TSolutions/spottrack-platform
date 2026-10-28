package com.spottrack.platform.maintenance.application.acl;

import com.spottrack.platform.maintenance.application.commandServices.MaintenanceCommandService;
import com.spottrack.platform.maintenance.domain.model.aggregates.Technician;
import com.spottrack.platform.maintenance.domain.model.commands.CreateTechnician;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaintenanceContextFacadeImplTest {

    private MaintenanceCommandService commandService;
    private MaintenanceContextFacadeImpl facade;

    @BeforeEach
    void setUp() {
        commandService = mock(MaintenanceCommandService.class);
        facade = new MaintenanceContextFacadeImpl(commandService);
    }

    @Test
    void provisionsTechnicianLinkedToUserAndAdminAndReturnsItsId() {
        when(commandService.handle(org.mockito.ArgumentMatchers.any(CreateTechnician.class)))
                .thenReturn(Result.success(new Technician("tech-1", "Maria Perez", 10L, 42L)));

        var result = facade.provisionTechnician(10L, 42L, "Maria Perez");

        assertEquals("tech-1", assertInstanceOf(Result.Success.class, result).value());
        var captor = ArgumentCaptor.forClass(CreateTechnician.class);
        verify(commandService).handle(captor.capture());
        assertEquals("Maria Perez", captor.getValue().name());
        assertEquals(10L, captor.getValue().userId());
        assertEquals(42L, captor.getValue().adminUserId());
    }

    @Test
    void propagatesFailureFromTheCommandService() {
        var error = ApplicationError.unexpected("Technician creation", "db down");
        when(commandService.handle(org.mockito.ArgumentMatchers.any(CreateTechnician.class)))
                .thenReturn(Result.failure(error));

        var result = facade.provisionTechnician(10L, 42L, "Maria Perez");

        assertEquals(error, assertInstanceOf(Result.Failure.class, result).error());
    }
}
