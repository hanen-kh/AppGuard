package com.sfm.projetPFE.ServiceStatus;

import java.util.List;

public interface ServiceStatusService {

    double getDiskUsageForPath(String path);

    double getMemoryUsageForService(String serviceName);

    double getCpuUsageForService(String serviceName);

    void checkService(String serviceName, String host, int port);

    List<ServiceStatus> getAllServiceStatuses();
}
