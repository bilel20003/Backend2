package com.centre.service.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class Requete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String objet;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private EtatRequete etat; // Enum pour les états possibles

    private String noteRetour;
    private Date date;

    // Relations avec les autres entités
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)  // Le client crée la requête
    private UserInfo client;

    @ManyToOne
    @JoinColumn(name = "guichetier_id", nullable = false)  // Le guichetier reçoit et traite la requête
    private UserInfo guichetier;

    @ManyToOne
    @JoinColumn(name = "technicien_id")  // Le technicien peut être assigné à la requête
    private UserInfo technicien;

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EtatRequete getEtat() {
        return etat;
    }

    public void setEtat(EtatRequete etat) {
        this.etat = etat;
    }

    public String getNoteRetour() {
        return noteRetour;
    }

    public void setNoteRetour(String noteRetour) {
        this.noteRetour = noteRetour;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public UserInfo getTechnicien() {
        return technicien;
    }

    public void setTechnicien(UserInfo technicien) {
        this.technicien = technicien;
    }
}
