package com.sfm.projetPFE.User;

import com.sfm.projetPFE.Jwt.Authentification;
import com.sfm.projetPFE.Jwt.JwtService;
import com.sfm.projetPFE.AppGuardLogs.LogLevel;
import com.sfm.projetPFE.AppGuardLogs.LogService;
import com.sfm.projetPFE.Validation.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {



    @Autowired
    private UserServiceImpl userService;

   @Autowired
   private AuthenticationManager authenticationManager;
   @Autowired
   private JwtService jwtService;

   @Autowired
   private ValidationService validationService;
@Autowired
private PasswordEncoder passwordEncoder;

    @Autowired
    private LogService logService;

    @Autowired
    private RestTemplate restTemplate;







    @PostMapping("/refresh")
    public Map<String, String> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid  refresh token  !");
        }
        String username = jwtService.extractUsername(refreshToken);
        return jwtService.generate(username);
    }


    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Authentification authentification) {

        long start = System.currentTimeMillis();
        try {
            User utilisateur = userService.loadUserByUsername(authentification.username());

            if (!passwordEncoder.matches(authentification.password(), utilisateur.getPassword())) {
                logService.createLog(
                        utilisateur.getId(),
                        "auth-service",
                        "login()",
                        LogLevel.WARN,
                        start,
                        "Incorrect password"
                );
                throw new BadCredentialsException("Incorrect Password !!!");
            }

            // SUCCESSFUL login
            logService.createLog(
                    utilisateur.getId(),
                    "auth-service",
                    "login()",
                    LogLevel.INFO,
                    start,
                    null
            );

            return jwtService.generate(utilisateur.getUsername());

        } catch (Exception e) {
            logService.createLog(
                    0, // si le user n'existe pas
                    "auth-service",
                    "login()",
                    LogLevel.ERROR,
                    start,
                    e.getMessage()
            );
            throw e;
        }
    }



    @PostMapping("/logout")
    public void logout(){
        jwtService.deconnexion();
    }


    @PostMapping(path="/requestNewPassword")
    public ResponseEntity<String> requestNewPassword(@RequestBody Map<String, String> parametres){
        validationService.requestNewPassword(parametres);
        return ResponseEntity.status(HttpStatus.OK).body("Your code is sent by email");
    }

    @PostMapping("/validate-code")
    public ResponseEntity<Map<String, String>> validateCode(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(validationService.validateCode(request));
    }

    @PostMapping("/change-password")
    public  ResponseEntity<String> changePassword(@RequestBody Map<String, String> request) {
        validationService.changePassword(request);
        return ResponseEntity.status(HttpStatus.OK).body("Password changed successfully ");
    }


    @PostMapping("/create_user")
 @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody UserDTO user) {
        long start = System.currentTimeMillis(); // Démarre le chronomètre pour suivre la latence
        try {
            // AppGuardLogs de début de la création d'un utilisateur
            logService.createLog(
                    0,                             // ID utilisateur inconnu (administrateur en cours)
                    "user-service",
                    "createUser()",
                    LogLevel.INFO,
                    start,
                    "Creating user with email: " + user.getEmail() // Message informatif
            );

            // Appeler le service pour créer un utilisateur
            User savedUser = userService.createUser(user);

            // AppGuardLogs de succès après création de l'utilisateur
            logService.createLog(
                    savedUser.getId(),             // ID utilisateur créé
                    "user-service",
                    "createUser()",
                    LogLevel.INFO,
                    start,
                    "User created successfully"    // Message de succès
            );

            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);

        } catch (NoSuchElementException e) {
            // AppGuardLogs d'erreur pour ID de rôle non valide
            logService.createLog(
                    0,                             // ID utilisateur inconnu
                    "user-service",
                    "createUser()",
                    LogLevel.ERROR,
                    start,
                    "Role ID not found: " + e.getMessage()
            );

            // Si l'ID du rôle est invalide
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Role ID not found");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

        } catch (RuntimeException e) {
            // AppGuardLogs d'erreur si l'email est déjà utilisé ou une autre erreur
            logService.createLog(
                    0,                             // ID utilisateur inconnu
                    "user-service",
                    "createUser()",
                    LogLevel.ERROR,
                    start,
                    "Error occurred: " + e.getMessage()
            );

            // 🔹 Si l'email est déjà utilisé ou autre erreur
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage()); // Message de l'exception
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        try {
            User user = userService.getUserById(id);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/all_users")
   // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        long start = System.currentTimeMillis(); // Démarre le chronomètre pour suivre la latence
        try {
            // AppGuardLogs de début de la récupération des utilisateurs
            logService.createLog(
                    0,                             // ID utilisateur inconnu (administrateur en cours)
                    "user-service",                // Nom du service
                    "getAllUsers()",               // Méthode
                    LogLevel.INFO,                 // Niveau INFO
                    start,                         // Temps de début
                    "Fetching all users"          // Message informatif
            );

            // Récupérer tous les utilisateurs
            List<UserDTO> users = userService.getAllUsers();

            // AppGuardLogs de succès après récupération des utilisateurs
            logService.createLog(
                    0,                             // ID utilisateur inconnu
                    "user-service",                // Nom du service
                    "getAllUsers()",               // Méthode
                    LogLevel.INFO,                 // Niveau INFO
                    start,                         // Temps de début
                    "Successfully fetched all users" // Message de succès
            );

            // Retourner la réponse avec la liste des utilisateurs
            return new ResponseEntity<>(users, HttpStatus.OK);

        } catch (Exception e) {
            // AppGuardLogs d'erreur en cas d'exception
            logService.createLog(
                    0,                             // ID utilisateur inconnu
                    "user-service",                // Nom du service
                    "getAllUsers()",               // Méthode
                    LogLevel.ERROR,                // Niveau d'erreur
                    start,                         // Temps de début
                    "Error fetching users: " + e.getMessage() // Message d'erreur
            );

            // Créer une liste vide de UserDTO en cas d'erreur
            List<UserDTO> emptyList = new ArrayList<>();


            return new ResponseEntity<>(emptyList, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable int id, @RequestBody User user) {
        long start = System.currentTimeMillis(); // Démarre le chronomètre pour suivre la latence
        try {
            // AppGuardLogs de début de la mise à jour de l'utilisateur
            logService.createLog(
                    0,                             // ID utilisateur inconnu
                    "user-service",                // Nom du service
                    "updateUser()",                // Méthode
                    LogLevel.INFO,                 // Niveau INFO
                    start,                         // Temps de début
                    "Updating user with ID: " + id // Message informatif
            );

            // Appeler le service pour mettre à jour l'utilisateur
            User updatedUser = userService.updateUser(id, user);

            // AppGuardLogs de succès après la mise à jour de l'utilisateur
            logService.createLog(
                    0,                             // ID utilisateur inconnu
                    "user-service",                // Nom du service
                    "updateUser()",                // Méthode
                    LogLevel.INFO,                 // Niveau INFO
                    start,                         // Temps de début
                    "Successfully updated user with ID: " + id // Message de succès
            );

            // Retourner la réponse avec l'utilisateur mis à jour
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);

        } catch (RuntimeException e) {
            // AppGuardLogs d'erreur si l'utilisateur n'est pas trouvé ou si une autre erreur survient
            logService.createLog(
                    0,                             // ID utilisateur inconnu
                    "user-service",                // Nom du service
                    "updateUser()",                // Méthode
                    LogLevel.ERROR,                // Niveau d'erreur
                    start,                         // Temps de début
                    "Error updating user with ID: " + id + " - " + e.getMessage() // Message d'erreur
            );

            // Retourner une réponse d'erreur avec le statut NOT_FOUND
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        long start = System.currentTimeMillis(); // Pour mesurer la latence

        try {
            // Tentative de suppression
            userService.deleteUser(id);

            // AppGuardLogs de succès
            logService.createLog(
                    0,                            // ID utilisateur inconnu ou admin
                    "user-service",              // Nom du service
                    "deleteUser()",
                    LogLevel.INFO,
                    start,
                    "Successfully deleted user with ID: " + id // Message
            );

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (RuntimeException e) {
            // AppGuardLogs d'erreur
            logService.createLog(
                    0,                            // ID utilisateur inconnu ou admin
                    "user-service",
                    "deleteUser()",
                    LogLevel.ERROR,
                    start,
                    "Failed to delete user with ID: " + id + " - " + e.getMessage()
            );

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }




    @GetMapping("/supervisors")

    public ResponseEntity<List<SupervisorDto>> getAllSupervisors() {
        List<SupervisorDto> supervisors = userService.getAllSupervisors();
        return ResponseEntity.ok(supervisors);
    }


}
