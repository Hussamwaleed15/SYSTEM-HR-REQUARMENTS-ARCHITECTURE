package com.services.candidate.repository;

import com.services.candidate.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByEmail(String email);

    List<Candidate> findByIsEmployedTrue();

    List<Candidate> findByIsEmployedFalse();

    List<Candidate> findBySkillsContainingIgnoreCase(String skill);

    List<Candidate> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    List<Candidate> findByExperienceYearsBetween(Integer minYears, Integer maxYears);

    List<Candidate> findByAiValidatedTrue();

    List<Candidate> findByAiValidatedFalse();

    long countByIsEmployedTrue();

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Candidate c WHERE c.email = :email")
    boolean existsByEmail(@Param("email") String email);
}