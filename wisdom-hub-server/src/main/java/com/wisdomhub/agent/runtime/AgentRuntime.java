package com.wisdomhub.agent.runtime;

import com.wisdomhub.agent.config.AgentAiProperties;
import com.wisdomhub.agent.dto.AgentChatRequest;
import com.wisdomhub.agent.trace.AgentExecutionTrace;
import com.wisdomhub.agent.trace.AgentToolCallTrace;
import com.wisdomhub.agent.trace.AgentTraceContext;
import com.wisdomhub.context.UserContext;
import com.wisdomhub.entity.User;
import com.wisdomhub.exception.UnauthorizedException;
import com.wisdomhub.mapper.UserMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Minimal Agent runtime for chat integration.
 *
 * <p>Module 2 adds the first model call path. The runtime still does not perform
 * tool calling, database access or trace persistence. It receives a request,
 * resolves execution metadata and delegates the user's message to Spring AI's
 * {@link ChatClient} when one is available.</p>
 */
@Component
public class AgentRuntime {

    private static final Logger log = LoggerFactory.getLogger(AgentRuntime.class);

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final AgentAiProperties aiProperties;
    private final UserMapper userMapper;

    public AgentRuntime(ObjectProvider<ChatClient> chatClientProvider, AgentAiProperties aiProperties,
                        UserMapper userMapper) {
        this.chatClientProvider = chatClientProvider;
        this.aiProperties = aiProperties;
        this.userMapper = userMapper;
    }

    /**
     * Prepares an Agent execution without invoking the model.
     *
     * <p>This method exists so later milestones can plug in a controller and tool
     * calling without changing the foundation objects.</p>
     *
     * @param request future chat request
     * @return infrastructure-level execution result
     */
    public AgentExecutionResult prepare(AgentChatRequest request) {
        LocalDateTime startTime = LocalDateTime.now();
        AgentExecutionContext context = buildContext(request, startTime);
        AgentExecutionContextHolder.set(context);
        boolean modelAvailable = chatClientProvider.getIfAvailable() != null;

        try {
            String answer = modelAvailable
                    ? "AI 基础设施已就绪，但当前 Milestone 尚未开放聊天执行。"
                    : "AI 模型尚未启用或未配置，应用仍可正常启动。";

            long latencyMs = Duration.between(startTime, LocalDateTime.now()).toMillis();
            return new AgentExecutionResult(
                    answer,
                    context.getTraceId(),
                    context.getProvider(),
                    context.getModel(),
                    modelAvailable,
                    latencyMs
            );
        } finally {
            AgentExecutionContextHolder.clear();
        }
    }

    /**
     * Executes the minimal chat flow.
     *
     * <p>This method calls only the configured LLM through Spring AI ChatClient.
     * It does not call any business service, tool, mapper or database.</p>
     *
     * @param request chat request from the future Agent controller
     * @return model answer and execution metadata
     */
    public AgentExecutionResult chat(AgentChatRequest request) {
        LocalDateTime startTime = LocalDateTime.now();
        AgentExecutionContext context = buildContext(request, startTime);
        AgentExecutionContextHolder.set(context);
        AgentExecutionTrace trace = AgentTraceContext.start(
                context.getTraceId(),
                context.getMessage(),
                context.getModel(),
                context.getProvider(),
                startTime
        );
        ChatClient chatClient = chatClientProvider.getIfAvailable();
        boolean success = false;

        try {
            if (chatClient == null) {
                long latencyMs = Duration.between(startTime, LocalDateTime.now()).toMillis();
                success = true;
                return new AgentExecutionResult(
                        "AI 模型尚未启用或未配置，请先配置 DeepSeek API 后再尝试。",
                        context.getTraceId(),
                        context.getProvider(),
                        context.getModel(),
                        false,
                        latencyMs
                );
            }

            String answer = chatClient.prompt(context.getMessage())
                    .call()
                    .content();

            long latencyMs = Duration.between(startTime, LocalDateTime.now()).toMillis();
            success = true;
            return new AgentExecutionResult(
                    answer,
                    context.getTraceId(),
                    context.getProvider(),
                    context.getModel(),
                    true,
                    latencyMs
            );
        } finally {
            finishTrace(trace, startTime, success);
            AgentTraceContext.clear();
            AgentExecutionContextHolder.clear();
        }
    }

    /**
     * Builds execution metadata for a future Agent run.
     */
    private AgentExecutionContext buildContext(AgentChatRequest request, LocalDateTime startTime) {
        User currentUser = loadCurrentUser();
        AgentAiProperties.Provider provider = resolveProvider(request);
        AgentAiProperties.ModelSettings modelSettings = resolveModelSettings(provider);
        String traceId = AgentTraceContext.currentTraceId().orElseGet(() -> UUID.randomUUID().toString());

        return new AgentExecutionContext(
                traceId,
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getAccountId(),
                currentUser.getUsername(),
                request != null ? request.getMessage() : null,
                provider.name().toLowerCase(),
                modelSettings != null ? modelSettings.getModel() : null,
                startTime
        );
    }

    /**
     * Resolves the requested provider, falling back to the configured default.
     */
    private AgentAiProperties.Provider resolveProvider(AgentChatRequest request) {
        if (request != null && StringUtils.hasText(request.getProvider())) {
            try {
                return AgentAiProperties.Provider.valueOf(request.getProvider().trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return aiProperties.getDefaultProvider();
            }
        }
        return aiProperties.getDefaultProvider();
    }

    /**
     * Returns the configured metadata for a provider.
     */
    private AgentAiProperties.ModelSettings resolveModelSettings(AgentAiProperties.Provider provider) {
        if (provider == AgentAiProperties.Provider.OPENAI) {
            return aiProperties.getOpenai();
        }
        if (provider == AgentAiProperties.Provider.OLLAMA) {
            return aiProperties.getOllama();
        }
        return aiProperties.getDeepseek();
    }

    /**
     * Loads the authenticated user created by the existing JWT flow.
     */
    private User loadCurrentUser() {
        Long userId = UserContext.getUserId();
        String email = UserContext.getUserEmail();

        if (userId == null || !StringUtils.hasText(email)) {
            throw new UnauthorizedException("请先登录");
        }

        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new UnauthorizedException("登录用户不存在，请重新登录");
        }

        return user;
    }

    /**
     * Completes and logs the in-memory trace for one Agent execution.
     */
    private void finishTrace(AgentExecutionTrace trace, LocalDateTime startTime, boolean success) {
        LocalDateTime endTime = LocalDateTime.now();
        long elapsedMs = Duration.between(startTime, endTime).toMillis();
        trace.finish(endTime, elapsedMs, success);
        logTrace(trace);
    }

    /**
     * Emits a compact SLF4J trace log for the Agent run.
     */
    private void logTrace(AgentExecutionTrace trace) {
        if (trace.getToolCalls().isEmpty()) {
            log.info("[Agent Trace] traceId={} provider={} model={} tool=none toolCount=0 elapsed={}ms success={}",
                    trace.getTraceId(),
                    trace.getProvider(),
                    trace.getModel(),
                    trace.getElapsedMs(),
                    trace.isSuccess());
            return;
        }

        for (AgentToolCallTrace toolCall : trace.getToolCalls()) {
            log.info("[Agent Trace] traceId={} provider={} model={} tool={} args={} returnCount={} toolElapsed={}ms toolSuccess={} toolCount={} elapsed={}ms success={}",
                    trace.getTraceId(),
                    trace.getProvider(),
                    trace.getModel(),
                    toolCall.getToolName(),
                    toolCall.getArguments(),
                    toolCall.getReturnCount(),
                    toolCall.getElapsedMs(),
                    toolCall.isSuccess(),
                    trace.getToolCount(),
                    trace.getElapsedMs(),
                    trace.isSuccess());
        }
    }
}
