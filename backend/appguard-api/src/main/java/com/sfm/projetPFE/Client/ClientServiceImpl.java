package com.sfm.projetPFE.Client;


import com.sfm.projetPFE.Server.Server;
import com.sfm.projetPFE.User.SupervisorDto;
import com.sfm.projetPFE.User.User;
import com.sfm.projetPFE.User.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@NoArgsConstructor
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;
   @Autowired
   private UserRepository userRepository;


    private static final String CLIENT_EXISTS_KEY = "client:exists:"; // client:exists:khmilet
    private static final String CLIENT_INFO_KEY = "client:info:";     // client:info:khmilet → ID
    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Override
    public List<ClientDTO> getClientsOfCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<Client> clients = isAdmin
                ? clientRepository.findAll()
                : clientRepository.findBySupervisors_Email(email);

        // Transformation en DTO avec le nombre de superviseurs
        return clients.stream()
                .map(client -> new ClientDTO(
                        client.getName(),
                        client.getContactEmail(),
                        client.getDescription(),
                        client.getSupervisors().size() // <- ici
                ))
                .toList();
    }





    @Transactional
    @Override
    public void createClient(ClientDTO clientDto) {
        // Valider le DTO
        if (clientDto == null) {
            throw new RuntimeException("Client data cannot be null");
        }
        if (clientDto.getName() == null || clientDto.getName().isBlank()) {
            throw new RuntimeException("Client name cannot be empty");
        }
        if (clientRepository.findByName(clientDto.getName()).isPresent()) {
            throw new RuntimeException("Client with name " + clientDto.getName() + " already exists");
        }

        // Créer l'entité Client
        Client client = new Client();
        client.setName(clientDto.getName());
        client.setContactEmail(clientDto.getContactEmail());
        client.setDescription(clientDto.getDescription());



        // Vérifier et assigner les superviseurs
        if (clientDto.getSupervisorIds() != null && !clientDto.getSupervisorIds().isEmpty()) {
            Set<User> supervisors = new HashSet<>();
            for (Long supervisorId : clientDto.getSupervisorIds()) {
                User supervisor = userRepository.findById(Math.toIntExact(supervisorId))
                        .orElseThrow(() -> new RuntimeException("User with ID " + supervisorId + " not found"));
                supervisors.add(supervisor);
            }
            client.setSupervisors(supervisors);
        }

        // Sauvegarder le client
       clientRepository.save(client);
    }



    @Transactional
    @Override
    public void updateClient(Long id, ClientDTO clientDto) {
        // Récupérer le client existant
        Client client = clientRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new RuntimeException("Client with ID " + id + " not found"));

        // Valider le DTO
        if (clientDto == null) {
            throw new RuntimeException("Client data cannot be null");
        }
        if (clientDto.getName() == null || clientDto.getName().isBlank()) {
            throw new RuntimeException("Client name cannot be empty");
        }
        // Vérifier l'unicité du nom (sauf si inchangé)
        if (!clientDto.getName().equals(client.getName()) && clientRepository.findByName(clientDto.getName()).isPresent()) {
            throw new RuntimeException("Client with name " + clientDto.getName() + " already exists");
        }

        // Mettre à jour les champs
        client.setName(clientDto.getName());
        client.setContactEmail(clientDto.getContactEmail());
        client.setDescription(clientDto.getDescription());



        // Mettre à jour les superviseurs
        if (clientDto.getSupervisorIds() != null) {
            Set<User> supervisors = new HashSet<>();
            if (!clientDto.getSupervisorIds().isEmpty()) {
                for (Long supervisorId : clientDto.getSupervisorIds()) {
                    User supervisor = userRepository.findById(Math.toIntExact(supervisorId))
                            .orElseThrow(() -> new RuntimeException("User with ID " + supervisorId + " not found"));
                    supervisors.add(supervisor);
                }
            }
            client.setSupervisors(supervisors); // Remplace l'ensemble existant
        }

        // Sauvegarder le client
        clientRepository.save(client);
    }

    @Override
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(Math.toIntExact(id))) {
            throw new RuntimeException("Client not found");
        }
        clientRepository.deleteById(Math.toIntExact(id));
    }

    @Override
    public Client getClientById(Long id) {
        return clientRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }


    public Client getClientByNameOrThrow(String clientNameRaw) {
        String clientName = clientNameRaw.trim().toLowerCase();

        // Vérification Redis
        String clientInfoKey = CLIENT_INFO_KEY + clientName;
        String cachedClientId = redisTemplate.opsForValue().get(clientInfoKey);

        if (cachedClientId != null) {
            return clientRepository.findById(Math.toIntExact(Long.valueOf(cachedClientId)))
                    .orElseThrow(() -> new RuntimeException("❌ Client ID " + cachedClientId + " invalid"));
        }

        // Vérification en base
        Client client = clientRepository.findByName(clientName)
                .orElseThrow(() -> new RuntimeException("❌ Client " + clientName + " not found on database"));

        // Mise en cache
        redisTemplate.opsForValue().set(clientInfoKey, String.valueOf(client.getId()), Duration.ofDays(1));
        redisTemplate.opsForValue().set(CLIENT_EXISTS_KEY + clientName, "1", Duration.ofDays(1));

        return client;
    }

}
