package com.sfm.projetPFE.Validation;

import com.sfm.projetPFE.Config.EmailService;
import com.sfm.projetPFE.AppGuardLogs.LogLevel;
import com.sfm.projetPFE.AppGuardLogs.LogServiceImpl;
import com.sfm.projetPFE.User.User;
import com.sfm.projetPFE.User.UserRepository;
import com.sfm.projetPFE.User.UserServiceImpl;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static java.time.temporal.ChronoUnit.MINUTES;
import java.time.Instant;
import java.util.Map;
import java.util.Random;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class ValidationServiceImpl implements ValidationService {

    private static final String SERVICE_EXISTS_KEY = "service:exists:"; // ex: service:exists:192.168.1.100:MySQL
    public static final String SERVICE_INFO_KEY = "service:info:";     // ex: service:info:192.168.1.100:MySQL
    private static final String PENDING_SERVICES_KEY = "service:pending";



    @Autowired
    private ValidationRepository validationRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    PasswordEncoder passwordEncoder;
@Autowired
private UserServiceImpl userServiceImpl;

@Autowired
private LogServiceImpl logService;

    @Override
    public void codeActivation(String username) {
        long start = System.currentTimeMillis();

        try {
            // Charger l'utilisateur
            User utilisateur = userServiceImpl.loadUserByUsername(username);

            if (utilisateur == null) {
                throw new UsernameNotFoundException("User not found with username: " + username);
            }

            Validation validation = new Validation();
            validation.setUser(utilisateur);

            // Définir les instants d'activation et d'expiration
            Instant activation = Instant.now();
            validation.setActivation(activation);
            Instant expiration = activation.plus(10, MINUTES);
            validation.setExpiration(expiration);

            // Générer un code de validation aléatoire à 6 chiffres
            Random random = new Random();
            String code = String.format("%06d", random.nextInt(999999));
            validation.setCode(code);

            // Enregistrer l'objet Validation
            validationRepository.save(validation);

            // Envoyer le mail
            String subject = "Your validation code";
            String body = "Your code : " + validation.getCode() + " — it expires in 10 minutes.";
            emailService.sendSimpleEmail(utilisateur.getEmail(), subject, body);

            // AppGuardLogs de succès
            logService.createLog(
                    utilisateur.getId(),
                    "validation-service",
                    "codeActivation()",
                    LogLevel.INFO,
                    start,
                    "Code sent successfully"
            );

        } catch (UsernameNotFoundException e) {
            // AppGuardLogs spécifique utilisateur non trouvé
            logService.createLog(
                    0,
                    "validation-service",
                    "codeActivation()",
                    LogLevel.WARN,
                    start,
                    e.getMessage()
            );
            throw e;

        } catch (Exception e) {
            // AppGuardLogs d'erreur inattendue
            logService.createLog(
                    0,
                    "validation-service",
                    "codeActivation()",
                    LogLevel.ERROR,
                    start,
                    e.getMessage()
            );
            throw e;
        }
    }





    @Override
    public void requestNewPassword(Map<String, String> parametres){
        //"email":"khmiletthanen@gmail.com"
        codeActivation(parametres.get("email"));}




    @Override
    public Map<String, String> validateCode(Map<String, String> parametres) {
        long start = System.currentTimeMillis(); // Démarrer le chronomètre pour calculer la latence
        try {
            String email = parametres.get("email");
            String code = parametres.get("code");

            Validation validation = validationRepository.findByCode(code);
            User utilisateur = validation.getUser();
            if (validation == null || Instant.now().isAfter(validation.getExpiration())) {
                // AppGuardLogs d'erreur si le code est invalide ou expiré
                logService.createLog(
                        utilisateur.getId(),                            // ID utilisateur inconnu
                        "validation-service",         // Nom du service
                        "validateCode()",             // Méthode
                        LogLevel.ERROR,               // Niveau d'erreur
                        start,                        // Temps de début
                        "Invalid or expired code"     // Message d'erreur
                );

                Map<String, String> response = Map.of("message", "Invalid Code");
                return ResponseEntity.badRequest().body(response).getBody();
            }


            if (!utilisateur.getEmail().equals(email)) {
                // AppGuardLogs d'erreur si l'email ne correspond pas
                logService.createLog(
                        utilisateur.getId(),
                        "validation-service",
                        "validateCode()",
                        LogLevel.ERROR,
                        start,
                        "Incorrect email"             // Message d'erreur
                );

                Map<String, String> response = Map.of("message", "Email incorrect");
                return ResponseEntity.badRequest().body(response).getBody();
            }

            // AppGuardLogs de succès si le code est valide
            logService.createLog(
                    utilisateur.getId(),
                    "validation-service",
                    "validateCode()",
                    LogLevel.INFO,
                    start,
                    "success"                          // Pas d’erreur (stackTrace)
            );

            Map<String, String> response = Map.of("message", "Valid code. You can change your password.");
            return ResponseEntity.ok().body(response).getBody();

        } catch (Exception e) {
            // AppGuardLogs d'erreur inattendue
            logService.createLog(
                    0,                            // ID utilisateur inconnu
                    "validation-service",
                    "validateCode()",
                    LogLevel.ERROR,
                    start,
                    e.getMessage()
            );
            throw e; // Propager l'exception
        }
    }



    @Override
    public void changePassword(Map<String, String> parametres) {
        long start = System.currentTimeMillis(); // Démarrer le chronomètre pour calculer la latence
        try {
            // Extraire les paramètres
            String email = parametres.get("email");
            String password = parametres.get("password");

            // Charger l'utilisateur par email
            User utilisateur = userServiceImpl.loadUserByUsername(email);
            if (utilisateur == null) {
                // AppGuardLogs d'erreur si l'utilisateur n'est pas trouvé
                logService.createLog(
                        0,                             // ID utilisateur inconnu
                        "user-service",                // Nom du service
                        "changePassword()",            // Méthode
                        LogLevel.ERROR,                // Niveau d'erreur
                        start,                         // Temps de début
                        "Unknown user with email: " + email // Message d'erreur
                );
                throw new RuntimeException("Unknown user ");
            }

            // Activer l'utilisateur s'il n'est pas actif
            if (!utilisateur.isActif()) {
                utilisateur.setActif(true);
                userRepository.save(utilisateur);

                // AppGuardLogs de succès pour l'activation de l'utilisateur
                logService.createLog(
                        utilisateur.getId(),          // ID utilisateur
                        "user-service",               // Nom du service
                        "changePassword()",           // Méthode
                        LogLevel.INFO,                // Niveau INFO
                        start,                        // Temps de début
                        "User activated"              // Message de succès
                );
            }

            // Mettre à jour le mot de passe si fourni
            if (password != null && !password.isEmpty()) {
                String hashedPassword = passwordEncoder.encode(password);
                utilisateur.setPassword(hashedPassword);
                userRepository.save(utilisateur);

                // AppGuardLogs de succès pour la mise à jour du mot de passe
                logService.createLog(
                        utilisateur.getId(),          // ID utilisateur
                        "user-service",               // Nom du service
                        "changePassword()",           // Méthode
                        LogLevel.INFO,                // Niveau INFO
                        start,                        // Temps de début
                        "Password updated successfully" // Message de succès
                );
            }

            // Optionnel: Suppression de la validation si nécessaire (à décommenter si utilisé)
            // validationRepository.delete(validation);

        } catch (Exception e) {
            // AppGuardLogs d'erreur inattendue
            logService.createLog(
                    0,                            // ID utilisateur inconnu
                    "user-service",
                    "changePassword()",
                    LogLevel.ERROR,
                    start,
                    e.getMessage()
            );
            throw e; // Propager l'exception
        }
    }







}
