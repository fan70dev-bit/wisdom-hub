package com.wisdomhub.agent.knowledge;

import com.wisdomhub.dto.PageResult;
import com.wisdomhub.dto.PostVO;
import com.wisdomhub.service.PostService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Local knowledge provider backed by existing Wisdom Hub posts.
 *
 * <p>This provider reuses {@link PostService} and does not access Mapper or SQL
 * directly. It adapts post search results into the normalized
 * {@link SearchResult} model used by the Agent knowledge router.</p>
 */
@Component("localSearchProvider")
public class LocalSearchProvider implements SearchProvider {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 5;
    private static final int MAX_CONTENT_LENGTH = 240;

    private final PostService postService;

    public LocalSearchProvider(PostService postService) {
        this.postService = postService;
    }

    @Override
    public List<SearchResult> search(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        PageResult<PostVO> pageResult = postService.search(normalizedKeyword, DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE);

        List<SearchResult> results = new ArrayList<>();
        if (pageResult.getList() == null) {
            return results;
        }

        for (PostVO post : pageResult.getList()) {
            results.add(new SearchResult(
                    normalizeTitle(post),
                    buildContent(post),
                    "local-post:" + post.getId()
            ));
        }
        return results;
    }

    /**
     * Normalizes model-provided search text before calling PostService.
     */
    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return " ";
        }
        String normalized = keyword.trim();
        return normalized.length() > 50 ? normalized.substring(0, 50) : normalized;
    }

    /**
     * Provides a stable title when a post has no explicit title.
     */
    private String normalizeTitle(PostVO post) {
        if (StringUtils.hasText(post.getTitle())) {
            return post.getTitle();
        }
        return "未命名文章 #" + post.getId();
    }

    /**
     * Builds a concise content field for LLM summarization.
     */
    private String buildContent(PostVO post) {
        String content = post.getContent();
        if (!StringUtils.hasText(content)) {
            return "";
        }

        String plainText = content
                .replaceAll("!\\[[^]]*]\\([^)]*\\)", " ")
                .replaceAll("\\[[^]]*]\\([^)]*\\)", " ")
                .replaceAll("[#>*_`\\-]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (plainText.length() <= MAX_CONTENT_LENGTH) {
            return plainText;
        }
        return plainText.substring(0, MAX_CONTENT_LENGTH) + "...";
    }
}
