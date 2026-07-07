package com.morningstar.old.infra.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页工具类
 */
@Data
@Schema(description = "分页结果对象")
public class PageResult<T> implements Serializable {
    /**
     * 总记录数
     */
    @Schema(description = "总记录数")
    private Integer totalRecordNum;

    /**
     * 总页数
     */
    @Schema(description = "总页数")
    private Integer totalPageNum;

    /**
     * 页记录数
     */
    @Schema(description = "页记录数")
    private Integer pageSize;

    /**
     * 当前页记录数
     */
    @Schema(description = "当前页记录数")
    private Integer currentPageSize;

    /**
     * 当前第几页(1+)
     */
    @Schema(description = "当前第几页(1+)")
    private Integer pageNum;

    /**
     * 记录集合
     */
    @Schema(description = "记录集合")
    private List<T> records;

    public PageResult(List<T> records, int pageNum, int pageSize, long totalRecordNum) {
        this.totalRecordNum = (int) totalRecordNum;
        this.totalPageNum = (int) ((totalRecordNum % pageSize == 0) ? totalRecordNum / pageSize : totalRecordNum / pageSize + 1);
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.currentPageSize = records.size();
        this.records = records;
    }
}
