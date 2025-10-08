package com.ecom.users.config;

import com.ecom.users.entity.Role;
import com.ecom.users.entity.User;
import com.ecom.users.enums.TypeRole;
import com.ecom.users.repository.RoleRepository;
import com.ecom.users.repository.UserRepository;
import com.ecom.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqlCreate {
    @Value("${client.id}")
    private  String clientId;
    @Value("${client.secret}")
    private  String clientSecret;
    @Value("${sas.jwk.uri}")
    private String jwtUri;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

        @EventListener(ApplicationReadyEvent.class)
        public void onApplicationReady() {

            //On créer le rôle ADMIN en BDD
            try {
                Optional<Role> roleAdmin = roleRepository.findByLibelle(String.valueOf(TypeRole.ADMIN));
                if (roleAdmin.isEmpty()) {
                    Role role1 = Role.builder()
                            .libelle(String.valueOf(TypeRole.ADMIN))
                            .build();
                    roleRepository.save(role1);
                }
            } catch (Exception e) {
                log.info(String.valueOf(e));
            }

            //On créer le rôle USER en BDD
            try {
                Optional<Role> roleAdmin = roleRepository.findByLibelle(String.valueOf(TypeRole.USER));
                if (roleAdmin.isEmpty()) {
                    Role role2 = Role.builder()
                            .libelle(String.valueOf(TypeRole.USER))
                            .build();
                    roleRepository.save(role2);
                }
            } catch (Exception e) {
                log.info(String.valueOf(e));
            }

            //On créer le rôle AGENT en BDD
            try {
                Optional<Role> roleAdmin = roleRepository.findByLibelle(String.valueOf(TypeRole.AGENT));
                if (roleAdmin.isEmpty()) {
                    Role role3 = Role.builder()
                            .libelle(String.valueOf(TypeRole.AGENT))
                            .build();
                    roleRepository.save(role3);
                }
            } catch (Exception e) {
                log.info(String.valueOf(e));
            }

            //On créer un utilisateur ADMIN
            try {
                Optional<User> userAdmin = userRepository.findByEmail("admin@admin.com");
                if (userAdmin.isEmpty()) {
                    User user = User.builder()
                            .name("studi")
                            .username("admin")
                            .email("admin@admin.com")
                            .password(passwordEncoder.encode("1234"))
                            .active(true)
                            .secretKey(this.userService.generateAndEncryptKeyForDB())
                            .build();
                    userRepository.save(user);
                    userService.addRoleToUser("admin@admin.com", "ADMIN");
                    userService.addRoleToUser("admin@admin.com", "USER");
                    userService.addRoleToUser("admin@admin.com", "AGENT");
                    log.info("Compte admin créé");
                } else {
                    log.info("Compte Admin déja present");
                }
            } catch (Exception e) {
                log.info(String.valueOf(e));
            }
        }



}
