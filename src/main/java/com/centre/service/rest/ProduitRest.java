package com.centre.service.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.centre.service.model.Produit;

@RequestMapping(path = "/api/produits")
public interface ProduitRest {

    @PostMapping("/addproduit")
    ResponseEntity<?> createProduit(@RequestBody(required = true) Produit produit);

    @GetMapping("/getallproduits")
    ResponseEntity<?> getAllProduits();

    @GetMapping("/getproduit/{id}")
    ResponseEntity<?> getProduitById(@PathVariable Long id);

    @PutMapping("/updateproduit/{id}")
    ResponseEntity<?> updateProduit(@PathVariable Long id, @RequestBody Produit produit);

    @DeleteMapping("/deleteproduit/{id}")
    ResponseEntity<?> deleteProduit(@PathVariable Long id);
}