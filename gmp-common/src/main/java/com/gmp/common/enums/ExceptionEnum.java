package com.gmp.common.enums;


import lombok.Getter;

@Getter
public enum ExceptionEnum {
    ACCESS_TOKEN_CANNOT_BE_NULL(401, "用户未授权"),
    CHECK_CODE_ERROR(500, "电子签名有误"),
    DATA_TYPE_ERROR(400, "数据类型错误"),
    INVALID_USERNAME_PASSWORD(400, "无效的用户名或密码"),
    DEL_USERNAME(400, "账号已锁定"),
    LOCKED_PASSWORD(400, "密码已过期"),
    UNAUTHORIZED(401, "没有访问权限"),
    FILE_UPLOAD_ERROR(500, "文件上传异常"),
    INVALID_FILE_TYPE(400, "无效的文件类型"),
    LOGIN_UNAUTHORIZED(401,"登录状态过期"),
    PARAMS_IS_ERROR(400,"传递的参数有误"),
    MENU_EDIT_ERROR(400,"栏目信息有误")
    ;


    private int code;
    private String message;


    ExceptionEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}