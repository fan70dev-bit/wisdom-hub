package com.wisdomhub.agent.config;

import com.wisdomhub.agent.tool.KnowledgeTool;
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
 * and the application still starts normally without any API key. The configured
 * ChatClient is also the place where the Agent's internal tools are made
 * available to the model.</p>
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
     * enables this path. The knowledge tool is registered as the single search
     * entry point so the model has one consistent knowledge query surface.</p>
     *
     * @param chatModel auto-configured Spring AI ChatModel
     * @param knowledgeTool internal tool that searches the knowledge router
     * @return the ChatClient used by AgentRuntime
     */
    @Bean
    @ConditionalOnMissingBean(name = "agentChatClient")
    @ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "openai")
    public ChatClient agentChatClient(ChatModel chatModel, KnowledgeTool knowledgeTool) {
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        你是 Wisdom Hub 的 AI 助手。
                        当用户要求查找、搜索、寻找相关文章、文档或知识内容时，必须调用知识搜索工具。
                        工具返回结果后，请用中文回答，并包含标题、来源和简短 AI 总结。
                        如果没有找到相关文章，请明确说明没有找到，并给出可尝试的关键词。
                        """)
                .defaultTools(knowledgeTool)
                .build();
    }
}
