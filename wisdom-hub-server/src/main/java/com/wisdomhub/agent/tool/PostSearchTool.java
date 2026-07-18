package com.wisdomhub.agent.tool;

import com.wisdomhub.dto.PageResult;
import com.wisdomhub.dto.PostVO;
import com.wisdomhub.service.PostService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring AI tool for searching Wisdom Hub posts.
 *
 * <p>This tool is the first bridge between the Agent layer and the existing
 * business layer. It calls {@link PostService} only. It never accesses Mapper,
 * SQL, database connections or persistence objects directly.</p>
 */
@Component
public class PostSearchTool {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 5;
    private static final int MAX_EXCERPT_LENGTH = 180;

    private final PostService postService;

    public PostSearchTool(PostService postService) {
        this.postService = postService;
    }

    /**
     * Searches public posts by keyword.
     *
     * <p>The model should call this method when a user asks to find related
     * posts. The returned data is intentionally small and presentation-oriented
     * so the model can summarize results without seeing internal audit fields.</p>
     *
     * @param keyword search keyword extracted from the user message
     * @return structured search results for model summarization
     */
    @Tool(name = "search_posts", description = "Search Wisdom Hub public posts by keyword. Use this when the user asks to find related articles or posts.")
    public PostSearchResult searchPosts(
            @ToolParam(description = "Keyword to search in post title or content, for example Redis, Spring AI, MySQL.")
            String keyword) {

        String normalizedKeyword = normalizeKeyword(keyword);
        PageResult<PostVO> pageResult = postService.search(normalizedKeyword, DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE);

        List<PostSearchItem> items = new ArrayList<>();
        if (pageResult.getList() != null) {
            for (PostVO post : pageResult.getList()) {
                items.add(new PostSearchItem(
                        post.getId(),
                        post.getTitle(),
                        post.getAuthorName(),
                        post.getCreateTime(),
                        buildExcerpt(post.getContent()),
                        post.getLikeCount(),
                        post.getFavoriteCount()
                ));
            }
        }

        return new PostSearchResult(
                normalizedKeyword,
                pageResult.getTotal(),
                pageResult.getPageNum(),
                pageResult.getPageSize(),
                items
        );
    }

    /**
     * Normalizes model-provided search text before handing it to PostService.
     */
    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return " ";
        }
        String normalized = keyword.trim();
        return normalized.length() > 50 ? normalized.substring(0, 50) : normalized;
    }

    /**
     * Builds a short content excerpt suitable for LLM summarization.
     */
    private String buildExcerpt(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }

        String plainText = content
                .replaceAll("!\\[[^]]*]\\([^)]*\\)", " ")
                .replaceAll("\\[[^]]*]\\([^)]*\\)", " ")
                .replaceAll("[#>*_`\\-]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (plainText.length() <= MAX_EXCERPT_LENGTH) {
            return plainText;
        }
        return plainText.substring(0, MAX_EXCERPT_LENGTH) + "...";
    }

    /**
     * Structured result returned to the model after a post search.
     */
    public static class PostSearchResult {

        private final String keyword;
        private final Integer total;
        private final Integer pageNum;
        private final Integer pageSize;
        private final List<PostSearchItem> posts;

        public PostSearchResult(String keyword, Integer total, Integer pageNum, Integer pageSize,
                                List<PostSearchItem> posts) {
            this.keyword = keyword;
            this.total = total;
            this.pageNum = pageNum;
            this.pageSize = pageSize;
            this.posts = posts;
        }

        public String getKeyword() {
            return keyword;
        }

        public Integer getTotal() {
            return total;
        }

        public Integer getPageNum() {
            return pageNum;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public List<PostSearchItem> getPosts() {
            return posts;
        }
    }

    /**
     * Presentation-safe post search item exposed to the model.
     */
    public static class PostSearchItem {

        private final Long id;
        private final String title;
        private final String author;
        private final LocalDateTime createTime;
        private final String excerpt;
        private final Integer likeCount;
        private final Integer favoriteCount;

        public PostSearchItem(Long id, String title, String author, LocalDateTime createTime,
                              String excerpt, Integer likeCount, Integer favoriteCount) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.createTime = createTime;
            this.excerpt = excerpt;
            this.likeCount = likeCount;
            this.favoriteCount = favoriteCount;
        }

        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public String getExcerpt() {
            return excerpt;
        }

        public Integer getLikeCount() {
            return likeCount;
        }

        public Integer getFavoriteCount() {
            return favoriteCount;
        }
    }
}
