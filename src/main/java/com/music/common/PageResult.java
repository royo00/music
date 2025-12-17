package com.music.common;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果类
 * 用于封装分页查询结果
 *
 * @param <T> 数据类型
 */
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer size;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 无参构造函数
     */
    public PageResult() {
    }

    /**
     * 构造函数 - 基础分页信息
     *
     * @param total 总记录数
     * @param list  数据列表
     */
    public PageResult(Long total, List<T> list) {
        this.total = total;
        this.list = list;
    }

    /**
     * 构造函数 - 完整分页信息
     *
     * @param total 总记录数
     * @param page  当前页码
     * @param size  每页数量
     * @param list  数据列表
     */
    public PageResult(Long total, Integer page, Integer size, List<T> list) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.list = list;
        this.pages = calculatePages(total, size);
        this.hasPrevious = page > 1;
        this.hasNext = page < this.pages;
    }

    /**
     * 静态工厂方法 - 创建分页结果
     *
     * @param total 总记录数
     * @param page  当前页码
     * @param size  每页数量
     * @param list  数据列表
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(Long total, Integer page, Integer size, List<T> list) {
        return new PageResult<>(total, page, size, list);
    }

    /**
     * 静态工厂方法 - 创建空分页结果
     *
     * @param page 当前页码
     * @param size 每页数量
     * @return 空分页结果对象
     */
    public static <T> PageResult<T> empty(Integer page, Integer size) {
        return new PageResult<>(0L, page, size, List.of());
    }

    /**
     * 计算总页数
     *
     * @param total 总记录数
     * @param size  每页数量
     * @return 总页数
     */
    private Integer calculatePages(Long total, Integer size) {
        if (total == 0 || size == 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / size);
    }

    /**
     * 获取起始记录索引（从0开始）
     */
    public Integer getStartIndex() {
        if (page == null || size == null) {
            return 0;
        }
        return (page - 1) * size;
    }

    /**
     * 获取结束记录索引
     */
    public Integer getEndIndex() {
        if (page == null || size == null || total == null) {
            return 0;
        }
        int end = page * size;
        return Math.min(end, total.intValue());
    }

    // Getters and Setters
    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
        if (this.size != null) {
            this.pages = calculatePages(total, this.size);
        }
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
        updateNavigationFlags();
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
        if (this.total != null) {
            this.pages = calculatePages(this.total, size);
        }
        updateNavigationFlags();
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

    public Boolean getHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    /**
     * 更新导航标志
     */
    private void updateNavigationFlags() {
        if (this.page != null && this.pages != null) {
            this.hasPrevious = this.page > 1;
            this.hasNext = this.page < this.pages;
        }
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "total=" + total +
                ", page=" + page +
                ", size=" + size +
                ", pages=" + pages +
                ", listSize=" + (list != null ? list.size() : 0) +
                ", hasPrevious=" + hasPrevious +
                ", hasNext=" + hasNext +
                '}';
    }
}
