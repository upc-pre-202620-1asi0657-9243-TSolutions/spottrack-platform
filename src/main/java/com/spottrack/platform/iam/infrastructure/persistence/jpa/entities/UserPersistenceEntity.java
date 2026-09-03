package com.spottrack.platform.iam.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "users")
public class UserPersistenceEntity extends AuditableAbstractPersistenceEntity {

    // unique=true adds a DB-level UNIQUE KEY on top of the application-level existsByUsername check,
    // closing the race-condition window that existed when only the application layer guarded uniqueness.
    @Column(name = "username", unique = true)
    private String username;
    private String password;
    private boolean active = true;

    // Boxed rather than primitive: ddl-auto=update adds this column with NULL for
    // rows that existed before this field was introduced, and a primitive boolean
    // can't hold that during hydration — Hibernate throws on every read of an old row.
    @Column(name = "notify_on_critical")
    private Boolean notifyOnCritical = true;

    @Column(name = "notify_on_warning")
    private Boolean notifyOnWarning = true;

    @Column(name = "notification_email")
    private String notificationEmail;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<RolePersistenceEntity> roles;

    public UserPersistenceEntity() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<RolePersistenceEntity> getRoles() {
        return roles;
    }

    public void setRoles(List<RolePersistenceEntity> roles) {
        this.roles = roles;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isNotifyOnCritical() {
        return notifyOnCritical == null || notifyOnCritical;
    }

    public void setNotifyOnCritical(boolean notifyOnCritical) {
        this.notifyOnCritical = notifyOnCritical;
    }

    public boolean isNotifyOnWarning() {
        return notifyOnWarning == null || notifyOnWarning;
    }

    public void setNotifyOnWarning(boolean notifyOnWarning) {
        this.notifyOnWarning = notifyOnWarning;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }
}
