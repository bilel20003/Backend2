package com.centre.service.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.centre.service.model.UserInfo;
import com.centre.service.repository.UserInfoRepository;

@Component
public class DataLoader implements ApplicationRunner{

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
public void run(ApplicationArguments args) throws Exception {
    // Vérifiez si l'utilisateur existe déjà
    if (!userInfoRepository.findByEmail("admin@email.com").isPresent()) {
        UserInfo userInfo = new UserInfo();
        userInfo.setName("admin");
        userInfo.setEmail("admin@email.com");
        userInfo.setIsDeletable("false");
        userInfo.setStatus("true");
        userInfo.setPassword(passwordEncoder.encode("admin"));
        userInfoRepository.save(userInfo);
    }
    System.out.println("==============You can login with : email(admin@email.com) and password(admin)============");
}

}
