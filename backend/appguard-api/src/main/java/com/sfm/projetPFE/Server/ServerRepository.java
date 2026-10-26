package com.sfm.projetPFE.Server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {
    Optional<Server> findByIp(String ip);
    Optional<Server> findByName(String name);

}
