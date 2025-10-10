package com.ecom.users.service;

import com.ecom.users.clients.OrdersRestClient;
import com.ecom.users.clients.ValidationRestClient;
import com.ecom.users.dto.NewPasswordDto;
import com.ecom.users.dto.UserActivationDto;
import com.ecom.users.entity.Role;
import com.ecom.users.entity.User;
import com.ecom.users.model.Order;
import com.ecom.users.model.Validation;
import com.ecom.users.repository.RoleRepository;
import com.ecom.users.repository.UserRepository;
import com.ecom.users.response.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserIntegrationTest {
    
    @InjectMocks
    private UserService userService;
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenTechnicService tokenTechnicService;

    @Mock
    private OrdersRestClient ordersRestClient;

    @Mock
    private ValidationRestClient validationRestClient;
    
    private Role userRole;
    private User user;

    @BeforeEach
    void setUp() {
        // Création d'un rôle USER
        userRole = new Role();
        userRole.setId(1);
        userRole.setLibelle("USER");

        // Création d'un utilisateur
        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setUsername("john123");
        user.setEmail("john@example.com");
        user.setPassword("1234");
        user.setActive(false); 
        user.setRoles(new HashSet<>(Set.of(userRole)));
    }

    // 1 : Test d'inscription réussie
    @Test
    void testRegistrationSuccess() throws Exception {
        // on simule aucun utilisateur n'existe avec cet email
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        // on simule la récupération du rôle USER depuis la base
        when(roleRepository.findByLibelle("USER")).thenReturn(Optional.of(userRole));
        // on simule l'encodage du mot de passe
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        // on simule la récupération d'un token technique
        when(tokenTechnicService.getTechnicalToken()).thenReturn("mocked-token");
        // on simule l'envoi d'un code de validation
        when(validationRestClient.sendValidation(anyString(), any()))
                .thenReturn(new Validation(100L));
        // on simule la création d'une commande via le service Orders
        when(ordersRestClient.createOrder(anyString(), any()))
                .thenReturn(ResponseEntity.ok(new Order(1L, null, null, 1L)));
        // on simule la sauvegarde de l'utilisateur et retourne l'objet sauvegardé
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        
        // Appel de la méthode
        ResponseEntity<?> response = userService.registration(user);

        // vérification, statut HTTP + rôle USER + save() + envoi validation + création commande
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(user.getRoles().contains(userRole));
        verify(userRepository, times(1)).save(any(User.class));
        verify(validationRestClient, times(1)).sendValidation(anyString(), any());
        verify(ordersRestClient, times(1)).createOrder(anyString(), any());
    }

    // 2 : Test inscription avec email déjà existant
    @Test
    void testRegistrationEmailAlreadyExists() {
        // on simule qu'un utilisateur existe déjà avec cet email
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Vérification, exception levéé avec message
        Exception exception = assertThrows(UserNotFoundException.class, () -> {userService.registration(user);});
        assertTrue(exception.getMessage().contains("existe déjà"));

        // Vérification,  save() non appelé
        verify(userRepository, never()).save(any(User.class));
    }

    // 3 : Test ajout de rôle réussi
    @Test
    void testAddRoleToUserSuccess() {
        // on simule récupération de l'utilisateur depuis la base
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        // on simule récupération du rôle USER
        when(roleRepository.findByLibelle("USER")).thenReturn(Optional.of(userRole));
        // on simule sauvegarde de l'utilisateur
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        // Appel du service pour ajouter le rôle
        userService.addRoleToUser(user.getEmail(), "USER");

        // Vérification, rôle ajouté
        assertTrue(user.getRoles().contains(userRole));
        verify(userRepository, times(1)).save(user); // Vérification,  sauvegarde
    }

    // 4: Test ajout de rôle avec rôle inexistant
    @Test
    void testAddRoleToUser_RoleNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(roleRepository.findByLibelle("ADMIN")).thenReturn(Optional.empty());

        // Vérification, exception levéé avec message si rôle n'existe pas
        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.addRoleToUser(user.getEmail(), "ADMIN");
        });
        assertTrue(exception.getMessage().contains("Role introuvable"));
    }

    // 5 : Test activation utilisateur réussie
    @Test
    void testActivateUserSuccess() {
        // on simule récupération de l'utilisateur
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        // on simule sauvegarde après activation
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        // Création du DTO
        UserActivationDto dto = new UserActivationDto();
        dto.setUserId(user.getId());

        // Appel du service
        userService.activationUser(dto);

        // Vérification, utilisateur actif
        assertTrue(user.getActive());
        verify(userRepository, times(1)).save(user); // Vérification,  sauvegarde
    }

    // 6 : Test activation utilisateur inexistant
    @Test
    void testActivateUserNotFound() {
        // on simule qu'aucun utilisateur n'existe
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserActivationDto dto = new UserActivationDto();
        dto.setUserId(999L);

        // Appel du service
        userService.activationUser(dto);

        // Vérification, save() non appelé
        verify(userRepository, never()).save(any(User.class));
    }

    // 7 : Test modification de mot de passe réussie
    @Test
    void testNewPasswordSuccess() {
        // on simule récupération de l'utilisateur
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        // on simule encodage du nouveau mot de passe
        when(passwordEncoder.encode("newPass123")).thenReturn("encoded-newPass");
        // on simule sauvegarde
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Création du DTO
        NewPasswordDto dto = new NewPasswordDto();
        dto.setUserId(user.getId());
        dto.setPassword("newPass123");

        // Appel du service
        userService.newPassword(dto);

        // Vérification, mot de passe modifié
        assertEquals("encoded-newPass", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    // 8 : Test modification mot de passe utilisateur inexistant
    @Test
    void testNewPasswordUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NewPasswordDto dto = new NewPasswordDto();
        dto.setUserId(999L);
        dto.setPassword("newPass123");

        // Vérification, exception levéé avec message
        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.newPassword(dto);
        });

        assertTrue(exception.getMessage().contains("Utilisateur introuvable"));
    }
}

