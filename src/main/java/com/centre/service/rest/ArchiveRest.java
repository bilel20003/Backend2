package com.centre.service.rest;

import com.centre.service.service.MinistereService;
import com.centre.service.service.ServiceeService;
import com.centre.service.service.UserInfoService;
import com.centre.service.service.ProduitService;
import com.centre.service.service.ObjetService;
import com.centre.service.service.RequeteService;
import com.centre.service.service.RdvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/archive")
public class ArchiveRest {

    @Autowired
    private MinistereService ministereService;

    @Autowired
    private ServiceeService serviceeService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private ProduitService produitService;

    @Autowired
    private ObjetService objetService;

    @Autowired
    private RequeteService requeteService;

    @Autowired
    private RdvService rdvService;

    // Get all archived Ministeres
    @GetMapping("/ministeres")
    public ResponseEntity<?> getAllArchivedMinisteres() {
        return ministereService.getAllArchivedMinisteres();
    }

    // Unarchive a Ministere
    @PutMapping("/ministeres/{id}/unarchive")
    public ResponseEntity<?> unarchiveMinistere(@PathVariable Long id) {
        return ministereService.unarchiveMinistere(id);
    }

    // Get all archived Services
    @GetMapping("/services")
    public ResponseEntity<?> getAllArchivedServices() {
        return serviceeService.getAllArchivedServices();
    }

    // Unarchive a Service
    @PutMapping("/services/{id}/unarchive")
    public ResponseEntity<?> unarchiveService(@PathVariable Long id) {
        return serviceeService.unarchiveService(id);
    }

    // Get all archived Users
    @GetMapping("/users")
    public ResponseEntity<?> getAllArchivedUsers() {
        return userInfoService.getAllArchivedUsers();
    }

    // Unarchive a User
    @PutMapping("/users/{id}/unarchive")
    public ResponseEntity<?> unarchiveUser(@PathVariable Long id) {
        return userInfoService.unarchiveAppuser(id);
    }

    // Get all archived Produits
    @GetMapping("/produits")
    public ResponseEntity<?> getAllArchivedProduits() {
        return produitService.getAllArchivedProduits();
    }

    // Unarchive a Produit
    @PutMapping("/produits/{id}/unarchive")
    public ResponseEntity<?> unarchiveProduit(@PathVariable Long id) {
        return produitService.unarchiveProduit(id);
    }

    // Get all archived Objets
    @GetMapping("/objets")
    public ResponseEntity<?> getAllArchivedObjets() {
        return objetService.getAllArchivedObjets();
    }

    // Unarchive an Objet
    @PutMapping("/objets/{id}/unarchive")
    public ResponseEntity<?> unarchiveObjet(@PathVariable Long id) {
        return objetService.unarchiveObjet(id);
    }

    // Get all archived Requetes
    @GetMapping("/requetes")
    public ResponseEntity<?> getAllArchivedRequetes() {
        return requeteService.getAllArchivedRequetes();
    }

    // Unarchive a Requete
    @PutMapping("/requetes/{id}/unarchive")
    public ResponseEntity<?> unarchiveRequete(@PathVariable Long id) {
        return requeteService.unarchiveRequete(id);
    }

    // Get all archived Rdvs
    @GetMapping("/rdvs")
    public ResponseEntity<?> getAllArchivedRdvs() {
        return rdvService.getAllArchivedRdvs();
    }

    // Unarchive an Rdv
    @PutMapping("/rdvs/{id}/unarchive")
    public ResponseEntity<?> unarchiveRdv(@PathVariable Long id) {
        return rdvService.unarchiveRdv(id);
    }
}