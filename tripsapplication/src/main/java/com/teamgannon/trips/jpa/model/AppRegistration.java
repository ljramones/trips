package com.teamgannon.trips.jpa.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity for storing user registration information for problem reports.
 * Uses a singleton pattern with a fixed ID "REGISTRATION".
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "app_registration")
public class AppRegistration implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String REGISTRATION_ID = "REGISTRATION";

    @Id
    private String id = REGISTRATION_ID;

    /**
     * Unique installation identifier, generated once and never changes.
     */
    private String installId;

    /**
     * User's email address for follow-up communication.
     */
    private String email;

    /**
     * User's display name for reports.
     */
    private String displayName;

    /**
     * Default preference: include system info in reports.
     */
    private boolean defaultIncludeSystemInfo = true;

    /**
     * Default preference: include log tail in reports.
     */
    private boolean defaultIncludeLogs = true;

    /**
     * Default preference: include screenshot in reports.
     */
    private boolean defaultIncludeScreenshot = false;

    /**
     * Timestamp when user first registered.
     */
    private Instant registeredAt;

    /**
     * Timestamp of last submitted report.
     */
    private Instant lastReportAt;

    /**
     * Creates a new registration with a generated install ID.
     */
    public static AppRegistration createNew(String email, String displayName) {
        AppRegistration registration = new AppRegistration();
        registration.setId(REGISTRATION_ID);
        registration.setInstallId(UUID.randomUUID().toString());
        registration.setEmail(email);
        registration.setDisplayName(displayName);
        registration.setRegisteredAt(Instant.now());
        return registration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AppRegistration that = (AppRegistration) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
