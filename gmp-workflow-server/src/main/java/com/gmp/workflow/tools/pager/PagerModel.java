package com.gmp.workflow.tools.pager;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class PagerModel<T> implements Serializable {
    private static final long serialVersionUID = 4804053559968742915L;
    private long total;
    private List<T> rows = new ArrayList<>();

    public PagerModel() {}

    public PagerModel(long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }
}
