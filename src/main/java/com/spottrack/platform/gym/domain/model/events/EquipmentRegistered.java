package com.spottrack.platform.gym.domain.model.events;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class EquipmentRegistered extends ApplicationEvent {

    /**
     * Id of the registered equipment
     */
    EquipmentId equipmentId;

    /**
     * @param id
     * @param source
     *
     */
    public EquipmentRegistered(EquipmentId id, Object source){
        super(source);
        this.equipmentId = id;
    }

}