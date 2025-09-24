package com.ecom.users.service;


import com.ecom.users.dto.UpdateRolesDto;
import com.ecom.users.entity.Role;
import com.ecom.users.entity.User;
import com.ecom.users.repository.RoleRepository;
import com.ecom.users.repository.UserRepository;
import com.ecom.users.response.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AdminService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public ResponseEntity<?> adminAddRoleUsers(Long userId, UpdateRolesDto roles) {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("Utilisateur introuvable"));
        user.getRoles().clear();
        userRepository.save(user);
        try {
            for (String roleName : roles.getRoleNames()) {
                Role addRole = roleRepository.findByLibelle(roleName).orElseThrow(()-> new UserNotFoundException("Role introuvable"));
                user.getRoles().add(addRole);
                userRepository.save(user);
            }
            return ResponseEntity.ok("Mise à jour effectuée !");
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Erreur lors du changement de role");
        }
    }
}
