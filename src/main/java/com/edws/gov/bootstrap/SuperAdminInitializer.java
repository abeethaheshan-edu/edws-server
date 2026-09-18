package com.edws.gov.bootstrap;

import com.edws.gov.config.BootstrapProperties;
import com.edws.gov.entity.User;
import com.edws.gov.entity.UserProfile;
import com.edws.gov.enums.Role;
import com.edws.gov.enums.UserStatus;
import com.edws.gov.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SuperAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminInitializer.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapProperties properties;

    public SuperAdminInitializer(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 BootstrapProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println(properties.toString());
        if (!properties.enabled()) {
            return;
        }

        String email = properties.superAdminEmail();

        if (userRepository.existsByEmail(email)) {
            log.info("Super admin already present, skipping bootstrap");
            return;
        }

        boolean generated = properties.superAdminPassword().isBlank();
        String password = generated ? generatePassword() : properties.superAdminPassword();

        UserProfile profile = new UserProfile();
        profile.setFullName(properties.superAdminName());

        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.SUPER_ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setProfile(profile);
        admin.setTempPassword(true);
        admin.setHouseHolder(false);

        userRepository.save(admin);

        log.warn("=================================================================");
        log.warn(" SUPER ADMIN CREATED");
        log.warn(" email    : {}", email);
        if (generated) {
            log.warn(" password : {}", password);
            log.warn(" This password is shown once. Sign in and change it now.");
        } else {
            log.warn(" password : taken from BOOTSTRAP_SUPER_ADMIN_PASSWORD");
        }
        log.warn(" You will be asked to set a new password at first sign in.");
        log.warn("=================================================================");
    }

    private String generatePassword() {
        byte[] bytes = new byte[9];
        RANDOM.nextBytes(bytes);
        return "Temp" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).replaceAll("[^A-Za-z0-9]", "") + "#1";
    }
}
