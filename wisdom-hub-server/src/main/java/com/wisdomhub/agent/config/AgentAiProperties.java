package com.wisdomhub.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Wisdom Hub's AI foundation layer.
 *
 * <p>This class owns only the application's AI selection settings. It does not
 * create model clients and it does not call any remote model API. Spring AI's
 * provider-specific settings still live under {@code spring.ai.*}; these
 * properties describe how the Agent layer should choose and label providers in
 * future milestones.</p>
 */
@ConfigurationProperties(prefix = "wisdomhub.agent.ai")
public class AgentAiProperties {

    /**
     * Master switch for the Agent AI foundation.
     *
     * <p>The default is {@code false} so local development can start without any
     * AI API key. When the project is ready to create model clients, enable this
     * flag and configure the matching {@code spring.ai.*} provider settings.</p>
     */
    private boolean enabled = false;

    /**
     * The provider preferred by the Agent layer when more than one model is
     * configured.
     */
    private Provider defaultProvider = Provider.DEEPSEEK;

    /**
     * OpenAI-compatible cloud model settings used by Agent metadata and future
     * routing logic.
     */
    private ModelSettings openai = new ModelSettings("gpt-4.1-mini");

    /**
     * DeepSeek settings. DeepSeek can be connected through Spring AI's OpenAI
     * compatible client by changing the OpenAI base URL in configuration.
     */
    private ModelSettings deepseek = new ModelSettings("deepseek-chat");

    /**
     * Local Ollama model settings.
     */
    private ModelSettings ollama = new ModelSettings("qwen2.5:7b");

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Provider getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(Provider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public ModelSettings getOpenai() {
        return openai;
    }

    public void setOpenai(ModelSettings openai) {
        this.openai = openai;
    }

    public ModelSettings getDeepseek() {
        return deepseek;
    }

    public void setDeepseek(ModelSettings deepseek) {
        this.deepseek = deepseek;
    }

    public ModelSettings getOllama() {
        return ollama;
    }

    public void setOllama(ModelSettings ollama) {
        this.ollama = ollama;
    }

    /**
     * Supported model provider identifiers for the Agent layer.
     */
    public enum Provider {
        OPENAI,
        DEEPSEEK,
        OLLAMA
    }

    /**
     * Minimal provider metadata used before full model routing exists.
     */
    public static class ModelSettings {

        /**
         * Whether this provider is allowed for Agent model routing.
         */
        private boolean enabled = false;

        /**
         * Logical model name displayed in traces and responses.
         */
        private String model;

        /**
         * Optional API base URL. This is metadata for now; Spring AI still reads
         * provider client settings from {@code spring.ai.*}.
         */
        private String baseUrl;

        /**
         * Default sampling temperature for future ChatClient options.
         */
        private Double temperature = 0.2;

        /**
         * Default maximum output tokens for future ChatClient options.
         */
        private Integer maxTokens = 1024;

        public ModelSettings() {
        }

        public ModelSettings(String model) {
            this.model = model;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Integer getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
        }
    }
}
