package com.sfm.projetPFE.AppGuardLogs;

import java.util.List;

public interface LogService {



    void createLog(int idUser, String serviceName, String methodName, LogLevel level,
                   long startTime, String stackTrace);


}
