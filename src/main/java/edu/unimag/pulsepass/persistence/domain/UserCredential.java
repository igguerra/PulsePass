package edu.unimag.pulsepass.persistence.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "user_credentials")
public class UserCredential {

    @Id 
    @Column(name = "user_id")
    private Long userId; 

    @MapsId 
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user; 

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash; 

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt; 

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 200)
    private Set<Role> roles = new HashSet<>(); 

    protected UserCredential() {

    }

    public UserCredential(User user, String passwordHash) {
        this.user = user;
        this.passwordHash = passwordHash;
        this.passwordChangedAt = LocalDateTime.now();
    }

    public void addRole(Role role) { this.roles.add(role); }

    public Long getUserId() { return userId; }
    public User getUser() { return user; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }



}
