package com.ems.repository;

import com.ems.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    List<Candidate> findByJobPostingId(Long jobPostingId);
    List<Candidate> findByStage(Candidate.CandidateStage stage);
    Optional<Candidate> findByEmail(String email);
    List<Candidate> findAllByOrderByAppliedDateDesc();
}
