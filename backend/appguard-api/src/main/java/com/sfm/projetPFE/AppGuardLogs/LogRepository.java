package com.sfm.projetPFE.AppGuardLogs;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface LogRepository extends MongoRepository<Log, String> {
}
