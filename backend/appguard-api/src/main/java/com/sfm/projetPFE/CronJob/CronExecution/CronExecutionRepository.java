package com.sfm.projetPFE.CronJob.CronExecution;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CronExecutionRepository extends JpaRepository<CronExecution, Long> {

    List<CronExecution> findByCronJobIdOrderByStartTimeDesc(Long cronJobId);
    List<CronExecution> findByStatus(ExecutionStatus status);
    void deleteByCronJobId(Long cronJobId);

    List<CronExecution> findByCronJob_Id(Long cronJobId, Pageable pageable);
    List<CronExecution> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<CronExecution> findTop10ByStatusIn(List<ExecutionStatus> statuses, Sort sort);
}

