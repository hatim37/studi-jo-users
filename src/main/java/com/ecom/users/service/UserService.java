package com.ecom.users.service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import com.ecom.users.entity.Role;
import com.ecom.users.entity.User;
import com.ecom.users.repository.RoleRepository;
import com.ecom.users.repository.UserRepository;
import com.ecom.users.response.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> registration(User user) throws NoSuchAlgorithmException {
        //Verification sur l'email saisi
        if(!user.getEmail().contains("@")){
            throw new UserNotFoundException("le format email est invalide");
        } if (!user.getEmail().contains(".")){
            throw new UserNotFoundException("le format email est invalide");
        }
        Optional<User> utilisateurOptional= this.userRepository.findByEmail(user.getEmail());
        if(utilisateurOptional.isPresent()){
            throw new UserNotFoundException("Cette email existe déjà!");
        }
        //Ajout d'un role USER à l'inscription
        Role userRole = roleRepository.findByLibelle("USER").orElseThrow(() -> new UserNotFoundException("Role introuvable"));
        Set<Role> authorities = new HashSet<>();
        authorities.add(userRole);
        user.setRoles(authorities);
        //ajoute le nom
        user.setName(user.getName());
        //ajouter le prenom
        user.setUsername(user.getUsername());
        //ajoute le secretKey
        user.setSecretKey(this.generateAndEncryptKeyForDB());
        //j'encode le mot de passe
        String passwordCrypt = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(passwordCrypt);
        //on sauvegarde
        user = this.userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("Utilisateur créé avec succès !");
    }

    public String generateAndEncryptKeyForDB() throws NoSuchAlgorithmException {
        // Génère une clé AES 256 bits
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();

        // Encode en Base64 pour stockage en BDD
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }


}
