package com.wisdomhub.dto;

import java.util.List;

/**
 * 分页响应封装
 */
public class PageResult<T> {
    
    /**
     * 当前页码
     */
    private Integer pageNum;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 总记录数
     */
    private Integer total;
    
    /**
     * 总页数
     */
    private Integer pages;
    
    /**
     * 当前页数据
     */
    private List<T> list;
    
    public PageResult() {
    }
    
    public PageResult(Integer pageNum, Integer pageSize, Integer total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = (total + pageSize - 1) / pageSize; // 计算总页数
        this.list = list;
    }
    
    public Integer getPageNum() {
        return pageNum;
    }
    
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    
    public Integer getTotal() {
        return total;
    }
    
    public void setTotal(Integer total) {
        this.total = total;
    }
    
    public Integer getPages() {
        return pages;
    }
    
    public void setPages(Integer pages) {
        this.pages = pages;
    }
    
    public List<T> getList() {
        return list;
    }
    
    public void setList(List<T> list) {
        this.list = list;
    }
    
    @Override
    public String toString() {
        return "PageResult{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", total=" + total +
                ", pages=" + pages +
                ", list=" + list +
                '}';
    }
}