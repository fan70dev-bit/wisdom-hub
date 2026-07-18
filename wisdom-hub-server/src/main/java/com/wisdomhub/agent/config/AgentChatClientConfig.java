package com.wisdomhub.agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI ChatClient configuration for the Agent foundation layer.
 *
 * <p>The configuration is deliberately conditional. It creates a
 * {@link ChatClient} only when chat model auto-configuration has been explicitly
 * enabled. In local development, chat model auto-configuration can stay disabled
 * and the application still starts normally without any API key.</p>
 */
@Configuration
@EnableConfigurationProperties(AgentAiProperties.class)
public class AgentChatClientConfig {

    /**
     * Creates the shared Agent ChatClient when OpenAI-compatible chat is enabled.
     *
     * <p>This method does not call the remote model API. It only wraps the
     * already-configured Spring AI model in a ChatClient. DeepSeek uses Spring
     * AI's OpenAI-compatible client, so {@code spring.ai.model.chat=openai}
     * enables this path.</p>
     *
     * @param chatModel auto-configured Spring AI ChatModel
     * @return the ChatClient used by AgentRuntime
     */
    @Bean
    @ConditionalOnMissingBean(name = "agentChatClient")
    @ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "openai")
    public ChatClient agentChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
