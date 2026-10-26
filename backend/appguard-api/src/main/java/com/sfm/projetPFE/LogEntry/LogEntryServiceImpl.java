package com.sfm.projetPFE.LogEntry;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfm.projetPFE.AppGuardLogs.LogService;
import com.sfm.projetPFE.Application.ApplicationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class LogEntryServiceImpl {

    private final Map<String, List<LogEntry>> logMap =new ConcurrentHashMap<>();
@Autowired
private ApplicationServiceImpl applicationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);


   @KafkaListener(topics = "app-logs", groupId = "loggroup")
    public void consumeLog(String message) {


       try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules(); // gère LocalDateTime

            JsonNode root = mapper.readTree(message);

            // 🔍 Extraction des données
            String applicationName = root.path("application").asText("unknown-app");
            if ("unknown-app".equals(applicationName)) {
                log.warn("⚠️ Application inconnue dans le message, ignoré");
                return;
            }

            // 🏠 Informations du serveur depuis les logs
           String hostIp = "";
           JsonNode ipNode = root.path("host").path("ip");

           if (ipNode.isArray() && ipNode.size() > 0) {
               hostIp = ipNode.get(0).asText(); // prend la première IP
           } else {
               // fallback si ip est absent ou mal formé
               hostIp = root.path("fields").path("host").asText("");
           }
            String hostName = root.path("host").path("name").asText("");

            String os = root.path("host").path("os").path("name").asText("");
           String clientName = root.path("clientName").asText(null);



           // ⚡ Vérification ultra-rapide nouvelle application (< 1ms)
            applicationService.checkNewApplication(applicationName, hostIp, hostName, os, clientName);

            // 📝 Traitement du log (comme votre code original)
            LogEntry entry = buildLogEntry(root, message);

            // ✅ Stockage dans la map groupée par application
            logMap.computeIfAbsent(applicationName, k -> new ArrayList<>()).add(entry);

            logger.info("📥 Log reçu pour [{}] niveau [{}]", applicationName, entry.getLevel());

            // 📡 Envoi live pour Angular via WebSocket
            messagingTemplate.convertAndSend("/topic/logs/", entry);

           if ("appguard".equals(applicationName)) {
               System.out.println("✅ Message WebSocket envoyé pour appguard");
           }

        } catch (Exception e) {
            log.error("❌ Erreur parsing message Kafka : {}", message, e);
        }
    }


    /**
     * 📝 Construction de l'entrée de log (votre logique existante)
     */
    private LogEntry buildLogEntry(JsonNode root, String message) {
        try {
            // 💬 Timestamp depuis Filebeat
            String timestampStr = root.path("@timestamp").asText();
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_DATE_TIME);

            // 🎯 Extraction directe du contenu du champ "message"
            String messageContent = root.path("message").asText("");

            // 🔥 Extraction du niveau depuis le JSON message (optionnel)
            String level = extractLevel(messageContent);

            // 🧾 Création de l'entrée log
            LogEntry entry = new LogEntry();
            entry.setId(UUID.randomUUID().toString());
            entry.setTimestamp(timestamp);
            entry.setLevel(level);
            entry.setMessage(messageContent); // ✅ Message JSON complet

            return entry;

        } catch (Exception e) {
            log.error("❌ Erreur parsing message : {}", message, e);
            return createFallbackLogEntry(message);
        }
    }

    private String extractLevel(String message) {
        if (message.contains("ERROR")) return "ERROR";
        if (message.contains("WARN")) return "WARN";
        if (message.contains("INFO")) return "INFO";
        if (message.contains("DEBUG")) return "DEBUG";
        return "UNKNOWN";
    }


    /**
     * 🚨 Fallback en cas d'erreur
     */
    private LogEntry createFallbackLogEntry(String originalMessage) {
        LogEntry fallback = new LogEntry();
        fallback.setId(UUID.randomUUID().toString());
        fallback.setTimestamp(LocalDateTime.now());
        fallback.setLevel("ERROR");
        fallback.setMessage("Erreur parsing: " + originalMessage);
        return fallback;
    }



}
