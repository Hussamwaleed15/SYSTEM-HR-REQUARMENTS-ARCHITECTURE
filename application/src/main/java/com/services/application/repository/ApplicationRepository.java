package com.services.application.repository;

import com.services.application.model.Application;
import com.services.application.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByCandidateId(Long candidateId);

    List<Application> findByJobId(Long jobId);

    List<Application> findByStatus(ApplicationStatus status);

    List<Application> findByStatusIn(List<ApplicationStatus> statuses);

    List<Application> findByEvaluationScoreGreaterThanEqual(Double score);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Application a WHERE a.candidateId = :candidateId AND a.jobId = :jobId")
    boolean existsByCandidateIdAndJobId(@Param("candidateId") Long candidateId, @Param("jobId") Long jobId);

    Optional<Application> findByCandidateIdAndJobId(Long candidateId, Long jobId);

    Optional<Application> findByTrackingId(String trackingId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Application a WHERE a.trackingId = :trackingId")
    boolean existsByTrackingId(@Param("trackingId") String trackingId);

    List<Application> findByApplicationDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByStatus(ApplicationStatus status);

    long countByApplicationDateAfter(LocalDateTime date);

    @Query("SELECT AVG(a.aiMatchScore) FROM Application a")
    Double getAverageAiMatchScore();

    @Query("SELECT MAX(a.aiMatchScore) FROM Application a")
    Double getMaxAiMatchScore();

    @Query("SELECT a.status, COUNT(a) FROM Application a GROUP BY a.status")
    List<Object[]> countGroupedByStatusRaw();

    default Map<ApplicationStatus, Long> countGroupedByStatus() {
        List<Object[]> results = countGroupedByStatusRaw();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (ApplicationStatus) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Query("SELECT MONTH(a.applicationDate) as month, COUNT(a) FROM Application a " +
            "WHERE a.applicationDate >= :startDate GROUP BY MONTH(a.applicationDate)")
    List<Object[]> countByMonthRaw(@Param("startDate") LocalDateTime startDate);

    default Map<String, Long> countByMonth() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(12);
        List<Object[]> results = countByMonthRaw(startDate);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> "Month " + row[0],
                        row -> (Long) row[1]
                ));
    }
}