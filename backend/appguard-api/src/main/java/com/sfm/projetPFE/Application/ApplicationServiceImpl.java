package com.sfm.projetPFE.Application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sfm.projetPFE.Alert.NewEntityAlert;
import com.sfm.projetPFE.AppGuardLogs.LogService;
import com.sfm.projetPFE.LogEntry.LogEntry;
import com.sfm.projetPFE.Server.Server;
import com.sfm.projetPFE.Server.ServerDto;
import com.sfm.projetPFE.Server.ServerRepository;
import com.sfm.projetPFE.Server.ServerServiceImpl;
import com.sfm.projetPFE.ServiceStatus.ServiceStatus;
import jakarta.annotation.PostConstruct;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;

import java.io.StringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


import static com.sfm.projetPFE.Server.ServerDto.*;
@Service
public class ApplicationServiceImpl implements ApplicationService {


    @Autowired
    private ApplicationRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    @Autowired
    private ElasticsearchClient elasticsearchClient;

  


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

  @Autowired
  private ServerServiceImpl serverService;

    // 🔑 Clés Redis optimisées
    private static final String APP_EXISTS_KEY = "app:exists:"; // app:exists:my-webapp
    private static final String SERVER_EXISTS_KEY = "srv:exists:"; // srv:exists:192.168.1.100
    private static final String PENDING_APPS_KEY = "app:pending"; // Queue applications à vérifier
    private static final String APP_INFO_KEY = "app:info:"; // app:info:my-webapp → ID
    public static final String SERVER_INFO_KEY = "srv:info:"; // srv:info:192.168.1.100 → ID


    /**
     * 🎯 Méthode principale ultra-rapide pour vérifier nouvelle application
     * Retourne true si potentiellement nouvelle (sera vérifiée en batch)
     */



    @Override
    public void checkNewApplication(String applicationName, String hostIp, String hostName, String os, String clientName) {
        String appKey = APP_EXISTS_KEY + applicationName.toLowerCase().trim();

        if (Boolean.TRUE.equals(redisTemplate.hasKey(appKey))) {
            return; // ✅ Application déjà connue
        }

        ApplicationCheckData checkData = new ApplicationCheckData(applicationName, hostIp, hostName, os, clientName);
        String serializedData = serializeCheckData(checkData);

        redisTemplate.opsForSet().add(PENDING_APPS_KEY, serializedData);

        logger.debug("🔍 Application potentiellement nouvelle ajoutée en queue : {}", applicationName);
    }


    /**
     * 🔄 Traitement batch des applications en attente (toutes les 10 secondes)
     */

    @Override
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void processPendingEntities() {
        // 🔄 Traitement des applications
        Set<String> pendingApps = redisTemplate.opsForSet().members(PENDING_APPS_KEY);
        if (pendingApps != null && !pendingApps.isEmpty()) {
            logger.info("🔄 Traitement de {} applications potentiellement nouvelles", pendingApps.size());

            List<String> appsList = new ArrayList<>(pendingApps);
            int chunkSize = 30;
            for (int i = 0; i < appsList.size(); i += chunkSize) {
                List<String> chunk = appsList.subList(i, Math.min(i + chunkSize, appsList.size()));
                processApplicationChunk(chunk);
            }

            redisTemplate.delete(PENDING_APPS_KEY);
        }


    }



    /**
     * 📦 Traitement d'un chunk d'applications
     */

    @Override
    public void processApplicationChunk(List<String> appChunk) {
        Map<String, Application> appCache = new HashMap<>();
        Map<String, Server> serverCache = new HashMap<>();
        List<NewEntityAlert> alerts = new ArrayList<>();

        for (String serializedData : appChunk) {
            try {
                ApplicationCheckData checkData = deserializeCheckData(serializedData);

                // 🏠 Traitement du serveur en premier
                Server server = serverService.getOrCreateServer(checkData.getHostIp(), checkData.getHostName(), checkData.getOs(), checkData.getClientName());
                // 📱 Traitement de l'application
                processApplication(checkData, appCache, server, alerts);

            } catch (Exception e) {
                logger.error("❌ Erreur traitement application: {}", serializedData, e);
            }
        }

        // 💾 Sauvegarde groupée
        saveEntitiesBatch(appCache, serverCache);

        // 📢 Envoi des notifications
        sendAlerts(alerts);
    }



    /**
     * 📱 Traitement de l'application
     */

    @Override
   public void processApplication(ApplicationCheckData checkData, Map<String, Application> appCache,
                                    Server server, List<NewEntityAlert> alerts) {
        String appName = checkData.getApplicationName().trim();

        // Vérification cache local
        Application app = appCache.get(appName.toLowerCase());
        if (app != null) {
            return;
        }

        // Vérification cache Redis
        String appInfoKey = APP_INFO_KEY + appName.toLowerCase();
        String cachedAppId = redisTemplate.opsForValue().get(appInfoKey);

        if (cachedAppId != null) {
            Optional<Application> cached = applicationRepository.findById(Long.valueOf(cachedAppId));
            if (cached.isPresent()) {
                app = cached.get();
                appCache.put(appName.toLowerCase(), app);
                return;
            }
        }

        // Vérification en base
        Optional<Application> existing = applicationRepository.findByName(appName);
        if (existing.isPresent()) {
            app = existing.get();

            // Mise en cache
            redisTemplate.opsForValue().set(appInfoKey, String.valueOf(app.getId()), Duration.ofDays(1));
            redisTemplate.opsForValue().set(APP_EXISTS_KEY + appName.toLowerCase(), "1", Duration.ofDays(1));
        } else {
            // 🆕 Nouvelle application
            app = new Application();
            app.setName(appName);

            // Normalisation du nom pour Elasticsearch
            String normalizedName = normalizeName(appName);
            String indexElasticsearch = normalizedName + "-logs-*";
            app.setIndexElasticsearch(indexElasticsearch);

            // Description automatique basée sur le serveur
            app.setDescription(String.format("Application découverte sur %s (%s)",
                    server.getName(), server.getIp()));

            //server
            app.setServer(server);

            // ClientName extrait depuis le document arrivant de kafka à springboot
            app.setClientName(checkData.getClientName());


            alerts.add(new NewEntityAlert("APPLICATION",
                    String.format(" Nouvelle application détectée : %s sur %s (%s)",
                            appName, server.getName(), server.getIp())));
        }

        appCache.put(appName.toLowerCase(), app);
    }



    /**
     * 💾 Sauvegarde groupée des entités
     */
    @Override
    public void saveEntitiesBatch(Map<String, Application> appCache, Map<String, Server> serverCache) {
        // Sauvegarde serveurs en premier (pour les IDs)
        if (!serverCache.isEmpty()) {
            List<Server> savedServers = serverRepository.saveAll(serverCache.values());

            // Mise à jour cache Redis des serveurs
            for (Server server : savedServers) {
                String serverInfoKey = SERVER_INFO_KEY + server.getIp();
                redisTemplate.opsForValue().set(serverInfoKey, String.valueOf(server.getId()), Duration.ofDays(1));
                redisTemplate.opsForValue().set(SERVER_EXISTS_KEY + server.getIp(), "1", Duration.ofDays(1));
            }
        }

        // Sauvegarde applications
        if (!appCache.isEmpty()) {
            List<Application> savedApps = applicationRepository.saveAll(appCache.values());

            // Mise à jour cache Redis des applications
            for (Application app : savedApps) {
                String appInfoKey = APP_INFO_KEY + app.getName().toLowerCase();
                redisTemplate.opsForValue().set(appInfoKey, String.valueOf(app.getId()), Duration.ofDays(1));
                redisTemplate.opsForValue().set(APP_EXISTS_KEY + app.getName().toLowerCase(), "1", Duration.ofDays(1));
            }
        }
    }

    /**
     * 📢 Envoi des notifications
     */
    @Override
    public void sendAlerts(List<NewEntityAlert> alerts) {
        for (NewEntityAlert alert : alerts) {
            messagingTemplate.convertAndSend("/topic/alerts", alert);
            logger.info("✅ {}", alert.getMessage());
        }
    }




    @Override
    public String serializeCheckData(ApplicationCheckData data) {
        return String.join("|",
                data.getApplicationName(),
                data.getHostIp(),
                data.getHostName() != null ? data.getHostName() : "",
                data.getOs() != null ? data.getOs() : "",
                data.getClientName() != null ? data.getClientName() : ""
        );
    }



    @Override
    public ApplicationCheckData deserializeCheckData(String data) {
        String[] parts = data.split("\\|", -1);
        return new ApplicationCheckData(
                parts[0], // applicationName
                parts[1], // hostIp
                parts.length > 2 ? parts[2] : "", // hostName
                parts.length > 3 ? parts[3] : ""  , // os
                parts.length > 4 ? parts[4] : ""  // clientName
        );
    }



    /**
     * 🚀 Initialisation du cache au démarrage
     */
    @Override
    @PostConstruct
    public void initializeCache() {

        // Cache des applications
        logger.info("🚀 Initialisation du cache Applications/Serveurs...");


        List<Application> allApps = applicationRepository.findAll();
        for (Application app : allApps) {
            String appInfoKey = APP_INFO_KEY + app.getName().toLowerCase();
            String appExistsKey = APP_EXISTS_KEY + app.getName().toLowerCase();
            redisTemplate.opsForValue().set(appInfoKey, String.valueOf(app.getId()), Duration.ofDays(1));
            redisTemplate.opsForValue().set(appExistsKey, "1", Duration.ofDays(1));
        }

        // Cache des serveurs
        List<Server> allServers = serverRepository.findAll();
        for (Server server : allServers) {
            String serverInfoKey = SERVER_INFO_KEY + server.getIp();
            String serverExistsKey = SERVER_EXISTS_KEY + server.getIp();
            redisTemplate.opsForValue().set(serverInfoKey, String.valueOf(server.getId()), Duration.ofDays(1));
            redisTemplate.opsForValue().set(serverExistsKey, "1", Duration.ofDays(1));
        }



        logger.info("✅ Cache initialisé : {} applications, {} serveurs", allApps.size(), allServers.size());
    }







    private String normalizeName(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private String extractClientName(String hostName, String hostIp) {
        if (hostName != null && !hostName.isEmpty() && !hostName.equals(hostIp)) {
            // Extraire le nom du client depuis le hostname (ex: client1-web-01 → client1)
            String[] parts = hostName.split("-");
            if (parts.length > 0) {
                return parts[0];
            }
        }
        return "unknown-client";
    }

    @Override
    public Application save(Application app) {
        if (app.getName() == null || app.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("The application name cannot be empty");
        }

        // Vérifier si une application avec le même nom existe déjà
        if (repository.findByName(app.getName()).isPresent()) {
            throw new IllegalArgumentException("An app with that name already exists");
        }

        // Normaliser le nom pour l'index Elasticsearch
        String normalizedName = normalizeName(app.getName());
        String indexElasticsearch =  normalizedName +"-logs" + "-*";
        app.setIndexElasticsearch(indexElasticsearch);

        // La sauvegarde déclenche @PrePersist pour createdAt
        return repository.save(app);
    }


    @Override
    public List<Application> getAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Application> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<Application> getByIndex(String index) {
        return repository.findByIndexElasticsearch(index);
    }

    @Override
    public ApplicationDto getApplicationByName(String appName) {
        Application application = applicationRepository.findByName(appName)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        Server server = application.getServer();
        List<String> serviceNames = server.getServices()
                .stream()
                .map(ServiceStatus::getServiceName)
                .toList();

        return convertToDto(application);
    }

    @Override
    public List<ApplicationDto> getAllApplications() {
        List<Application> applications = applicationRepository.findAll();

        if (applications.isEmpty()) {
            return Collections.emptyList();
        }

        return applications.stream()
                .filter(Objects::nonNull)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ApplicationDto convertToDto(Application app) {
        return ApplicationDto.builder()
                .id(app.getId())
                .name(app.getName())
                .description(app.getDescription())
                .indexElasticsearch(app.getIndexElasticsearch())
                .createdAt(app.getCreatedAt())
                .clientName(app.getClientName())
                .server(convertServerToDto(app.getServer()))
                .build();
    }

    private ServerDto convertServerToDto(Server server) {
        if (server == null) return null;

        return builder()
                .id(server.getId())
                .name(server.getName())
                .os(server.getOs())
                .ip(server.getIp())
                .services(server.getServices().stream()
                        .map(ServiceStatus::getServiceName)
                        .collect(Collectors.toList()))
                .build();
    }


    @Override
    public List<LogEntry> getLogsByApplicationName(String applicationName) {
        try {
            Optional<Application> app = repository.findByName(applicationName);
            if (app.isEmpty()) return Collections.emptyList();

            SearchRequest request = new SearchRequest.Builder()
                    .index(app.get().getIndexElasticsearch())
                    .query(q -> q.matchAll(m -> m))
                    .sort(s -> s.field(f -> f.field("@timestamp").order(SortOrder.Desc)))
                    .size(100)
                    .source(s -> s.filter(f -> f.includes("message", "@timestamp")))
                    .build();

            SearchResponse<ObjectNode> response = elasticsearchClient.search(request, ObjectNode.class);
            return parseResponse(response);

        } catch (Exception e) {
            logger.error("Failed to fetch logs", e);
            return Collections.emptyList();
        }
    }

    /**
     * Récupère les logs par niveau (ERROR, WARN, INFO, etc.), par date
     */

    @Override
    public List<LogEntry> getLogsFiltered(String applicationName, String level, String startDate, String endDate) {
        try {
            Optional<Application> appOpt = repository.findByName(applicationName);
            if (appOpt.isEmpty()) {
                return new ArrayList<>();
            }

            String indexPattern = appOpt.get().getIndexElasticsearch();

            // Construction de la query JSON sans filtrer par level
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("{\n");
            queryBuilder.append("  \"size\": 100,\n");
            queryBuilder.append("  \"_source\": [\"@timestamp\", \"level\", \"message\"],\n");
            queryBuilder.append("  \"sort\": [{\"@timestamp\": {\"order\": \"desc\"}}],\n");
            queryBuilder.append("  \"query\": {\n");

            List<String> mustClauses = new ArrayList<>();

            // 🔹 Filtre par date
            if ((startDate != null && !startDate.isEmpty()) || (endDate != null && !endDate.isEmpty())) {
                StringBuilder rangeClause = new StringBuilder();
                rangeClause.append("    {\"range\": {\"@timestamp\": {");

                boolean hasStart = startDate != null && !startDate.isEmpty();
                boolean hasEnd = endDate != null && !endDate.isEmpty();

                if (hasStart) {
                    rangeClause.append(String.format("\"gte\": \"%s\"", startDate));
                }
                if (hasEnd) {
                    if (hasStart) rangeClause.append(", ");
                    rangeClause.append(String.format("\"lte\": \"%s\"", endDate));
                }

                rangeClause.append("}}}");
                mustClauses.add(rangeClause.toString());
            }

            // 🔹 Si aucun filtre : match_all
            if (mustClauses.isEmpty()) {
                queryBuilder.append("    \"match_all\": {}\n");
            } else {
                queryBuilder.append("    \"bool\": {\n");
                queryBuilder.append("      \"must\": [\n");
                queryBuilder.append(String.join(",\n", mustClauses));
                queryBuilder.append("\n      ]\n");
                queryBuilder.append("    }\n");
            }

            queryBuilder.append("  }\n");
            queryBuilder.append("}");

            String query = queryBuilder.toString();

            // Debug
            System.out.println("=== QUERY SANS FILTRE LEVEL ===");
            System.out.println(query);

            // 🔸 Exécuter la recherche
            List<LogEntry> logs = executeSearch(indexPattern, query);

            // 🔸 Filtrer par niveau côté Java si nécessaire
            if (level != null && !level.isEmpty()) {
                logs = logs.stream()
                        .filter(log -> level.equalsIgnoreCase(log.getLevel()))
                        .collect(Collectors.toList());
            }

            return logs;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }



    /**
     * Exécute la recherche et retourne les résultats
     */
    private List<LogEntry> executeSearch(String indexPattern, String query) throws IOException {
        // Fix the JSON to use proper _source format
        String fixedQuery = query.replace(
                "\"_source\": [\"@timestamp\", \"level\", \"message\"]",
                "\"_source\": {\"includes\": [\"@timestamp\", \"level\", \"message\"]}"
        );

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(Arrays.asList(indexPattern))
                .withJson(new StringReader(fixedQuery))
                .build();

        SearchResponse<ObjectNode> response = elasticsearchClient.search(searchRequest, ObjectNode.class);
        return parseResponse(response);
    }

    /**
     * Parse la réponse simple
     */
    private List<LogEntry> parseResponse(SearchResponse<ObjectNode> response) {
        List<LogEntry> logs = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        for (Hit<ObjectNode> hit : response.hits().hits()) {
            ObjectNode source = hit.source();
            if (source != null) {
                LogEntry entry = new LogEntry();

                // 1. Récupérer @timestamp racine (champ principal)
                JsonNode timestampNode = source.get("@timestamp");
                if (timestampNode != null && timestampNode.isTextual()) {
                    try {
                        String timestampStr = timestampNode.asText();
                        entry.setTimestamp(LocalDateTime.parse(
                                timestampStr.replace("Z", ""),
                                DateTimeFormatter.ISO_DATE_TIME
                        ));
                    } catch (Exception e) {
                        // log erreur si besoin
                    }
                }

                // 2. Extraire le champ "message" (JSON stringifié)
                JsonNode messageNode = source.get("message");
                if (messageNode != null && messageNode.isTextual()) {
                    String rawJson = messageNode.asText();
                    entry.setMessage(rawJson); // Stocker le message brut

                    try {
                        JsonNode json = mapper.readTree(rawJson);

                        // 3. Extraire uniquement le "level" du message interne (si présent)
                        if (json.has("level") && !json.get("level").isNull()) {
                            entry.setLevel(json.get("level").asText());
                        }
                    } catch (Exception e) {
                        // ignore erreur parsing JSON message
                    }
                }

                logs.add(entry);
            }
        }
        return logs;
    }


}
