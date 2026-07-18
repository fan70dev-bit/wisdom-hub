package com.wisdomhub.agent.runtime;

/**
 * Internal result returned by {@link AgentRuntime}.
 *
 * <p>This class is separate from HTTP response DTOs so the runtime can evolve
 * independently from controller contracts in later milestones.</p>
 */
public class AgentExecutionResult {

    /**
     * Final answer or infrastructure status message.
     */
    private final String answer;

    /**
     * Unique execution trace identifier.
     */
    private final String traceId;

    /**
     * Logical model provider used by this run.
     */
    private final String provider;

    /**
     * Logical model name used by this run.
     */
    private final String model;

    /**
     * Whether a ChatClient was available.
     */
    private final boolean modelAvailable;

    /**
     * Total runtime latency in milliseconds.
     */
    private final long latencyMs;

    public AgentExecutionResult(String answer, String traceId, String provider, String model,
                                boolean modelAvailable, long latencyMs) {
        this.answer = answer;
        this.traceId = traceId;
        this.provider = provider;
        this.model = model;
        this.modelAvailable = modelAvailable;
        this.latencyMs = latencyMs;
    }

    public String getAnswer() {
        return answer;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public boolean isModelAvailable() {
        return modelAvailable;
    }

    public long getLatencyMs() {
        return latencyMs;
    }
}
