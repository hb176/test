package com.gmp.workflow.tools.vo;

import com.gmp.workflow.tools.common.ReturnCode;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import java.io.Serializable;

@Data
public class ReturnVo<T> implements Serializable {
    private static final long serialVersionUID = -5580228202640516960L;
    private String code;
    private String msg;
    private T data;

    public boolean isSuccess() {
        return StringUtils.isNotBlank(code) && ReturnCode.SUCCESS.equals(code);
    }

    public ReturnVo() {}

    public ReturnVo(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ReturnVo(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
