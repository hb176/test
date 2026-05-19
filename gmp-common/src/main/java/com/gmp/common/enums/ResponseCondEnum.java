package com.gmp.common.enums;


import lombok.Getter;


@Getter
public enum ResponseCondEnum {
    OK(200,"OK"),
    OPERATE_SUCCESSFUL(201,"操作成功"),
    WARN_PWD_ERROR_NUM(5022,"密码错误次数"),
    WARN_DEFPWD_EDIT(5020,"密码为初始密码，请立即修改"),
    WARN_PWD_EXPIRATION_TIME(5021,"密码已过期，请立即修改")
    ;


    private int status;
    private String message;

    ResponseCondEnum(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
