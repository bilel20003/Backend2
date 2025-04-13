package com.centre.service.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.centre.service.model.Role;
import com.centre.service.model.Servicee;
import com.centre.service.model.UserInfo;
import com.centre.service.model.Ministere; // Import manquant
import com.centre.service.repository.ServiceeRepository;
import com.centre.service.repository.UserInfoRepository;
import com.centre.service.repository.MinistereRepository; // Ajoutez ce repository
import java.util.Optional;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private ServiceeRepository serviceeRepository;

    @Autowired
    private MinistereRepository ministereRepository; // Ajoutez ce repository

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
                    newService.setMinistere(ministere); // Lier le ministère
                    return serviceeRepository.save(newService);
                });

        // Créer l'admin
        if (!userInfoRepository.findByEmail("admin@email.com").isPresent()) {
            UserInfo admin = new UserInfo();
            admin.setName("admin");
            admin.setEmail("admin@email.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setIsDeletable("false");
            admin.setStatus("true");
            admin.setRole(Role.ADMIN);
            admin.setService(adminService); // Lier le service

            userInfoRepository.save(admin);
        }

        System.out.println("============== You can login with: admin@email.com / admin ============");
    }
}