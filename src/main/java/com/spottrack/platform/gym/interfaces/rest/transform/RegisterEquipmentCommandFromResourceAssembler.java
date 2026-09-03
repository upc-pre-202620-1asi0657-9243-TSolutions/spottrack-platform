package com.spottrack.platform.gym.interfaces.rest.transform;

import com.spottrack.platform.gym.domain.model.commands.RegisterEquipment;
import com.spottrack.platform.gym.domain.model.valueobjects.ManufacturerId;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;
import com.spottrack.platform.gym.interfaces.rest.resources.RegisterEquipmentResource;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;

public class RegisterEquipmentCommandFromResourceAssembler {
    public static RegisterEquipment toCommandFromResource(RegisterEquipmentResource resource){
        return new RegisterEquipment(
                resource.equipmentName(),
                resource.status(),
                resource.model(),
                new ManufacturerId(resource.manufacturerId()),
                new ZoneId(resource.zoneId()),
                new Money(resource.purchaseAmount(), resource.purchaseCurrency()),
                resource.maintenanceThreshold()
        );
    }
}
