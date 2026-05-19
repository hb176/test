package com.gmp.common.exceptions;

import com.gmp.common.base.ResultCode;

/**
 * 业务异常类 - 用于业务逻辑中抛出可预见的异常
 * 统一由全局异常处理器捕获并转换为标准Result响应
 *
 * 使用示例：
 * throw new BusinessException(ResultCode.DUPLICATE_KEY, "用户名已存在");
 * throw new BusinessException("表单数据校验失败");
 *
 * @author hb176
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
