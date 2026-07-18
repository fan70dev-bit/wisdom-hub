package com.wisdomhub.agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI ChatClient configuration for the Agent foundation layer.
 *
 * <p>The configuration is deliberately conditional. It creates a
 * {@link ChatClient} only when Spring AI has already created a {@link ChatModel}.
 * In local development, all model auto-configuration can stay disabled and the
 * application still starts normally without any API key.</p>
 */
@Configuration
@EnableConfigurationProperties(AgentAiProperties.class)
public class AgentChatClientConfig {

    /**
     * Creates the shared Agent ChatClient when a ChatModel is available.
     *
     * <p>This method does not call the remote model API. It only wraps the
     * already-configured Spring AI model in a ChatClient. If no model bean exists,
     * this bean is skipped.</p>
     *
     * @param chatModelProvider provider for an auto-configured Spring AI ChatModel
     * @return the ChatClient used by AgentRuntime
     */
    @Bean
    @ConditionalOnBean(ChatModel.class)
    @ConditionalOnMissingBean(name = "agentChatClient")
    public ChatClient agentChatClient(ObjectProvider<ChatModel> chatModelProvider) {
        ChatModel chatModel = chatModelProvider.getIfAvailable();
        return ChatClient.builder(chatModel).build();
    }
}
