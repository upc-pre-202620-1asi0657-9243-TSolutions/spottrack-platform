package com.spottrack.platform.profiles.interfaces.rest.transform;

import com.spottrack.platform.profiles.domain.model.aggregates.Client;
import com.spottrack.platform.profiles.interfaces.rest.resources.ClientResource;

public class ClientResourceFromEntityAssembler {

    public static ClientResource toResourceFromEntity(Client entity) {
        var personInfo = entity.getPersonInfo();
        var phoneNumber = personInfo != null ? personInfo.phoneNumber().phoneNumber() : null;
        var firstName   = personInfo != null ? personInfo.firstName() : null;
        var lastName    = personInfo != null ? personInfo.lastName() : null;
        var dni         = personInfo != null ? personInfo.dni().dni() : null;
        return new ClientResource(
                entity.getId(),
                entity.getFullName(),
                entity.getEmailAddress(),
                phoneNumber,
                firstName,
                lastName,
                dni);
    }
}
