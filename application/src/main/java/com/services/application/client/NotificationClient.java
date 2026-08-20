ackage com.services.application.client;

import com.services.application.enums.ApplicationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for Notification Service.
 * Sends email notifications to candidates on status changes.
 */
@Component
@Slf4j
public class NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${services.notification.url}")
    private String notificationUrl;

    public NotificationClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Sends a status change email notification to the candidate.
     * Failure is non-fatal: logged as a warning and execution continues.
     *
     * @param candidateEmail  recipient email address
     * @param applicationId   the application ID
     * @param oldStatus       previous status
     * @param newStatus       new status
     */
    public void sendStatusChangeEmail(String candidateEmail, Long applicationId,
                                      ApplicationStatus oldStatus, ApplicationStatus newStatus) {
        if (candidateEmail == null || candidateEmail.isBlank()) {
            log.warn("Cannot send notification: candidate email is blank for application {}", applicationId);
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("to", candidateEmail);
            payload.put("subject", buildSubject(newStatus));
            payload.put("body", buildBody(applicationId, oldStatus, newStatus));

            ResponseEntity<String> response = restTemplate.postForEntity(notificationUrl, payload, String.class);
            log.info("Status change notification sent to {} for application {} ({} -> {}). Response: {}",
                    candidateEmail, applicationId, oldStatus, newStatus, response.getStatusCode());
        } catch (Exception e) {
            // Non-fatal: notification failure should not rollback the status change
            log.warn("Failed to send status notification to {} for application {}: {}",
                    candidateEmail, applicationId, e.getMessage());
        }
    }

    private String buildSubject(ApplicationStatus newStatus) {
        return switch (newStatus) {
            case INTERVIEW      -> "Interview Scheduled - Your Application Update";
            case UNDER_REVIEW   -> "Your Application is Under Review";
            case OFFER_EXTENDED -> "Congratulations! You Have Received a Job Offer";
            case HIRED          -> "Welcome! You Have Been Hired";
            case REJECTED       -> "Application Status Update";
            case WITHDRAWN      -> "Application Withdrawal Confirmed";
            default             -> "Your Application Status Has Been Updated";
        };
    }

    private String buildBody(Long applicationId, ApplicationStatus oldStatus, ApplicationStatus newStatus) {
        String statusMessage = switch (newStatus) {
            case INTERVIEW      -> "Your application has been moved to the interview stage. You will be contacted shortly with interview details.";
            case UNDER_REVIEW   -> "Your interview was successful and your application is now under review by our team.";
            case OFFER_EXTENDED -> "We are pleased to offer you a position. Please check your email for the full offer details.";
            case HIRED          -> "Congratulations! Your application has been finalized and you are now officially hired. Welcome to the team!";
            case REJECTED       -> "After careful consideration, we regret to inform you that your application was not successful at this time. We encourage you to apply for future opportunities.";
            case WITHDRAWN      -> "Your application withdrawal has been confirmed. We hope to see your application again in the future.";
            default             -> "Your application status has been updated from " + oldStatus + " to " + newStatus + ".";
        };

        return String.format("""
                Dear Candidate,

                %s

                Application Reference ID: %d
                Previous Status: %s
                New Status: %s

                Thank you for your interest.

                Best regards,
                HR Recruitment Team
                """, statusMessage, applicationId, oldStatus, newStatus);
    }
}
