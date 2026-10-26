package com.sfm.projetPFE.User;

import com.sfm.projetPFE.Client.Client;
import com.sfm.projetPFE.Role.TypeRole;
import io.lettuce.core.models.role.RedisInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);


    List<User> findByRole_Type(TypeRole type);
    @Query("SELECT u FROM User u JOIN u.role r WHERE r.type = 'SUPERVISOR'")
    List<User> findAllSupervisors();
}
