package com.sfm.projetPFE.ServiceStatus;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfm.projetPFE.Alert.AlertServiceImpl;
import com.sfm.projetPFE.Alert.NewEntityAlert;
import com.sfm.projetPFE.Application.ApplicationServiceImpl;
import com.sfm.projetPFE.Server.Server;
import com.sfm.projetPFE.Server.ServerRepository;
import com.sfm.projetPFE.Server.ServerServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;



@Slf4j
@EnableScheduling
@Service
public class ServiceStatusServiceImpl  {


@Autowired
private ApplicationServiceImpl applicationService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
     @Autowired
     private ServiceStatusRepository serviceStatusRepository;


     @Autowired
     private AlertServiceImpl alertServiceImpl;

     @Autowired
    private  ElasticsearchClient elasticsearchClient;

    private final Map<String, List<ServiceStatus>> serviceStatusMap = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ServerServiceImpl serverService;

    // 🔑 Clés Redis optimisées pour serveurs et services

    private static final String SERVICE_EXISTS_KEY = "svc:exists:"; // svc:exists:192.168.1.100:mongodb
    private static final String SERVICE_INFO_KEY = "svc:info:"; // svc:info:<id> → ID
    private static final String PENDING_SERVICES_KEY = "svc:pending"; // Queue services à vérifier


    @PostConstruct
    public void initializeCache() {
       // log.info("🧹 Nettoyage du cache existant pour Serveurs/Services...");
      //  Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushDb();


        List<ServiceStatus> allServices = serviceStatusRepository.findAll();
        for (ServiceStatus service : allServices) {
            String serviceExistsKey = SERVICE_EXISTS_KEY + service.getServer().getIp() + ":" + service.getServiceName().toLowerCase();
            String serviceInfoKey = SERVICE_INFO_KEY + service.getId();
            redisTemplate.opsForValue().set(serviceExistsKey, "1", Duration.ofDays(1));
            redisTemplate.opsForValue().set(serviceInfoKey, String.valueOf(service.getId()), Duration.ofDays(1));
        }
        log.info("✅ Cache des Serveurs/Services initialisé : {} serveurs, {} services",
                serverRepository.count(), serviceStatusRepository.count());
    }

    /**
     * 🎯 Méthode ultra-rapide pour vérifier un nouveau service
     * Retourne true si potentiellement nouveau (sera vérifié en batch)
     */
    public void checkNewService(String serviceName, String hostIp, String hostName, String os, String clientName) {
        String serviceKey = SERVICE_EXISTS_KEY + hostIp + ":" + serviceName.toLowerCase().trim();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(serviceKey))) {
            return; // ✅ Service déjà connu
        }

        ServiceCheckData checkData = new ServiceCheckData(serviceName, hostIp, hostName, os, clientName);
        String serializedData = serializeServiceCheckData(checkData);
        redisTemplate.opsForSet().add(PENDING_SERVICES_KEY, serializedData);

        log.debug("🔍 Service potentiellement nouveau ajouté en queue : {}", serviceName);
    }



    private String serializeServiceCheckData(ServiceCheckData data) {
        return String.join("|",
                data.getServiceName(),
                data.getHostIp(),
                data.getHostName() != null ? data.getHostName() : "",
                data.getOs() != null ? data.getOs() : "",
                data.getClientName() != null ? data.getClientName() : ""

        );
    }

    private ServiceCheckData deserializeServiceCheckData(String data) {
        String[] parts = data.split("\\|", -1);
        return new ServiceCheckData(
                parts[0], // serviceName
                parts[1], // hostIp
                parts.length > 2 ? parts[2] : "", // hostName
                parts.length > 3 ? parts[3] : "" , // os
                parts.length > 4 ? parts[4] : ""
        );
    }

    /**
     * 🔄 Traitement batch des services en attente (toutes les 5 secondes)
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processPendingServices() {
        Set<String> pendingServices = redisTemplate.opsForSet().members(PENDING_SERVICES_KEY);
        if (pendingServices != null && !pendingServices.isEmpty()) {
            log.info("🔄 Traitement de {} services potentiellement nouveaux", pendingServices.size());
            List<String> servicesList = new ArrayList<>(pendingServices);
            int chunkSize = 30;
            for (int i = 0; i < servicesList.size(); i += chunkSize) {
                List<String> chunk = servicesList.subList(i, Math.min(i + chunkSize, servicesList.size()));
                processServiceChunk(chunk);
            }
            redisTemplate.delete(PENDING_SERVICES_KEY);
        }
    }

    /**
     * 📦 Traitement d'un chunk de services
     */
    private void processServiceChunk(List<String> serviceChunk) {
        Map<String, ServiceStatus> serviceCache = new HashMap<>();
        List<NewEntityAlert> alerts = new ArrayList<>();

        for (String serializedData : serviceChunk) {
            try {
                ServiceCheckData checkData = deserializeServiceCheckData(serializedData);
                Server server = serverService.getOrCreateServer(checkData.getHostIp(), checkData.getHostName(), checkData.getOs(), checkData.getClientName());
                ServiceStatus service = processService(checkData, serviceCache, server, alerts);
            } catch (Exception e) {
                log.error("❌ Erreur traitement service: {}", serializedData, e);
            }
        }

        saveServiceEntitiesBatch(serviceCache);
        sendAlerts(alerts);
    }

    /**
     * 🏠 Traitement du serveur (création si nécessaire)
     */


    private ServiceStatus processService(ServiceCheckData checkData, Map<String, ServiceStatus> serviceCache, Server server, List<NewEntityAlert> alerts) {
        String serviceKey = checkData.getHostIp() + ":" + checkData.getServiceName().toLowerCase().trim();
        ServiceStatus service = serviceCache.get(serviceKey);
        if (service != null) {
            return service;
        }

        String serviceInfoKey = SERVICE_INFO_KEY + serviceKey;
        String cachedServiceId = redisTemplate.opsForValue().get(serviceInfoKey);
        if (cachedServiceId != null) {
            Optional<ServiceStatus> cached = serviceStatusRepository.findById(Long.valueOf(cachedServiceId));
            if (cached.isPresent()) {
                service = cached.get();
                serviceCache.put(serviceKey, service);
                return service;
            }
        }

        Optional<ServiceStatus> existing = serviceStatusRepository.findByServiceNameAndServerId(checkData.getServiceName(), server.getId());
        if (existing.isPresent()) {
            service = existing.get();
            redisTemplate.opsForValue().set(serviceInfoKey, String.valueOf(service.getId()), Duration.ofDays(1));
            redisTemplate.opsForValue().set(SERVICE_EXISTS_KEY + serviceKey, "1", Duration.ofDays(1));
        } else {
            service = new ServiceStatus();
            service.setServiceName(checkData.getServiceName());
            service.setServer(server);
            alerts.add(new NewEntityAlert("SERVICE",
                    String.format("🛠️ Nouveau service détecté : %s sur %s (%s)", checkData.getServiceName(), server.getName(), server.getIp())));
        }
        serviceCache.put(serviceKey, service);
        return service;
    }

    /**
     * 💾 Sauvegarde groupée des entités de service
     */
    private void saveServiceEntitiesBatch(Map<String, ServiceStatus> serviceCache) {
        if (!serviceCache.isEmpty()) {
            List<ServiceStatus> savedServices = serviceStatusRepository.saveAll(serviceCache.values());
            for (ServiceStatus service : savedServices) {
                String serviceKey = SERVICE_EXISTS_KEY + service.getServer().getIp() + ":" + service.getServiceName().toLowerCase();
                String serviceInfoKey = SERVICE_INFO_KEY + service.getId();
                redisTemplate.opsForValue().set(serviceKey, "1", Duration.ofDays(1));
                redisTemplate.opsForValue().set(serviceInfoKey, String.valueOf(service.getId()), Duration.ofDays(1));
            }
        }
    }

    /**
     * 📢 Envoi des notifications
     */
    private void sendAlerts(List<NewEntityAlert> alerts) {
        for (NewEntityAlert alert : alerts) {
            messagingTemplate.convertAndSend("/topic/alerts", alert);
            log.info("✅ {}", alert.getMessage());
        }
    }






    @KafkaListener(topics = "new-service-metrics", groupId = "metricgroup")
    public void consume(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ServiceMetricsDTO dto = mapper.readValue(message, ServiceMetricsDTO.class);

            // 🔹 Ignorer si metricset.name != "process"
            if (!"process".equalsIgnoreCase(dto.getMetricset().getName())) {
                log.debug("Metricset ignoré : {}", dto.getMetricset().getName());
                return;
            }

            JsonNode root = mapper.readTree(message);

            // Récupérer fields.serviceName
            String serviceName = root.path("fields").path("serviceName").asText(null);
            if (serviceName == null || serviceName.isEmpty()) {
                log.debug("Service inconnu ou absent, métriques abandonnées.");
                return;
            }

            // 🏠 Informations serveur
            String hostIp = "";
            JsonNode ipNode = root.path("host").path("ip");
            if (ipNode.isArray() && !ipNode.isEmpty()) {
                hostIp = ipNode.get(0).asText();
            } else {
                hostIp = root.path("fields").path("host").asText("");
            }

            String hostName = root.path("host").path("name").asText("");
            String os = root.path("host").path("os").path("name").asText("");
            String clientName = root.path("fields").path("clientName").asText(null);

            // ⚡ Vérification d'un nouveau service
            checkNewService(serviceName, hostIp, hostName, os, clientName);

            // 🔹 Création de l'objet ServiceStatus
            ServiceStatus status = new ServiceStatus();
            status.setServiceName(serviceName);

            double cpuUsage = getDoubleValue(root, "/process/cpu/pct", 0.0);
            double memoryUsage = getDoubleValue(root, "/process/memory/pct", 0.0);
            double diskUsage = calculateDiskUsage(root);

            status.setCpuUsage(round(cpuUsage, 4));
            status.setMemoryUsage(round(memoryUsage, 4));
            status.setDiskUsage(diskUsage);

            boolean isActive = cpuUsage > 0 || memoryUsage > 0;
            status.setIsActive(isActive);
            status.setLastChecked(LocalDateTime.now());

            // 🔹 Stockage dans la map
            serviceStatusMap.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(status);

            // 🔹 Envoi vers Angular via WebSocket
            messagingTemplate.convertAndSend("/topic/metrics", status);

            // 🔹 Pour debug : afficher la liste complète du service
            mapper.findAndRegisterModules(); // support LocalDateTime
            List<ServiceStatus> list = serviceStatusMap.get(serviceName);
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(list);
            log.debug("Liste mise à jour pour {} : {}", serviceName, json);

        } catch (Exception e) {
            log.error("❌ Erreur Kafka : {}", message, e);
        }
    }






    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    // ✅ Calcul du disk usage basé sur I/O
    private double calculateDiskUsage(JsonNode root) {
        try {
            // Parcourir les devices de stockage dans cgroup.io.stats
            JsonNode ioStats = root.at("/system/process/cgroup/io/stats");
            if (ioStats.isMissingNode()) {
                return 0.0;
            }

            final double[] totalBytes = {0.0};

            // Parcourir chaque device (nvme0n1, sda, etc.)
            ioStats.fields().forEachRemaining(device -> {
                JsonNode deviceStats = device.getValue();

                // Read bytes
                long readBytes = deviceStats.at("/read/bytes").asLong(0);
                // Write bytes
                long writeBytes = deviceStats.at("/write/bytes").asLong(0);

                totalBytes[0] += (readBytes + writeBytes);
            });

            // Convertir en MB et retourner un pourcentage relatif
            double totalMB = totalBytes[0] / (1024.0 * 1024.0);

            // Retourner les MB utilisés (vous pouvez ajuster selon vos besoins)
            return totalMB;

        } catch (Exception e) {
            log.warn("Erreur calcul disk usage: {}", e.getMessage());
            return 0.0;
        }
    }


    private int getIntValue(JsonNode node, String path, int defaultValue) {
        JsonNode valueNode = node.at(path);
        return valueNode.isMissingNode() || valueNode.isNull() ? defaultValue : valueNode.asInt();
    }

    private double getDoubleValue(JsonNode root, String path, double defaultValue) {
        JsonNode node = root.at(path);
        if (node != null && node.isNumber()) {
            return node.asDouble();
        }
        return defaultValue;
    }

    private String extractServiceName(ServiceMetricsDTO dto) {
        if (dto.getFields() != null && dto.getFields().getServiceName() != null)
            return dto.getFields().getServiceName();
        return "unknown";
    }











    public List<ServiceStatus> getMetricsByServiceName(String serviceName) throws IOException {
        String index = serviceName.toLowerCase() + "-metrics-*";

        SearchResponse<JsonData> response = elasticsearchClient.search(s -> s
                .index(index)
                .size(10)
                .sort(sort -> sort
                        .field(f -> f
                                .field("@timestamp")
                                .order(SortOrder.Desc)
                        )
                )
                .query(q -> q.matchAll(m -> m)), JsonData.class);

        return response.hits().hits().stream()
                .map(hit -> {
                    JsonNode source = hit.source().to(JsonNode.class);
                    ServiceStatus status = new ServiceStatus();

                    status.setServiceName(source.at("/fields/serviceName").asText(null));
                  //  status.setHost(source.at("/fields/host").asText(null));
                    status.setPort(source.at("/fields/port").asInt(-1));
                    status.setIsActive(source.at("/fields/isActive").asBoolean(false));
                    status.setCpuUsage(source.at("/process/cpu/pct").asDouble(0.0) * 100);
                    status.setMemoryUsage(source.at("/process/memory/pct").asDouble(0.0) * 100);
                    status.setDiskUsage(0.0); // à compléter si données présentes
                    status.setLastChecked(LocalDateTime.now());

                    return status;
                })
                .collect(Collectors.toList());
    }





    public List<ServiceStatus> getAllServiceStatuses() {
        return serviceStatusRepository.findAll();
    }
}
