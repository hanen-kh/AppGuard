package com.sfm.projetPFE.Jwt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface JwtRepository extends JpaRepository<Jwt, Long> {

    // Trouver un JWT par sa valeur
    Optional<Jwt> findByValue(String value);


    // Trouver un JWT valide pour un utilisateur donné
    @Query("SELECT j FROM Jwt j WHERE j.desactivated = :desactivated AND j.expired = :expired AND j.user.email = :email")
    Optional<Jwt> findValidToken(@Param("email") String email,
                                 @Param("expired") boolean expired,
                                 @Param("desactivated") boolean desactivated);

    // Trouver tous les JWT associés à un utilisateur donné
    @Query("SELECT j FROM Jwt j WHERE j.user.email = :email")
    Stream<Jwt> findTokensByUserEmail(@Param("email") String email);

    // Supprimer tous les JWT expirés et désactivés
    void deleteAllByExpiredAndDesactivated(boolean expired, boolean desactivated);
}
