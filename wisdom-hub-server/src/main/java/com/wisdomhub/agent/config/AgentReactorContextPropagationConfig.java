package com.wisdomhub.agent.config;

import com.wisdomhub.agent.runtime.AgentExecutionContextHolder;
import com.wisdomhub.agent.trace.AgentTraceContext;
import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

/**
 * Registers Agent ThreadLocal state for Reactor context propagation.
 */
@Configuration
public class AgentReactorContextPropagationConfig {

    private static final String AGENT_EXECUTION_CONTEXT_KEY = "wisdomhub.agent.executionContext";
    private static final String AGENT_TRACE_CONTEXT_KEY = "wisdomhub.agent.traceContext";

    @PostConstruct
    public void registerAgentContextPropagation() {
        ContextRegistry registry = ContextRegistry.getInstance();

        registry.registerThreadLocalAccessor(
                AGENT_EXECUTION_CONTEXT_KEY,
                () -> AgentExecutionContextHolder.currentContext().orElse(null),
                AgentExecutionContextHolder::set,
                AgentExecutionContextHolder::clear
        );
        registry.registerThreadLocalAccessor(
                AGENT_TRACE_CONTEXT_KEY,
                () -> AgentTraceContext.currentTrace().orElse(null),
                AgentTraceContext::set,
                AgentTraceContext::clear
        );

        Hooks.enableAutomaticContextPropagation();
    }
}
