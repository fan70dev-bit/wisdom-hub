package com.wisdomhub.agent.trace;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Thread-local holder for the current Agent execution trace.
 *
 * <p>Spring AI tool calls execute as part of the same request flow, so a
 * ThreadLocal context lets tools append trace entries without changing the
 * public controller contract or DTOs. The runtime must always call
 * {@link #clear()} in a finally block.</p>
 */
public final class AgentTraceContext {

    private static final ThreadLocal<AgentExecutionTrace> CURRENT_TRACE = new ThreadLocal<>();

    private AgentTraceContext() {
    }

    /**
     * Returns the current trace id if a trace is already active.
     */
    public static Optional<String> currentTraceId() {
        AgentExecutionTrace trace = CURRENT_TRACE.get();
        return trace == null ? Optional.empty() : Optional.of(trace.getTraceId());
    }

    /**
     * Starts a trace or reuses an existing one on the current thread.
     */
    public static AgentExecutionTrace start(String traceId, String userMessage, String model, String provider,
                                            LocalDateTime startTime) {
        AgentExecutionTrace existingTrace = CURRENT_TRACE.get();
        if (existingTrace != null) {
            return existingTrace;
        }

        AgentExecutionTrace trace = new AgentExecutionTrace(traceId, userMessage, model, provider, startTime);
        CURRENT_TRACE.set(trace);
        return trace;
    }

    /**
     * Adds a tool call entry to the current trace when a trace is active.
     */
    public static void recordToolCall(String toolName, String arguments, Integer returnCount,
                                      long elapsedMs, boolean success) {
        AgentExecutionTrace trace = CURRENT_TRACE.get();
        if (trace != null) {
            trace.addToolCall(new AgentToolCallTrace(toolName, arguments, returnCount, elapsedMs, success));
        }
    }

    /**
     * Returns the current trace, if any.
     */
    public static Optional<AgentExecutionTrace> currentTrace() {
        return Optional.ofNullable(CURRENT_TRACE.get());
    }

    /**
     * Clears the current trace to avoid leaking request state between threads.
     */
    public static void clear() {
        CURRENT_TRACE.remove();
    }
}
