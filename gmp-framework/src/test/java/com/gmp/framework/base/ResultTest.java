package com.gmp.framework.base;

import com.gmp.common.base.ResultCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ResultTest {

    @Test
    void okShouldReturnSuccessCode() {
        Result<String> result = Result.ok("data");
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo("data");
        assertThat(result.getMessage()).isEqualTo("操作成功");
        assertThat(result.getTimestamp()).isPositive();
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void okWithNoDataShouldReturnSuccess() {
        Result<Void> result = Result.ok();
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
    }

    @Test
    void okMsgShouldReturnSuccessWithMessage() {
        Result<Void> result = Result.okMsg("自定义消息");
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("自定义消息");
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void failWithResultCodeShouldReturnError() {
        Result<Void> result = Result.fail(ResultCode.BAD_REQUEST);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("请求参数错误");
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    void failWithResultCodeAndCustomMessage() {
        Result<Void> result = Result.fail(ResultCode.NOT_FOUND, "用户不存在");
        assertThat(result.getCode()).isEqualTo(404);
        assertThat(result.getMessage()).isEqualTo("用户不存在");
    }

    @Test
    void failWithRawCodeAndMessage() {
        Result<Void> result = Result.fail(500, "服务器错误");
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).isEqualTo("服务器错误");
    }

    @Test
    void okWithMessageAndData() {
        Result<String> result = Result.ok("创建成功", "entity-id");
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("创建成功");
        assertThat(result.getData()).isEqualTo("entity-id");
    }
}
