package com.ems.repository;

import com.ems.entity.AppraisalCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppraisalCycleRepository extends JpaRepository<AppraisalCycle, Long> {
    Optional<AppraisalCycle> findFirstByStatus(AppraisalCycle.CycleStatus status);
    Optional<AppraisalCycle> findByPeriodAndYear(String period, Integer year);
}
