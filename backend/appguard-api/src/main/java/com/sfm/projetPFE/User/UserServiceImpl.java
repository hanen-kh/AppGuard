package com.sfm.projetPFE.User;

import com.sfm.projetPFE.Config.EmailService;
import com.sfm.projetPFE.Role.Role;
import com.sfm.projetPFE.Role.RoleRepository;
import com.sfm.projetPFE.Role.TypeRole;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@NoArgsConstructor
public class UserServiceImpl implements UserService , UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    @Override

    public User createUser(UserDTO userDTO) {
        // Check if the user already exists
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new RuntimeException("A user with this email already exists.");
        }


        // Generate a default password
        String generatedPassword = userDTO.getFirstname() + "&" + userDTO.getLastname() + "1";

        // Encrypt the password using BCrypt
        String encodedPassword = passwordEncoder.encode(generatedPassword);

        // Create a new user from the DTO
        User user = new User();
        user.setFirstname(userDTO.getFirstname());
        user.setLastname(userDTO.getLastname());
        user.setEmail(userDTO.getEmail());
        user.setPassword(encodedPassword);

        // Get the role by ID
        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found for ID: " + userDTO.getRoleId()));

        user.setRole(role); // Assign the role to the user

        // Send an email with the password
        String subject = "Your Password";
        String body = "Your password is: " + generatedPassword + ". \nPlease change your password as soon as possible.";
        emailService.sendSimpleEmail(user.getEmail(), subject, body);

        // Save the user in the database
        return userRepository.save(user);
    }



    @Override
    public User getUserById(int id) {
        // Récupérer l'utilisateur par son ID
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID:" + id));
    }

    @Override

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = new ArrayList<>();

        for (User user : users) {
            userDTOs.add(convertToDTO(user));
        }
        return userDTOs;
    }


    @Override
    public User updateUser(int id, User user) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));


        existingUser.setFirstname(user.getFirstname());
        existingUser.setLastname(user.getLastname());
        existingUser.setEmail(user.getEmail());

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(int id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));


        userRepository.delete(user);
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository
                .findByEmail(username)
                .orElseThrow(() -> new  UsernameNotFoundException("No user matches this ID"));
    }


@Override
public UserDTO convertToDTO(User user) {
    UserDTO userDTO = new UserDTO();
    userDTO.setId(user.getId());
    userDTO.setFirstname(user.getFirstname());
    userDTO.setLastname(user.getLastname());
    userDTO.setEmail(user.getEmail());

    // Récupérer l'ID du rôle et l'utiliser pour obtenir le type du rôle
    if (user.getRole() != null) {
        Role role = roleRepository.findById(user.getRole().getId())  // Trouver le rôle par ID
                .orElseThrow(() -> new RuntimeException("Role not found"));
        userDTO.setTypeRole(role.getType().name());
    } else {
        userDTO.setTypeRole("UNKNOWN");  // Si aucun rôle, on peut définir un type par défaut
    }

    return userDTO;
}

    @Override
    public List<SupervisorDto> getAllSupervisors() {
        List<User> supervisors = userRepository.findAllSupervisors();
        return supervisors.stream()
                .map(user -> new SupervisorDto(
                        (long) user.getId(),
                        user.getFirstname(),
                        user.getLastname(),
                        user.getSupervisedClients() != null ? user.getSupervisedClients().size() : 0
                ))
                .collect(Collectors.toList());
    }



}
