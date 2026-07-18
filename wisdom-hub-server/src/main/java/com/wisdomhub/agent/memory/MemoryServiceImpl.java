package com.wisdomhub.agent.memory;

import com.wisdomhub.agent.runtime.AgentExecutionContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

/**
 * Default Agent memory service backed by Spring AI ChatMemory.
 */
@Service
public class MemoryServiceImpl implements MemoryService {

    private static final int MAX_MESSAGES = 20;

    private final ChatMemory chatMemory;
    private final Advisor advisor;

    public MemoryServiceImpl(ChatMemoryRepository chatMemoryRepository) {
        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(MAX_MESSAGES)
                .build();
        this.advisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    @Override
    public Advisor advisor() {
        return advisor;
    }

    @Override
    public ChatClient.ChatClientRequestSpec applyConversation(ChatClient.ChatClientRequestSpec requestSpec,
                                                              AgentExecutionContext context) {
        return requestSpec.advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId(context)));
    }

    private String conversationId(AgentExecutionContext context) {
        return String.valueOf(context.getUserId());
    }
}
