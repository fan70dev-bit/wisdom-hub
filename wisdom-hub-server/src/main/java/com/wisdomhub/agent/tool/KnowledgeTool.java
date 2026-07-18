package com.wisdomhub.agent.tool;

import com.wisdomhub.agent.knowledge.KnowledgeService;
import com.wisdomhub.agent.knowledge.SearchResult;
import com.wisdomhub.agent.trace.AgentTraceContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Spring AI tool for searching normalized Agent knowledge.
 *
 * <p>This tool depends on {@link KnowledgeService} instead of a concrete source.
 * It is the preferred search surface for future providers such as web search or
 * enterprise knowledge bases.</p>
 */
@Component
public class KnowledgeTool {

    private final KnowledgeService knowledgeService;

    public KnowledgeTool(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    /**
     * Searches the Agent knowledge router by keyword.
     *
     * @param keyword keyword extracted from the user request
     * @return concise normalized results for LLM summarization
     */
    @Tool(name = "search_knowledge", description = "Search Wisdom Hub local articles and routed knowledge sources by keyword. Always use this tool when the user asks to search, find, look up, or retrieve related articles, posts, documents, or knowledge content.")
    public List<SearchResult> searchKnowledge(
            @ToolParam(description = "Keyword to search, for example Redis, Spring AI, MySQL, workflow.")
            String keyword) {
        long startNanos = System.nanoTime();
        String normalizedKeyword = normalizeKeyword(keyword);
        boolean success = false;
        int returnCount = 0;

        try {
            List<SearchResult> results = knowledgeService.search(normalizedKeyword);
            returnCount = results != null ? results.size() : 0;
            success = true;
            return results;
        } finally {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            AgentTraceContext.recordToolCall(
                    "KnowledgeTool.searchKnowledge",
                    "keyword=" + normalizedKeyword,
                    returnCount,
                    elapsedMs,
                    success
            );
        }
    }

    /**
     * Normalizes model-provided search text before sending it to the knowledge service.
     */
    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return " ";
        }
        String normalized = keyword.trim();
        return normalized.length() > 50 ? normalized.substring(0, 50) : normalized;
    }
}
