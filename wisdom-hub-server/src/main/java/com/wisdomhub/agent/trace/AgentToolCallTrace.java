package com.wisdomhub.agent.trace;

/**
 * In-memory trace entry for one Agent tool call.
 *
 * <p>This object is intentionally not a persistence entity. It captures only the
 * minimum information needed to understand which tool was called during a single
 * Agent execution.</p>
 */
public class AgentToolCallTrace {

    /**
     * Human-readable tool name.
     */
    private final String toolName;

    /**
     * Sanitized argument summary.
     */
    private final String arguments;

    /**
     * Number of result items returned by the tool.
     */
    private final Integer returnCount;

    /**
     * Tool execution duration in milliseconds.
     */
    private final long elapsedMs;

    /**
     * Whether the tool call completed successfully.
     */
    private final boolean success;

    public AgentToolCallTrace(String toolName, String arguments, Integer returnCount, long elapsedMs, boolean success) {
        this.toolName = toolName;
        this.arguments = arguments;
        this.returnCount = returnCount;
        this.elapsedMs = elapsedMs;
        this.success = success;
    }

    public String getToolName() {
        return toolName;
    }

    public String getArguments() {
        return arguments;
    }

    public Integer getReturnCount() {
        return returnCount;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public boolean isSuccess() {
        return success;
    }
}
