package com.sfm.projetPFE.Jwt;


import com.sfm.projetPFE.AppGuardLogs.LogLevel;
import com.sfm.projetPFE.AppGuardLogs.LogServiceImpl;
import com.sfm.projetPFE.User.User;
import com.sfm.projetPFE.User.UserServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
@Transactional
public class JwtService {
    private final LogServiceImpl logService;
    private final UserServiceImpl userService;
    private final JwtRepository jwtRepository;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);


    public Jwt tokenByValeur(String valeur) {
        return jwtRepository.findByValue(valeur)
                .orElseThrow(() -> new RuntimeException("no token"));
    }

    public Map<String, String> generate(String username) {
        User utilisateur = userService.loadUserByUsername(username);
        this.disableTokens(utilisateur);
        final Map<String, String> jwtMap = generateJwt(utilisateur);
        final Jwt jwt = Jwt.builder()
                .desactivated(false)
                .expired(false)
                .value(jwtMap.get("accessToken"))  // << maintenant c’est accessToken
                .user(utilisateur)
                .build();
        jwtRepository.save(jwt);
        logger.info("Sauvegarde du token en base ");
        return jwtMap;
    }


    public void disableTokens(User utilisateur) {
        final List<Jwt> jwtList = jwtRepository.findTokensByUserEmail(utilisateur.getEmail()).map(
                jwt -> {
                    jwt.setDesactivated(true);
                    jwt.setExpired(true);
                    return jwt;
                }

        ).collect(Collectors.toList());
        jwtRepository.saveAll(jwtList);
    }

    public Map<String, String> generateJwt(User utilisateur) {
        final long currentTime = System.currentTimeMillis();
        final long accessExpiration = currentTime + (300 * 60 * 1000); // 30 minutes
       final long refreshExpiration = currentTime + (24 * 60 * 60 * 1000); // 24 heures

        // Création Access Token
        final String accessToken = Jwts.builder()
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(accessExpiration))
                .setSubject(utilisateur.getEmail())
                .addClaims(Map.of(
                        "nom", utilisateur.getFirstname(),
                        "type", "access"
                ))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();

        // Création Refresh Token
       final String refreshToken = Jwts.builder()
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(refreshExpiration))
                .setSubject(utilisateur.getEmail())
                .addClaims(Map.of(
                        "nom", utilisateur.getFirstname(),
                        "type", "refresh"
                ))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();

        return Map.of(
                "accessToken", accessToken,
               "refreshToken", refreshToken
        );
    }


    public Key getKey() {
        final byte[] decoder = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(decoder);
    }

    public Date getExpirationDateFromToken(String token) {
        return this.getClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.before(new Date());
    }

    public  <T> T getClaim(String token, Function<Claims, T> function) {
        Claims claims = getAllClaims(token);
        return function.apply(claims);
    }

    public Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return this.getClaim(token, Claims::getSubject);
    }

    public void deconnexion(){
        User utilisateur = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Jwt jwt = jwtRepository.findValidToken(utilisateur.getEmail(), false, false)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        jwt.setDesactivated(true);
        jwt.setExpired(true);
        jwtRepository.save(jwt);

        // Création du log pour la déconnexion
        long startTime = System.currentTimeMillis();
        try {
            logService.createLog(
                    utilisateur.getId(),                   // idUser
                    "auth-service",                        // serviceName
                    "logout()",                            // methodName
                    LogLevel.INFO,                         // Niveau de log
                    startTime,                             // Temps de début
                    null                                    // Pas de stack trace car pas d'erreur
            );
        }  catch (Exception e) {
            logService.createLog(
                    0, // si le user n'existe pas
                    "auth-service",
                    "login()",
                    LogLevel.ERROR,
                    startTime,
                    e.getMessage()
            );
            throw e;
        }

    }

    // @Scheduled(cron = "@daily")
   // @Scheduled(cron = "0 */5 * * * * ")
    //public void removeUselessTokens(){
      //  log.info("Supprission des Tokens à {}", Instant.now());
        //jwtRepository.deleteAllByExpiredAndDesactivated(true,true);
    //}


    //  AJOUTÉ pour vérifier si c'est un Refresh Token :
    public boolean isRefreshToken(String token) {
        String type = getClaim(token, claims -> claims.get("type", String.class));
        return "refresh".equals(type);
    }
}
