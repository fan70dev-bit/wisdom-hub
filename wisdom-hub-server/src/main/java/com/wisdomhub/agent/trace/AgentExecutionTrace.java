package com.wisdomhub.agent.trace;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory execution trace for one Agent chat request.
 *
 * <p>The trace records the runtime chain for observability only. It is not
 * mapped to a database table and is discarded after the request finishes and the
 * final log line has been emitted.</p>
 */
public class AgentExecutionTrace {

    /**
     * Unique trace identifier for this Agent run.
     */
    private final String traceId;

    /**
     * User's natural language request.
     */
    private final String userMessage;

    /**
     * Logical model name used for the run.
     */
    private final String model;

    /**
     * Logical provider name used for the run.
     */
    private final String provider;

    /**
     * Execution start time.
     */
    private final LocalDateTime startTime;

    /**
     * Execution end time.
     */
    private LocalDateTime endTime;

    /**
     * Total elapsed time in milliseconds.
     */
    private long elapsedMs;

    /**
     * Whether the whole Agent run succeeded.
     */
    private boolean success;

    /**
     * Tool call entries captured during this run.
     */
    private final List<AgentToolCallTrace> toolCalls = new ArrayList<>();

    public AgentExecutionTrace(String traceId, String userMessage, String model, String provider,
                               LocalDateTime startTime) {
        this.traceId = traceId;
        this.userMessage = userMessage;
        this.model = model;
        this.provider = provider;
        this.startTime = startTime;
    }

    /**
     * Records one tool call in this execution.
     */
    public void addToolCall(AgentToolCallTrace toolCall) {
        if (toolCall != null) {
            this.toolCalls.add(toolCall);
        }
    }

    /**
     * Marks the execution as finished.
     */
    public void finish(LocalDateTime endTime, long elapsedMs, boolean success) {
        this.endTime = endTime;
        this.elapsedMs = elapsedMs;
        this.success = success;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getModel() {
        return model;
    }

    public String getProvider() {
        return provider;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<AgentToolCallTrace> getToolCalls() {
        return Collections.unmodifiableList(toolCalls);
    }

    public int getToolCount() {
        return toolCalls.size();
    }
}
