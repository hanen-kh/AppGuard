package com.sfm.projetPFE.Application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByIndexElasticsearch(String indexElasticsearch);
    Optional<Application> findByName(String nom);

}
