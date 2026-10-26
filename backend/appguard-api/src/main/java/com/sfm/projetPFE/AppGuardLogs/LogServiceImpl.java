package com.sfm.projetPFE.AppGuardLogs;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogRepository logRepository;

    private PrintWriter writer;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void createLog(int idUser, String serviceName, String methodName, LogLevel level,
                          long startTime, String stackTrace) {
        Log log = new Log();
        log.setId(UUID.randomUUID().toString());
        log.setIdUser(idUser);
        log.setServiceName(serviceName);
        log.setMethodName(methodName);
        log.setLevel(level);
        log.setTimestamp(LocalDateTime.now());
        log.setLatency(System.currentTimeMillis() - startTime);

        log.setStackTrace(stackTrace);
        System.out.println("logging");
        logRepository.save(log);
    }








    }