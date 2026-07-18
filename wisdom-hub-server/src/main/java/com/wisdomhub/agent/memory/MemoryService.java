package com.wisdomhub.agent.memory;

import com.wisdomhub.agent.runtime.AgentExecutionContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;

/**
 * Agent memory boundary.
 */
public interface MemoryService {

    /**
     * Returns the Spring AI memory advisor used by the Agent ChatClient.
     */
    Advisor advisor();

    /**
     * Applies the current conversation id to a ChatClient request.
     */
    ChatClient.ChatClientRequestSpec applyConversation(ChatClient.ChatClientRequestSpec requestSpec,
                                                       AgentExecutionContext context);
}
