package com.wisdomhub.agent.knowledge;

/**
 * Normalized search result returned by knowledge providers.
 *
 * <p>The model intentionally contains only title, content and source so future
 * providers can be mixed without leaking provider-specific fields into the
 * Agent tool contract.</p>
 */
public class SearchResult {

    /**
     * Result title.
     */
    private final String title;

    /**
     * Short result content or excerpt.
     */
    private final String content;

    /**
     * Source identifier, such as {@code local-post:123} or a future web URL.
     */
    private final String source;

    public SearchResult(String title, String content, String source) {
        this.title = title;
        this.content = content;
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSource() {
        return source;
    }
}
