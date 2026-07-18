package com.wisdomhub.agent.runtime;

import java.util.Optional;

/**
 * Thread-local holder for the current Agent execution context.
 *
 * <p>Tools invoked by Spring AI can use this holder to read the authenticated
 * Agent user without changing controller DTOs or tool method signatures. The
 * runtime must clear the holder after each request.</p>
 */
public final class AgentExecutionContextHolder {

    private static final ThreadLocal<AgentExecutionContext> CURRENT_CONTEXT = new ThreadLocal<>();

    private AgentExecutionContextHolder() {
    }

    /**
     * Stores the current Agent execution context.
     */
    public static void set(AgentExecutionContext context) {
        CURRENT_CONTEXT.set(context);
    }

    /**
     * Returns the current Agent execution context if one is active.
     */
    public static Optional<AgentExecutionContext> currentContext() {
        return Optional.ofNullable(CURRENT_CONTEXT.get());
    }

    /**
     * Clears the current Agent execution context.
     */
    public static void clear() {
        CURRENT_CONTEXT.remove();
    }
}
