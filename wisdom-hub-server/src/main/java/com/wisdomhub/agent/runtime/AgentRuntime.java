package com.wisdomhub.agent.runtime;

import com.wisdomhub.agent.config.AgentAiProperties;
import com.wisdomhub.agent.dto.AgentChatRequest;
import com.wisdomhub.context.UserContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
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

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final AgentAiProperties aiProperties;

    public AgentRuntime(ObjectProvider<ChatClient> chatClientProvider, AgentAiProperties aiProperties) {
        this.chatClientProvider = chatClientProvider;
        this.aiProperties = aiProperties;
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
        boolean modelAvailable = chatClientProvider.getIfAvailable() != null;

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
        ChatClient chatClient = chatClientProvider.getIfAvailable();

        if (chatClient == null) {
            long latencyMs = Duration.between(startTime, LocalDateTime.now()).toMillis();
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
        return new AgentExecutionResult(
                answer,
                context.getTraceId(),
                context.getProvider(),
                context.getModel(),
                true,
                latencyMs
        );
    }

    /**
     * Builds execution metadata for a future Agent run.
     */
    private AgentExecutionContext buildContext(AgentChatRequest request, LocalDateTime startTime) {
        AgentAiProperties.Provider provider = resolveProvider(request);
        AgentAiProperties.ModelSettings modelSettings = resolveModelSettings(provider);

        return new AgentExecutionContext(
                UUID.randomUUID().toString(),
                UserContext.getUserId(),
                UserContext.getUserEmail(),
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
}
