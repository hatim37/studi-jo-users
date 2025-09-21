package com.ecom.users;

import com.ecom.users.config.RsakeysConfig;
import com.ecom.users.entity.Role;
import com.ecom.users.entity.User;
import com.ecom.users.enums.TypeRole;
import com.ecom.users.repository.RoleRepository;
import com.ecom.users.repository.UserRepository;
import com.ecom.users.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Slf4j
@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(RsakeysConfig.class)
public class UsersApplication {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UsersApplication(RoleRepository roleRepository, UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }


    public static void main(String[] args) {
        SpringApplication.run(UsersApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> {

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
                    userService.addRoleToUser("admin@admin.com", "USER");
                    userService.addRoleToUser("admin@admin.com", "ADMIN");
                } else {
                    log.info("Compte Admin déja present");
                }
            } catch (Exception e) {
                log.info(String.valueOf(e));
            }


        };
    }

}
