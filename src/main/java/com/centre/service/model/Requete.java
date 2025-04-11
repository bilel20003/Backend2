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
    private ReclamationType type; // Utilisation de l'énumération pour le type de réclamation

    @Enumerated(EnumType.STRING)
    private ReclamationObjet objet; // Utilisation de l'énumération pour l'objet de réclamation

    private String description;

    @Enumerated(EnumType.STRING)
    private EtatRequete etat; // Enum pour les états possibles

    private String noteRetour;
    private Date date; // Date de la requête


    @ManyToOne
    @JoinColumn(name = "client_id")
    private UserInfo client; // Le client qui a créé la requête

    @ManyToOne
    @JoinColumn(name = "guichetier_id")
    private UserInfo guichetier; // Le guichetier qui traite la requête

    @ManyToOne
    @JoinColumn(name = "technicien_id")
    private UserInfo technicien; // Le guichetier qui traite la requête

    

}