package com.centre.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity 
@Table // Nom explicite pour la table 
@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserInfo implements Serializable {

    private static final long SerialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String email;
    private String password;
    private String isDeletable;
    private String status;
    
    @Enumerated(EnumType.STRING)
    private Role role;

    public static long getSerialversionuid() {
        return SerialVersionUID;
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIsDeletable() {
        return isDeletable;
    }

    public void setIsDeletable(String isDeletable) {
        this.isDeletable = isDeletable;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    
}