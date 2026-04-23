package com.wisdomhub.service;

import com.wisdomhub.dto.UpdatePostRequest;
import com.wisdomhub.entity.Post;

import java.util.List;

/**
 * 文章/碎碎念服务接口
 */
public interface PostService {

    /**
     * 创建文章/碎碎念（自动识别当前用户）
     */
    Long create(Post post);

    /**
     * 我的花园列表
     */
    List<Post> getGarden();

    /**
     * 广场列表
     */
    List<Post> getPlaza();

    /**
     * 根据 ID 查询帖子详情
     *
     * @param id 帖子 ID
     * @return 帖子详情
     */
    Post getById(Long id);

    /**
     * 修改帖子
     *
     * @param request 修改请求
     * @return 是否修改成功
     */
    boolean update(UpdatePostRequest request);

    /**
     * 删除帖子（同步清理 OSS 资源）
     *
     * @param id 帖子 ID
     * @return 是否删除成功
     */
    boolean delete(Long id);
}