package com.gmp.workflow.tools.pager;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class Query implements Serializable {
    private static final long serialVersionUID = 817880730448759944L;
    private int pageSize = 20;
    private int pageNum;
    private Map<String, ORDERBY> sqlOrderBy;
    private String sortField;
    private String sortOrder;

    public int getPageNum() {
        if (pageNum <= 0) pageNum = 1;
        return pageNum;
    }
}
