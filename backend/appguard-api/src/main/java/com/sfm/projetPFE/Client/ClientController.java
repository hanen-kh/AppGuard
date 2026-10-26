package com.sfm.projetPFE.Client;


import com.sfm.projetPFE.User.SupervisorDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")


public class ClientController {

    @Autowired
    private ClientServiceImpl clientService;


    @GetMapping("/by_user")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERVISOR')")
    public ResponseEntity<List<ClientDTO>> getClientsOfCurrentUser() {
        List<ClientDTO> clients = clientService.getClientsOfCurrentUser();
        return ResponseEntity.ok(clients);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> createClient(@Valid @RequestBody ClientDTO clientDto) {
        clientService.createClient(clientDto);
        return ResponseEntity.status(201).build();
    }



    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> updateClient(@PathVariable Long id, @Valid @RequestBody ClientDTO clientDto) {
        clientService.updateClient(id, clientDto);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getBy_id/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }



}
