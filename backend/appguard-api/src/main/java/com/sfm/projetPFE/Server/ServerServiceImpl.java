package com.sfm.projetPFE.Server;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfm.projetPFE.Alert.Alert;
import com.sfm.projetPFE.Alert.AlertServiceImpl;
import com.sfm.projetPFE.Alert.NewEntityAlert;
import com.sfm.projetPFE.Application.Application;
import com.sfm.projetPFE.Client.Client;
import com.sfm.projetPFE.Client.ClientService;
import com.sfm.projetPFE.Client.ClientServiceImpl;
import com.sfm.projetPFE.Config.EmailService;
import com.sfm.projetPFE.CronJob.Cron.CronJob;
import com.sfm.projetPFE.CronJob.Cron.CronJobRepository;
import com.sfm.projetPFE.CronJob.Cron.CronJobServiceImpl;
import com.sfm.projetPFE.Role.TypeRole;
import com.sfm.projetPFE.ServiceStatus.ServiceStatus;
import com.sfm.projetPFE.User.User;
import com.sfm.projetPFE.User.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.round;
import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

@Service
public class ServerServiceImpl implements ServerService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private AlertServiceImpl alertService;
    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private CronJobServiceImpl cronJobService;

    @Autowired
    private CronJobRepository cronJobRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EmailService emailService;

    // 🔑 Clés Redis pour serveurs
    private static final String SERVER_EXISTS_KEY = "srv:exists:"; // srv:exists:hostName
    private static final String SERVER_INFO_KEY = "srv:info:";     // srv:info:hostName → ID

    @Autowired
    private ClientServiceImpl clientService;

    // Cache local pour éviter les recherches répétées dans un même contexte
    private final Map<String, Server> serverCache = new HashMap<>();


    @PostConstruct
    public void initializeServerCache() {
        log.info("Initializing server cache...");
        List<Server> allServers = serverRepository.findAll();
        for (Server server : allServers) {
            String hostName = server.getName();
            String serverInfoKey = SERVER_INFO_KEY + hostName;
            String serverExistsKey = SERVER_EXISTS_KEY + hostName;
            redisTemplate.opsForValue().set(serverInfoKey, String.valueOf(server.getId()), Duration.ofDays(1));
            redisTemplate.opsForValue().set(serverExistsKey, "1", Duration.ofDays(1));
            serverCache.put(hostName, server);
        }

        log.info("✅ Cache des Serveurs initialisé : {} entrées", allServers.size());
    }
    public Server getOrCreateServer(String hostIp, String hostName, String os, String clientName) {
        if (hostName == null || hostName.isBlank()) {
            throw new IllegalArgumentException("Le hostname est requis pour identifier le serveur.");
        }

        Server server = serverCache.get(hostName);
        if (server != null) {
            updateServerIfNeeded(server, hostIp, hostName, os);
            return server;
        }

        String serverInfoKey = SERVER_INFO_KEY + hostName;
        String cachedServerId = redisTemplate.opsForValue().get(serverInfoKey);
        if (cachedServerId != null) {
            Optional<Server> cached = serverRepository.findById(Long.valueOf(cachedServerId));
            if (cached.isPresent()) {
                server = cached.get();
                updateServerIfNeeded(server, hostIp, hostName, os);
                serverCache.put(hostName, server);
                return server;
            }
        }

        Optional<Server> existing = serverRepository.findByName(hostName);
        if (existing.isPresent()) {
            server = existing.get();
            updateServerIfNeeded(server, hostIp, hostName, os);
            redisTemplate.opsForValue().set(serverInfoKey, String.valueOf(server.getId()), Duration.ofDays(1));
            redisTemplate.opsForValue().set(SERVER_EXISTS_KEY + hostName, "1", Duration.ofDays(1));
        } else {
            // 👇 Ajoute ici la récupération du client
            Client client = clientService.getClientByNameOrThrow(clientName);

            server = new Server();
            server.setIp(hostIp);
            server.setName(hostName);
            server.setOs(os);
            server.setClient(client);
            server.setStatus(ServerStatus.ACTIVE);
            server.setAgentUrl("http://" + hostIp + ":8000");
            String generatedKey = UUID.randomUUID().toString();
            server.setAgentApiKey(generatedKey);


            server.setServices(new ArrayList<>());

            server = serverRepository.save(server);

            redisTemplate.opsForValue().set(serverInfoKey, String.valueOf(server.getId()), Duration.ofDays(1));
            redisTemplate.opsForValue().set(SERVER_EXISTS_KEY + hostName, "1", Duration.ofDays(1));
        }

        serverCache.put(hostName, server);
        return server;
    }

    private void updateServerIfNeeded(Server server, String hostIp, String hostName, String os) {
        boolean updated = false;
        if (server.getOs() == null || !server.getOs().equalsIgnoreCase(os)) {
            server.setOs(os);
            updated = true;
        }
        if (server.getName() == null || server.getName().isEmpty()) {
            server.setName(hostName);
            updated = true;
        }
        if (updated) {
            serverRepository.save(server); // Mise à jour en base
            log.debug("🔄 Mise à jour serveur : {}", hostIp);
        }
    }



    @Scheduled(fixedRate = 5 * 60 * 1000) // toutes les 5 minutes
    public void pingServers() {
        List<Server> servers = serverRepository.findAll();
        for (Server server : servers) {
            boolean reachable = false;

            try {
                String pingUrl = server.getAgentUrl() + "/ping";
                log.debug("Pinging server {}", pingUrl);

                ResponseEntity<String> response = restTemplate.getForEntity(pingUrl, String.class);
                reachable = response.getStatusCode().is2xxSuccessful();
            } catch (Exception e) {
                log.warn("Ping failed for server {}: {}", server.getName(), e.getMessage());
                reachable = false;
            }

            ServerStatus oldStatus = server.getStatus();
            ServerStatus newStatus = reachable ? ServerStatus.ACTIVE : ServerStatus.UNREACHABLE;

            // Ne faire quelque chose que si le statut a changé
            if (oldStatus != newStatus) {
                updateStatus(server, newStatus);

                String alertType = (newStatus == ServerStatus.UNREACHABLE) ? "SERVER_DOWN" : "SERVER_ACTIVE";
                String message = String.format(
                        "Server '%s' (Client: %s) is now %s.",
                        server.getName(),

                        newStatus.name()
                );


                NewEntityAlert alert = new NewEntityAlert(alertType, message);

                // Envoi par WebSocket sur le topic global
                messagingTemplate.convertAndSend("/topic/alerts", alert);

                log.info("Alert sent via WebSocket: {} - {}", alertType, message);
            }
        }
    }

    private void updateStatus(Server server, ServerStatus newStatus) {
        if (server.getStatus() != newStatus) {
            log.info("Updating status of server {} to {}", server.getName(), newStatus);
            server.setStatus(newStatus);
            serverRepository.save(server);
        }
    }

    @Transactional
    public void enableMaintenanceMode(Long serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found with ID: " + serverId));

        server.setStatus(ServerStatus.MAINTENANCE);
        serverRepository.save(server);

        List<CronJob> jobs = cronJobRepository.findByServer_Id(serverId);
        List<Long> jobIds = jobs.stream().map(CronJob::getId).collect(Collectors.toList());

        cronJobService.deactivateCronJobs(jobIds);

        log.info("Server {} set to MAINTENANCE. {} cron jobs disabled.", server.getName(), jobIds.size());

        // 🔔 Notification directe aux superviseurs
        Set<User> supervisors = server.getClient().getSupervisors();
        String subject = "🔧 Server in Maintenance: " + server.getName();
        String body = String.format("Dear Supervisor,\n\nThe server '%s' has been put into maintenance mode.\n\nClient: %s\n\nRegards,\nMonitoring System",
                server.getName(), server.getClient().getName());

        for (User supervisor : supervisors) {
            if (supervisor.getEmail() != null && !supervisor.getEmail().isBlank()) {
                emailService.sendSimpleEmail(supervisor.getEmail(), subject, body);
            }

        }

        List<User> admins = userRepository.findByRole_Type(TypeRole.ADMIN);
        // Fusionner les deux groupes
        Set<User> allRecipients = new HashSet<>(supervisors);
        allRecipients.addAll(admins);

        Alert savedAlert= alertService.triggerAlert("SERVER_MAINTENANCE",
                String.format("Server '%s', Client: '%s', is now in MAINTENANCE mode. %d cron jobs have been deactivated.",
                        server.getName(), server.getClient().getName(),jobIds.size()));

        for (User user: allRecipients) {
            messagingTemplate.convertAndSend("/topic/admin/alerts/" + user.getId(), savedAlert);
        }

    }


    @Transactional
    public void disableMaintenanceMode(Long serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found with ID: " + serverId));

        server.setStatus(ServerStatus.ACTIVE);
        serverRepository.save(server);

        List<CronJob> jobs = cronJobRepository.findByServer_Id(serverId);
        List<Long> jobIds = jobs.stream().map(CronJob::getId).collect(Collectors.toList());

        cronJobService.activateCronJobs(jobIds);

        log.info("Server {} reactivated. {} cron jobs re-enabled.", server.getName(), jobIds.size());

        // 🔔 Notification directe aux superviseurs
        Set<User> supervisors = server.getClient().getSupervisors();
        String subject = "✅ Server Reactivated: " + server.getName();
        String body = String.format("Dear Supervisor,\n\nThe server '%s' has been reactivated and its cron jobs have been enabled.\n\nClient: %s\n\nRegards,\nMonitoring System",
                server.getName(), server.getClient().getName());

        for (User supervisor : supervisors) {
            if (supervisor.getEmail() != null && !supervisor.getEmail().isBlank()) {
                emailService.sendSimpleEmail(supervisor.getEmail(), subject, body);
            }
        }

        List<User> admins = userRepository.findByRole_Type(TypeRole.ADMIN);
        // Fusionner les deux groupes
        Set<User> allRecipients = new HashSet<>(supervisors);
        allRecipients.addAll(admins);

        Alert savedAlert= alertService.triggerAlert("SERVER_ACTIVE",
                String.format("Server '%s', Client: '%s', has been reactivated. %d cron jobs have been re-enabled.",
                        server.getName(), server.getClient().getName(),jobIds.size()));

        for (User user: allRecipients) {
            messagingTemplate.convertAndSend("/topic/admin/alerts/" + user.getId(), savedAlert);
        }
    }




   @Override
    public List<ServerDto> getAllServersWithStats() {
        return serverRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ServerDto convertToDto(Server server) {
        ServerDto dto = new ServerDto();

        // Données de base
        dto.setId(server.getId());
        dto.setName(server.getName());
        dto.setOs(server.getOs());
        dto.setClient(server.getClient().getName()); // LAZY loading ici
        dto.setStatus(server.getStatus());

        // Calcul des comptes
        dto.setCronJobs(server.getCronJobs().size());
        dto.setApplications(server.getApplications().size());
        dto.setServicesNb(server.getServices().size());

        // Extraction des noms
        dto.setApplicationNames(
                server.getApplications()
                        .stream()
                        .map(Application::getName)   // à adapter selon ton attribut
                        .collect(Collectors.toList())
        );

        dto.setServiceNames(
                server.getServices()
                        .stream()
                        .map(ServiceStatus::getServiceName)       // idem
                        .collect(Collectors.toList())
        );

        return dto;
    }


    @KafkaListener(topics = "system-metrics", groupId = "metricgroup")
    public void consumeSystemMetrics(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(message);

            // ✅ CPU global
            double cpuUser = getDoubleValue(root, "/system/cpu/user/pct", 0.0);
            double cpuSystem = getDoubleValue(root, "/system/cpu/system/pct", 0.0);
            double cpuIdle = getDoubleValue(root, "/system/cpu/idle/pct", 0.0);

            // ✅ Mémoire
            double memUsedPct = getDoubleValue(root, "/system/memory/used/pct", 0.0);
            long memTotal = getIntValue(root, "/system/memory/total", 0);
            long memUsed = getIntValue(root, "/system/memory/used/bytes", 0);

            // ✅ Disque (filesystem global)
            double diskUsedPct = getDoubleValue(root, "/system/filesystem/used/pct", 0.0);
            long diskTotal = getIntValue(root, "/system/filesystem/total", 0);
            long diskUsed = getIntValue(root, "/system/filesystem/used/bytes", 0);

            // 🔹 Création d’un objet DTO
            SystemMetricsDTO dto = new SystemMetricsDTO();
            dto.setCpuUser(round(cpuUser * 100, 2));
            dto.setCpuSystem(round(cpuSystem * 100, 2));
            dto.setCpuIdle(round(cpuIdle * 100, 2));
            dto.setMemUsedPct(round(memUsedPct * 100, 2));
            dto.setMemTotal(memTotal);
            dto.setMemUsed(memUsed);
            dto.setDiskUsedPct(round(diskUsedPct * 100, 2));
            dto.setDiskTotal(diskTotal);
            dto.setDiskUsed(diskUsed);
            dto.setTimestamp(LocalDateTime.now());

            // ✅ Envoi vers Angular via WebSocket
            messagingTemplate.convertAndSend("/topic/system-metrics", dto);

            log.info("📊 System metrics reçues: {}", dto);

        } catch (Exception e) {
            log.error("❌ Erreur Kafka (system-metrics) : {}", message, e);
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

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
