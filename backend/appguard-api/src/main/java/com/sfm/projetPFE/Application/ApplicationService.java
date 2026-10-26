package com.sfm.projetPFE.Application;

import com.sfm.projetPFE.Alert.NewEntityAlert;
import com.sfm.projetPFE.LogEntry.LogEntry;
import com.sfm.projetPFE.Server.Server;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ApplicationService {

     Application save(Application app);

     List<Application> getAll() ;

     Optional<Application> getById(Long id);

     void delete(Long id) ;

    List<Application> getByIndex(String index) ;

    ApplicationDto getApplicationByName(String appName);

    List<ApplicationDto> getAllApplications();

    List<LogEntry> getLogsByApplicationName(String applicationName);

    List<LogEntry> getLogsFiltered(String applicationName, String level, String startDate, String endDate);

    void checkNewApplication(String applicationName, String hostIp, String hostName, String os, String clientName);
    void processPendingEntities();
    void processApplicationChunk(List<String> appChunk);
    void processApplication(ApplicationCheckData checkData, Map<String, Application> appCache, Server server, List<NewEntityAlert> alerts);
    void saveEntitiesBatch(Map<String, Application> appCache, Map<String, Server> serverCache);
    void sendAlerts(List<NewEntityAlert> alerts);
    String serializeCheckData(ApplicationCheckData data);
    ApplicationCheckData deserializeCheckData(String data);
    void initializeCache();

}
