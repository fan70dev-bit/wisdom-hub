package com.wisdomhub.agent.controller;

import com.wisdomhub.agent.dto.AgentChatRequest;
import com.wisdomhub.agent.dto.AgentChatResponse;
import com.wisdomhub.agent.runtime.AgentExecutionResult;
import com.wisdomhub.agent.runtime.AgentRuntime;
import com.wisdomhub.context.UserContext;
import com.wisdomhub.dto.Result;
import com.wisdomhub.exception.UnauthorizedException;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * HTTP entry point for the minimal Agent chat integration.
 *
 * <p>This controller exposes only the first chat endpoint. It does not implement
 * tool calling, trace persistence, workflow orchestration or database access.</p>
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentRuntime agentRuntime;

    public AgentController(AgentRuntime agentRuntime) {
        this.agentRuntime = agentRuntime;
    }

    /**
     * Executes a minimal LLM chat request.
     *
     * <p>The request is passed directly to {@link AgentRuntime}, which delegates
     * to Spring AI ChatClient when a model is configured.</p>
     *
     * @param request user chat request
     * @return model response wrapped in the existing API response envelope
     */
    @PostMapping("/chat")
    public Result<AgentChatResponse> chat(@Valid @RequestBody AgentChatRequest request) {
        ensureAuthenticated();
        AgentExecutionResult result = agentRuntime.chat(request);
        AgentChatResponse response = new AgentChatResponse(
                result.getAnswer(),
                result.getTraceId(),
                result.getProvider(),
                result.getModel(),
                result.isModelAvailable()
        );
        return Result.success(response);
    }

    /**
     * Streams an Agent chat response over Server-Sent Events.
     *
     * <p>The synchronous endpoint above remains unchanged. This endpoint uses
     * Spring AI's Reactor-based {@code ChatClient.stream()} path and lets the
     * runtime keep tool calling and trace logging active for the whole stream.</p>
     *
     * @param request user chat request
     * @return streamed answer chunks as SSE data events
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@Valid @RequestBody AgentChatRequest request) {
        ensureAuthenticated();
        return agentRuntime.chatStream(request)
                .map(chunk -> ServerSentEvent.builder(chunk).event("message").build());
    }

    /**
     * Requires the existing JWT interceptor to have populated UserContext.
     */
    private void ensureAuthenticated() {
        if (UserContext.getUserId() == null || !StringUtils.hasText(UserContext.getUserEmail())) {
            throw new UnauthorizedException("请先登录");
        }
    }
}
