package com.centre.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "requetes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Requete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RequeteType type;

    private String description;

    @Enumerated(EnumType.STRING)
    private EtatRequete etat;

    private String noteRetour;
    private Date date;

    private Date dateTraitement; // Fixed typo: changed from dateTraitment

    @ManyToOne
    @JoinColumn(name = "objet_id", nullable = false)
    private Objet objet;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private UserInfo client;

    @ManyToOne
    @JoinColumn(name = "guichetier_id")
    private UserInfo guichetier;

    @ManyToOne
    @JoinColumn(name = "technicien_id")
    private UserInfo technicien;

    @Column(nullable = false)
    private boolean archiver = false;
}