package com.wisdomhub.agent.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request object for a future Agent chat entry point.
 *
 * <p>Milestone 1 does not expose a controller. This DTO defines the stable input
 * contract that a later controller can pass into {@code AgentRuntime}.</p>
 */
public class AgentChatRequest {

    /**
     * Natural language message from the current user.
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * Optional conversation identifier reserved for future multi-turn chat.
     */
    private String conversationId;

    /**
     * Optional requested provider name reserved for future model routing.
     */
    private String provider;

    public AgentChatRequest() {
    }

    public AgentChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
