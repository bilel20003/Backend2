package com.centre.service.service;

import com.centre.service.model.EtatRequete;
import com.centre.service.model.Requete;
import com.centre.service.model.UserInfo;
import com.centre.service.repository.RequeteRepository;
import com.centre.service.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class RequeteService {

    @Autowired
    private RequeteRepository requeteRepository;

    @Autowired
    private UserInfoRepository personneRepository;

    // Créer une nouvelle requête
    public Requete creerRequete(UserInfo client, UserInfo guichetier, String type, String objet, String description) {
        Requete requete = new Requete();
        requete.setClient(client);
        requete.setGuichetier(guichetier);
        requete.setType(type);
        requete.setObjet(objet);
        requete.setDescription(description);
        requete.setEtat(EtatRequete.EN_COURS_DE_TRAITEMENT);  // Exemple d'état initial
        requete.setDate(new Date());

        // Sauvegarder la requête
        return requeteRepository.save(requete);
    }

    // Méthode pour assigner un technicien à une requête
    public Requete assignTechnician(Long requeteId, Long technicienId) throws Exception {
        Optional<Requete> optionalRequete = requeteRepository.findById(requeteId);
        if (!optionalRequete.isPresent()) {
            throw new Exception("Requête non trouvée.");
        }

        Requete requete = optionalRequete.get();

        Optional<UserInfo> optionalTechnicien = personneRepository.findById(technicienId);
        if (!optionalTechnicien.isPresent() || !optionalTechnicien.get().getRole().equals("TECHNICIEN")) {
            throw new Exception("Technicien non trouvé ou rôle incorrect.");
        }

        UserInfo technicien = optionalTechnicien.get();
        requete.setTechnicien(technicien);

        return requeteRepository.save(requete);
    }

    // Autres méthodes de service pour traiter les requêtes...
}
