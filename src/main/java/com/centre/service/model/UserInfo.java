package com.centre.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
@NamedQuery(name = "UserInfo.getAllAppuser", query = "SELECT new com.centre.service.model.UserInfo(u.id, u.name, u.email, u.status, u.role, u.service, u.produit) FROM UserInfo u WHERE u.isDeletable = 'true' AND u.email NOT IN (:email)")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;
    private String isDeletable;
    private String status;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Servicee service;

    @Column(nullable = false)
    private boolean archiver = false;

    @Column
    private String resetToken;

    @Column
    private LocalDateTime resetTokenExpiry;

    // Updated constructor to include produit
    public UserInfo(Long id, String name, String email, String status, Role role, Servicee service, Produit produit) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = status;
        this.role = role;
        this.service = service;
        this.produit = produit;
        this.archiver = false;
    }
}