package com.centre.service.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.centre.service.model.Role;
import com.centre.service.model.Servicee;
import com.centre.service.model.UserInfo;
import com.centre.service.model.Ministere;
import com.centre.service.model.Produit;
import com.centre.service.repository.ServiceeRepository;
import com.centre.service.repository.UserInfoRepository;
import com.centre.service.repository.MinistereRepository;
import com.centre.service.repository.RoleRepository;
import com.centre.service.repository.ProduitRepository;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private ServiceeRepository serviceeRepository;

    @Autowired
    private MinistereRepository ministereRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Créer ou récupérer un ministère
        Ministere ministere = ministereRepository.findByNomMinistere("Ministère de l'IT")
                .orElseGet(() -> {
                    Ministere newMinistere = new Ministere();
                    newMinistere.setNomMinistere("Ministère de l'IT");
                    return ministereRepository.save(newMinistere);
                });

        // Créer ou récupérer le service "Administration" lié au ministère
        Servicee adminService = serviceeRepository.findByNomService("Administration")
                .orElseGet(() -> {
                    Servicee newService = new Servicee();
                    newService.setNomService("Administration");
                    newService.setMinistere(ministere);
                    return serviceeRepository.save(newService);
                });

        // Créer ou récupérer le produit "Any" pour non-client
        Produit anyProduit = produitRepository.findByNom("Any")
                .orElseGet(() -> {
                    Produit newProduit = new Produit();
                    newProduit.setNom("Any");
                    newProduit.setDescription("Placeholder product for non-client users");
                    newProduit.setTopologie("N/A");
                    newProduit.setPrix(0.0);
                    return produitRepository.save(newProduit);
                });

        // Créer ou récupérer les rôles nécessaires
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ADMIN");
                    return roleRepository.save(newRole);
                });

        // Créer les autres rôles pour éviter des erreurs futures
        String[] roles = { "GUICHETIER", "TECHNICIEN", "CLIENT" };
        for (String roleName : roles) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName(roleName);
                        return roleRepository.save(newRole);
                    });
        }

        // Créer l'admin
        if (!userInfoRepository.findByEmail("admin@email.com").isPresent()) {
            UserInfo admin = new UserInfo();
            admin.setName("admin");
            admin.setEmail("admin@email.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setIsDeletable("false");
            admin.setStatus("true");
            admin.setRole(adminRole);
            admin.setService(adminService);
            admin.setProduit(anyProduit); // Use "Any" product for ADMIN

            userInfoRepository.save(admin);
        }

        System.out.println("============== You can login with: admin@email.com / admin ============");
    }
}