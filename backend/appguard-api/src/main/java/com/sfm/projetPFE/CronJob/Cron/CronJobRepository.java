package com.sfm.projetPFE.CronJob.Cron;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CronJobRepository extends JpaRepository<CronJob, Long> {
    List<CronJob> findByStatus(CronStatus cronStatus);

    List<CronJob> findByServer_Id(Long serverId);
}
