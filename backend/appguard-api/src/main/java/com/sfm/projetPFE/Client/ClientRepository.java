package com.sfm.projetPFE.Client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    Optional<Client> findByName(String name);

    List<Client> findBySupervisors_Email(String email);  // le username (email) de l'utilisateur connecté

}
