package com.sfm.projetPFE.Validation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidationRepository extends JpaRepository<Validation, Long> {

    Validation findByCode(String code);
}
