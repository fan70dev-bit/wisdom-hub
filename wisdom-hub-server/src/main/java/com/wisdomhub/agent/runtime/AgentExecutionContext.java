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
     * Current authenticated user's public account id.
     */
    private final String accountId;

    /**
     * Current authenticated user's display name.
     */
    private final String username;

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

    public AgentExecutionContext(String traceId, Long userId, String userEmail, String accountId, String username,
                                 String message, String provider, String model, LocalDateTime startTime) {
        this.traceId = traceId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.accountId = accountId;
        this.username = username;
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

    public String getAccountId() {
        return accountId;
    }

    public String getUsername() {
        return username;
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
