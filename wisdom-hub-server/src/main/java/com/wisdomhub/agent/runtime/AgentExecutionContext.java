package com.wisdomhub.agent.runtime;

import java.time.LocalDateTime;

/**
 * Immutable context for one Agent execution.
 *
 * <p>The context carries request, user, model and trace metadata through the
 * Agent layer. It does not perform authorization and it does not access business
 * services.</p>
 */
public class AgentExecutionContext {

    /**
     * Unique execution trace identifier.
     */
    private final String traceId;

    /**
     * Current authenticated user id, if available.
     */
    private final Long userId;

    /**
     * Current authenticated user email, if available.
     */
    private final String userEmail;

    /**
     * User's natural language message.
     */
    private final String message;

    /**
     * Logical provider selected for this execution.
     */
    private final String provider;

    /**
     * Logical model selected for this execution.
     */
    private final String model;

    /**
     * Execution start timestamp.
     */
    private final LocalDateTime startTime;

    public AgentExecutionContext(String traceId, Long userId, String userEmail, String message,
                                 String provider, String model, LocalDateTime startTime) {
        this.traceId = traceId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.message = message;
        this.provider = provider;
        this.model = model;
        this.startTime = startTime;
    }

    public String getTraceId() {
        return traceId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getMessage() {
        return message;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
}
