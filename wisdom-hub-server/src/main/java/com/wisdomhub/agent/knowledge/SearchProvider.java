package com.wisdomhub.agent.knowledge;

import java.util.List;

/**
 * Generic search provider abstraction for the Agent knowledge router.
 *
 * <p>Implementations can search different knowledge sources, such as local
 * Wisdom Hub posts, web search, or enterprise knowledge bases. Providers must
 * return normalized {@link SearchResult} objects and hide their own data-source
 * details from tools.</p>
 */
public interface SearchProvider {

    /**
     * Searches a knowledge source by keyword.
     *
     * @param keyword keyword extracted from the user request
     * @return normalized search results
     */
    List<SearchResult> search(String keyword);
}
