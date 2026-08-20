ackage com.services.application.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Client for Candidate Service cross-service validation.
 * Verifies that a candidate exists before allowing application creation.
 */
@Component
@Slf4j
public class CandidateServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.candidate-service.url}")
    private String candidateServiceUrl;

    public CandidateServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Validates that the candidate exists in the candidate service.
     * Throws RuntimeException if candidate not found.
     */
    public void validateCandidateExists(Long candidateId) {
        String url = candidateServiceUrl + "/api/candidates/" + candidateId;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> candidate = restTemplate.getForObject(url, Map.class);
            if (candidate == null) {
                throw new RuntimeException("Candidate not found with ID: " + candidateId);
            }
            log.info("Candidate {} validated: exists", candidateId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Candidate not found with ID: " + candidateId);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to validate candidate {}: {}", candidateId, e.getMessage());
            throw new RuntimeException("Unable to verify candidate. Please try again later.");
        }
    }

    /**
     * Fetches the candidate email from candidate service.
     * Returns null if not found or service is unavailable.
     */
    public String getCandidateEmail(Long candidateId) {
        String url = candidateServiceUrl + "/api/candidates/" + candidateId;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> candidate = restTemplate.getForObject(url, Map.class);
            return candidate != null ? String.valueOf(candidate.getOrDefault("email", "")) : null;
        } catch (Exception e) {
            log.warn("Could not fetch candidate email for {}: {}", candidateId, e.getMessage());
            return null;
        }
    }
}
