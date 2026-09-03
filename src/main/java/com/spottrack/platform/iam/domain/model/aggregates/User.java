package com.spottrack.platform.iam.domain.model.aggregates;

import com.spottrack.platform.iam.domain.model.entities.Role;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class User extends AbstractDomainAggregateRoot<User> {

    @Setter
    private Long id;

    @Setter
    private String username;

    @Setter
    private String password;

    @Setter
    private Set<Role> roles;

    @Setter
    private boolean active = true;

    @Setter
    private boolean notifyOnCritical = true;

    @Setter
    private boolean notifyOnWarning = true;

    @Setter
    private String notificationEmail;

    public User() {
        this.roles = new HashSet<>();
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>();
    }

    public User(String username, String password, List<Role> roles) {
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>(roles);
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void addRoles(List<Role> roles) {
        this.roles.addAll(roles);
    }

    public void deactivate() {
        this.active = false;
    }
}
