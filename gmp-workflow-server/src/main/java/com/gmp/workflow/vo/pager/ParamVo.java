package com.gmp.workflow.vo.pager;

import com.gmp.workflow.tools.pager.Query;
import lombok.Data;

import java.io.Serializable;

@Data
public class ParamVo<T> implements Serializable {
    private T entity;
    private Query query;
}
