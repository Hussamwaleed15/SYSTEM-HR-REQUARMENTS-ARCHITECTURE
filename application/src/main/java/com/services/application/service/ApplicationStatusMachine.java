package com.services.application.service;

import com.services.application.enums.ApplicationStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * State Machine for ApplicationStatus transitions.
 *
 * Valid transitions:
 *   APPLIED        -> [INTERVIEW, REJECTED, WITHDRAWN]
 *   INTERVIEW      -> [UNDER_REVIEW, REJECTED, WITHDRAWN]
 *   UNDER_REVIEW   -> [OFFER_EXTENDED, REJECTED]
 *   OFFER_EXTENDED -> [HIRED, REJECTED]
 *   HIRED          -> [] (terminal)
 *   REJECTED       -> [] (terminal)
 *   WITHDRAWN      -> [] (terminal)
 */
@Component
public class ApplicationStatusMachine {

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> VALID_TRANSITIONS =
            new EnumMap<>(ApplicationStatus.class);

    static {
        VALID_TRANSITIONS.put(ApplicationStatus.APPLIED,
                EnumSet.of(ApplicationStatus.INTERVIEW,
                           ApplicationStatus.REJECTED,
                           ApplicationStatus.WITHDRAWN));

        VALID_TRANSITIONS.put(ApplicationStatus.INTERVIEW,
                EnumSet.of(ApplicationStatus.UNDER_REVIEW,
                           ApplicationStatus.REJECTED,
                           ApplicationStatus.WITHDRAWN));

        VALID_TRANSITIONS.put(ApplicationStatus.UNDER_REVIEW,
                EnumSet.of(ApplicationStatus.OFFER_EXTENDED,
                           ApplicationStatus.REJECTED));

        VALID_TRANSITIONS.put(ApplicationStatus.OFFER_EXTENDED,
                EnumSet.of(ApplicationStatus.HIRED,
                           ApplicationStatus.REJECTED));

        // Terminal states - no outgoing transitions
        VALID_TRANSITIONS.put(ApplicationStatus.HIRED, EnumSet.noneOf(ApplicationStatus.class));
        VALID_TRANSITIONS.put(ApplicationStatus.REJECTED, EnumSet.noneOf(ApplicationStatus.class));
        VALID_TRANSITIONS.put(ApplicationStatus.WITHDRAWN, EnumSet.noneOf(ApplicationStatus.class));
    }

    public boolean isValidTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        Set<ApplicationStatus> allowedNext = VALID_TRANSITIONS.get(currentStatus);
        return allowedNext != null && allowedNext.contains(newStatus);
    }

    public void validateTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        if (!isValidTransition(currentStatus, newStatus)) {
            Set<ApplicationStatus> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
            throw new IllegalStateException(
                    String.format("Invalid status transition: %s -> %s. Allowed transitions from %s: %s",
                            currentStatus, newStatus, currentStatus, allowed));
        }
    }

    public Set<ApplicationStatus> getAllowedTransitions(ApplicationStatus currentStatus) {
        return VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
    }

    public boolean isTerminalStatus(ApplicationStatus status) {
        Set<ApplicationStatus> allowed = VALID_TRANSITIONS.get(status);
        return allowed != null && allowed.isEmpty();
    }
}