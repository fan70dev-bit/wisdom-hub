package com.wisdomhub.agent.config;

import com.wisdomhub.agent.tool.PostSearchTool;
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
 * ChatClient is also the place where the Agent's first internal tools are made
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
     * enables this path. The post search tool is registered as a default tool so
     * the model can decide when to call it during normal chat.</p>
     *
     * @param chatModel auto-configured Spring AI ChatModel
     * @param postSearchTool internal tool that searches posts through PostService
     * @return the ChatClient used by AgentRuntime
     */
    @Bean
    @ConditionalOnMissingBean(name = "agentChatClient")
    @ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "openai")
    public ChatClient agentChatClient(ChatModel chatModel, PostSearchTool postSearchTool) {
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        你是 Wisdom Hub 的 AI 助手。
                        当用户要求查找、搜索、寻找相关文章或内容时，优先调用可用的文章搜索工具。
                        工具返回文章后，请用中文回答，并包含文章标题、作者、发布时间和简短 AI 总结。
                        如果没有找到相关文章，请明确说明没有找到，并给出可尝试的关键词。
                        """)
                .defaultTools(postSearchTool)
                .build();
    }
}
