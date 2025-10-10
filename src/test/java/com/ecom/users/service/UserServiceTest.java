package com.ecom.users.service;

import com.ecom.users.clients.OrdersRestClient;
import com.ecom.users.clients.ValidationRestClient;
import com.ecom.users.dto.*;
import com.ecom.users.entity.Role;
import com.ecom.users.entity.User;
import com.ecom.users.model.Validation;
import com.ecom.users.repository.RoleRepository;
import com.ecom.users.repository.UserRepository;
import com.ecom.users.response.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


public class UserServiceTest {

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

    @InjectMocks
    private UserService userService;

    private User user;
    private Role role;


    @BeforeEach
    void setUp() {
        // Initialisation des mocks
        MockitoAnnotations.openMocks(this);

        // Création d'un utilisateur
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("1234");
        user.setName("John");
        user.setUsername("john");
        user.setActive(false);

        // Création d'un rôle
        role = new Role();
        role.setId(1);
        role.setLibelle("USER");

        // Ajout un rôle USER à l'utilisateur
        user.getRoles().add(role);
    }


    // 1 - Inscription utilisateur valide
    @Test
    void registration_shouldCreateUserSuccessfully() throws Exception {
        // on simule que l'email n'existe pas encore dans la base
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        // on simule la récupération du rôle USER
        when(roleRepository.findByLibelle("USER")).thenReturn(Optional.of(role));
        // on simule l'encodage du mot de passe
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPwd");
        // on simule la récupération du token technique
        when(tokenTechnicService.getTechnicalToken()).thenReturn("token-technique");
        // on simule la validation de l'utilisateur par le service validation
        Validation validation = new Validation();
        validation.setId(999L);
        when(validationRestClient.sendValidation(anyString(), any(ValidationDto.class))).thenReturn(validation);
        // on simule l'enregistrement de l'utilisateur avec un ID défini
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        // on simule la création d'une commande dans Orders
        when(ordersRestClient.createOrder(anyString(), any())).thenReturn(ResponseEntity.ok().build());
        // Appel de la méthode testée
        ResponseEntity<?> response = userService.registration(user);

        // Vérification, status HTTP + body
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        // Vérification, méthodes appelées = ok
        verify(userRepository).save(any(User.class));
        verify(validationRestClient).sendValidation(anyString(), any(ValidationDto.class));
        verify(ordersRestClient).createOrder(anyString(), any());
    }

    // 2 - Inscription avec email invalide
    @Test
    void registration_shouldThrow_whenEmailWithoutAt() {
        // Email invalide
        user.setEmail("invalid.email");
        // Vérification, exception UserNotFoundException levée avec message
        assertThatThrownBy(() -> userService.registration(user))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("format email est invalide");
    }

    // 3 - Inscription avec email invalide (sans .)
    @Test
    void registration_shouldThrow_whenEmailWithoutDot() {
        // Email invalide
        user.setEmail("invalid@email");
        // Vérification, exception UserNotFoundException levée avec message
        assertThatThrownBy(() -> userService.registration(user))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("format email est invalide");
    }

    // 4 - Inscription avec email déjà existant
    @Test
    void registration_shouldThrow_whenEmailExists() {
        // on simule que l'utilisateur existe déjà
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        // Vérification, exception UserNotFoundException levée avec message
        assertThatThrownBy(() -> userService.registration(user))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Cette email existe déjà!");
    }

    // 5 - Inscription avec rôle introuvable
    @Test
    void registration_shouldThrow_whenRoleNotFound() {
        // Email inexistant
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        // Rôle introuvable
        when(roleRepository.findByLibelle("USER")).thenReturn(Optional.empty());
        // Vérification, exception UserNotFoundException levée avec message
        assertThatThrownBy(() -> userService.registration(user))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Role introuvable");
    }

    // 6 - Inscription avec validation KO
    @Test
    void registration_shouldThrow_whenValidationFails() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByLibelle("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPwd");
        when(tokenTechnicService.getTechnicalToken()).thenReturn("token-technique");
        // on simule save pour que l'ID soit défini
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        // Validation avec retour null
        Validation validation = new Validation();
        validation.setId(null);
        when(validationRestClient.sendValidation(anyString(), any(ValidationDto.class))).thenReturn(validation);
        // Vérification, exception UserNotFoundException levée avec message
        assertThatThrownBy(() -> userService.registration(user))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Service indisponible");
    }

    // 7 - Ajouter un rôle à un utilisateur existant
    @Test
    void addRoleToUser_shouldAddRole_whenUserAndRoleExist() {
        // on simule que l'utilisateur existe
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        // on simule que le rôle ADMIN existe
        when(roleRepository.findByLibelle("ADMIN")).thenReturn(Optional.of(role));
        // on simule l'enregistrement de l'utilisateur
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Appel de la méthode
        userService.addRoleToUser(user.getEmail(), "ADMIN");

        // Vérification, le rôle a bien été ajouté
        assertTrue(user.getRoles().contains(role));
        // Vérification, utilisateur sauvegardé
        verify(userRepository, times(1)).save(user);
    }


    // 8 - Ajouter un rôle à un utilisateur inexistant
    @Test
    void addRoleToUser_shouldThrow_whenUserNotFound() {
        // on simule que l'utilisateur n'existe pas
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        // Vérification, exception UserNotFoundException levée avec message
        assertThatThrownBy(() -> userService.addRoleToUser(user.getEmail(), "ADMIN"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Utilisateur introuvable");
    }


    // 9 - Ajouter un rôle inexistant
    @Test
    void addRoleToUser_shouldThrow_whenRoleNotFound() {
        // on simule que l'utilisateur existe
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        // on simule que le rôle n'existe pas
        when(roleRepository.findByLibelle("ADMIN")).thenReturn(Optional.empty());

        // Vérification, exception UserNotFoundException levée avec message
        assertThatThrownBy(() -> userService.addRoleToUser(user.getEmail(), "ADMIN"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Role introuvable");
    }


    // 10 - Activer un utilisateur existant
    @Test
    void activationUser_shouldSetActiveTrue_whenUserExists() {
        // Création d'un DTO pour l'activation
        UserActivationDto dto = new UserActivationDto();
        dto.setUserId(user.getId());
        // on simule que l'utilisateur existe
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Appel de la méthode testée
        userService.activationUser(dto);

        // Vérification, utilisateur actif
        assertTrue(user.getActive());
        // Vérification, utilisateur sauvegardé
        verify(userRepository, times(1)).save(user);
    }


    // 11 - Activer un utilisateur inexistant
    @Test
    void activationUser_shouldDoNothing_whenUserNotFound() {
        // Création d'un DTO pour un utilisateur inexistant
        UserActivationDto dto = new UserActivationDto();
        dto.setUserId(99L);
        // on simule que l'utilisateur n'existe pas
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Appel de la méthode testée (ne doit rien faire)
        userService.activationUser(dto);

        // Vérification, aucune sauvegarde
        verify(userRepository, never()).save(any(User.class));
    }


    // 12 - Rechercher un utilisateur par ID existant
    @Test
    void findById_shouldReturnUser_whenUserExists() {
        // on simule que l'utilisateur existe
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // Appel de la méthode testée
        User result = userService.findById(user.getId());

        // Vérification, utilisateur existe
        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
    }


    // 13 - Rechercher un utilisateur par ID inexistant
    @Test
    void findById_shouldReturnNull_whenUserNotFound() {
        // on simule que l'utilisateur n'existe pas
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Appel de la méthode testée
        User result = userService.findById(99L);

        // Vérification, résultat null
        assertNull(result);
    }


    // 14 - Rechercher un utilisateur par email existant
    @Test
    void findByEmail_shouldReturnUserDto_whenUserExists() {
        // on simule que l'utilisateur existe
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Appel de la méthode testée
        UserDto result = userService.userByEmail(user.getEmail());

        // Vérification, champs retournés ok
        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getName(), result.getName());
    }


    // 15 - Rechercher un utilisateur par email inexistant
    @Test
    void findByEmail_shouldReturnNull_whenUserNotFound() {
        // on simule que l'utilisateur n'existe pas
        when(userRepository.findByEmail("inexistant@example.com")).thenReturn(Optional.empty());

        // Appel de la méthode testée
        User result = userService.findByEmail("inexistant@example.com");

        // Vérification, résultat null
        assertNull(result);
    }


    // 16 - Récupérer tous les utilisateurs
    @Test
    void findAll_shouldReturnListOfUserDto() {
        // Création utilisateur 2
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setName("User Two");
        user2.setUsername("user2");
        user2.setActive(true);
        user2.getRoles().add(role);

        // on simule que le repository renvoie une liste avec 2 utilisateurs
        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        // Appel de la méthode
        List<UserDto> result = userService.findAll();

        // Vérification, longueur de la liste ok
        assertEquals(2, result.size());
        // Vérification que les deux utilisateurs sont bien présents
        assertTrue(result.stream().anyMatch(u -> "test@example.com".equals(u.getEmail())));
        assertTrue(result.stream().anyMatch(u -> "user2@example.com".equals(u.getEmail())));
    }


    // 17 - Supprimer un utilisateur existant
    @Test
    void removeUser_shouldDeleteUser_whenUserExists() {
        // on simule que l'utilisateur existe
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        // on simule la suppression sans erreur
        doNothing().when(userRepository).delete(user);

        // Appel de la méthode testée
        ResponseEntity<?> response = userService.removeUser(user.getEmail());

        // Vérification, message succès
        assertEquals("Le compte est supprimé", response.getBody());
        // Vérification, méthode delete appelée
        verify(userRepository, times(1)).delete(user);
    }

    // 18 - Supprimer un utilisateur inexistant
    @Test
    void removeUser_shouldThrow_whenUserNotFound() {
        // on simule que l'utilisateur n'existe pas
        when(userRepository.findByEmail("inexistant@example.com")).thenReturn(Optional.empty());

        // Vérification, exception UserNotFoundException levée avec message
        assertThatThrownBy(() -> userService.removeUser("inexistant@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("utilisateur introuvable");
    }


    // 19 - Éditer le mot de passe (demande de validation OK)
    @Test
    void editPassword_shouldReturnValidation_whenUserExists() {
        // on simule que l'utilisateur existe
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        // on simule que le service de validation renvoie un ID valide
        Validation validation = new Validation();
        validation.setId(100L);
        when(validationRestClient.sendValidation(anyString(), any(ValidationDto.class))).thenReturn(validation);
        when(tokenTechnicService.getTechnicalToken()).thenReturn("token-technique");

        // Préparer le DTO
        EditPasswordDto dto = new EditPasswordDto();
        dto.setEmail(user.getEmail());

        // Appel de la méthode
        ResponseEntity<?> response = userService.editPassword(dto);

        // Vérification, appel service validation ok
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("id"));
        verify(validationRestClient, times(1)).sendValidation(anyString(), any(ValidationDto.class));
    }


    // 20 - Éditer le mot de passe (service validation KO)
    @Test
    void editPassword_shouldThrow_whenValidationFails() {
        // on simule que l'utilisateur existe
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        // on simule que le service de validation renvoie un ID null
        when(validationRestClient.sendValidation(anyString(), any(ValidationDto.class))).thenReturn(new Validation());
        when(tokenTechnicService.getTechnicalToken()).thenReturn("token-technique");

        EditPasswordDto dto = new EditPasswordDto();
        dto.setEmail(user.getEmail());

        // Vérification, exception UserNotFoundException levée avec message
        assertThatThrownBy(() -> userService.editPassword(dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Service indisponible");
    }

    // 21 - Nouveau mot de passe après validation
    @Test
    void newPassword_shouldUpdatePassword_whenUserExists_andValidationTrue() {
        // on simule que l'utilisateur existe
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        // on simule l'encodage du nouveau mot de passe
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPwd");
        // on simule l'enregistrement
        when(userRepository.save(any(User.class))).thenReturn(user);

        // on prépare le DTO
        NewPasswordDto dto = new NewPasswordDto();
        dto.setUserId(user.getId());
        dto.setPassword("newPassword");

        // Appel de la méthode testée
        ResponseEntity<?> response = userService.newPassword(dto);

        // Vérification, retour succès
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Votre mot de passe a été modifié avec succès !", response.getBody());
        verify(userRepository, times(1)).save(user);
    }

    // 22 - Mise à jour des informations utilisateur
    @Test
    void updateUser_shouldUpdateInfo_whenUserExists() {
        // on simule que l'utilisateur existe
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Préparer un DTO
        UserDto dto = new UserDto(user);
        dto.setName("New Name");
        dto.setUsername("newUsername");

        // Appel de la méthode testée
        ResponseEntity<?> response = userService.updateUser(dto);

        // Vérification, retour succès
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Votre compte a été modifié avec succès !", response.getBody());
        assertEquals("New Name", user.getName());
        assertEquals("newUsername", user.getUsername());
        verify(userRepository, times(1)).save(user);
    }


    // 23 - Rechercher un utilisateur par email et obtenir UserDto
    @Test
    void userByEmail_shouldReturnUserDto_whenUserExists() {
        // on simule que l'utilisateur existe
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Appel de la méthode testée
        UserDto result = userService.userByEmail(user.getEmail());

        // Vérification,  champs du DTO retourné
        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getRoles().size(), result.getRoles().size());
    }

    // 24 - editPassword avec email valide
    @Test
    void editPassword_shouldSendValidation_whenEmailExists() {
        // on simule que l'utilisateur existe
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        // on simule une validation avec ID valide
        Validation validation = new Validation();
        validation.setId(123L);
        when(validationRestClient.sendValidation(anyString(), any(ValidationDto.class)))
                .thenReturn(validation);
        when(tokenTechnicService.getTechnicalToken()).thenReturn("token-technique");

        EditPasswordDto dto = new EditPasswordDto();
        dto.setEmail(user.getEmail());

        // Appel de la méthode testée
        ResponseEntity<?> response = userService.editPassword(dto);

        // Vérification, appel service validation
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertThat(response.getBody()).isInstanceOf(Map.class);
        verify(validationRestClient).sendValidation(anyString(), any(ValidationDto.class));
    }

    // 25 - editPassword avec email inexistant
    @Test
    void editPassword_shouldThrow_whenEmailNotFound() {
        // on simule que l'utilisateur n'existe pas
        when(userRepository.findByEmail("inexistant@example.com")).thenReturn(Optional.empty());

        EditPasswordDto dto = new EditPasswordDto();
        dto.setEmail("inexistant@example.com");

        // Vérification, exception UserNotFoundException levée avec message
        assertThatThrownBy(() -> userService.editPassword(dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Utilisateur introuvable");
    }


    // 26 - newPassword avec utilisateur existant
    @Test
    void newPassword_shouldUpdatePassword_whenUserExists() {
        // on simule que l'utilisateur existe
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        // on simule l'encodage
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Préparer le DTO
        NewPasswordDto dto = new NewPasswordDto();
        dto.setUserId(user.getId());
        dto.setPassword("newPass");

        // Appel de la méthode testée
        ResponseEntity<?> response = userService.newPassword(dto);

        // Vérification, retour succès
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Votre mot de passe a été modifié avec succès !", response.getBody());
        assertEquals("encodedNewPass", user.getPassword());
        verify(userRepository).save(user);
    }

    // 27 - newPassword avec utilisateur inexistant
    @Test
    void newPassword_shouldThrow_whenUserNotFound() {
        // on simule que l'utilisateur n'existe pas
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NewPasswordDto dto = new NewPasswordDto();
        dto.setUserId(99L);
        dto.setPassword("newPass");

        // Vérification, exception UserNotFoundException levée avec message
        assertThatThrownBy(() -> userService.newPassword(dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Utilisateur introuvable");
    }

}

