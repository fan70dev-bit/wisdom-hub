package com.wisdomhub.service;

import com.wisdomhub.dto.CreatePostRequest;
import com.wisdomhub.dto.PageResult;
import com.wisdomhub.dto.PostVO;
import com.wisdomhub.dto.UpdatePostRequest;
import com.wisdomhub.entity.Post;

import java.util.List;

/**
 * 帖子服务接口
 */
public interface PostService {
    
    /**
     * 创建帖子
     * 
     * @param request 创建帖子请求
     * @return 帖子 ID
     */
    Long create(CreatePostRequest request);
    
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
     * 删除帖子（逻辑删除）
     * 
     * @param id 帖子 ID
     * @return 是否删除成功
     */
    boolean delete(Long id);
    
    /**
     * 我的花园列表
     * 
     * @return 帖子列表
     */
    List<Post> getGarden();
    
    /**
     * 广场列表
     * 
     * @return 帖子列表
     */
    List<Post> getPlaza();

    /**
     * 探索广场（分页查询公开帖子）
     *
     * @param pageNum 页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<PostVO> explore(Integer pageNum, Integer pageSize);

    /**
     * 关键字搜索（分页查询）
     *
     * @param keyword 搜索关键字
     * @param pageNum 页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<PostVO> search(String keyword, Integer pageNum, Integer pageSize);
}