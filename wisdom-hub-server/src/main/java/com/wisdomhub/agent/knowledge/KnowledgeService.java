package com.wisdomhub.agent.knowledge;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Unified knowledge search service for Agent tools.
 *
 * <p>The current implementation routes only to the local Wisdom Hub provider.
 * The service boundary keeps tools independent from concrete providers so
 * future web or enterprise providers can be added without changing tool method
 * contracts.</p>
 */
@Service
public class KnowledgeService {

    private final SearchProvider searchProvider;

    public KnowledgeService(@Qualifier("localSearchProvider") SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    /**
     * Searches the currently configured knowledge provider.
     *
     * @param keyword search keyword
     * @return normalized search results
     */
    public List<SearchResult> search(String keyword) {
        return searchProvider.search(keyword);
    }
}
