package com.centre.service.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class Rdv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date dateSouhaitee;
    private String description;
    private String status;
    private String typeProbleme;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_envoi")
    private Date dateEnvoi;

    // Relations avec les autres entités
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)  // Le client crée le rendez-vous
    private UserInfo client;

    @ManyToOne
    @JoinColumn(name = "guichetier_id", nullable = false)  // Le guichetier reçoit et traite le rendez-vous
    private UserInfo guichetier;

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateSouhaitee() {
        return dateSouhaitee;
    }

    public void setDateSouhaitee(Date dateSouhaitee) {
        this.dateSouhaitee = dateSouhaitee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTypeProbleme() {
        return typeProbleme;
    }

    public void setTypeProbleme(String typeProbleme) {
        this.typeProbleme = typeProbleme;
    }

    public Date getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(Date dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public UserInfo getClient() {
        return client;
    }

    public void setClient(UserInfo client) {
        this.client = client;
    }

    public UserInfo getGuichetier() {
        return guichetier;
    }

    public void setGuichetier(UserInfo guichetier) {
        this.guichetier = guichetier;
    }
}
