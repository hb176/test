package com.gmp.framework.config;

import com.gmp.common.base.ResultCode;
import com.gmp.common.exceptions.BusinessException;
import com.gmp.framework.base.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 [{}] {} - 请求路径: {}", e.getCode(), e.getMessage(), request.getRequestURI());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 - {} - 请求路径: {}", errorMsg, request.getRequestURI());
        return Result.fail(ResultCode.VALIDATION_FAILED, errorMsg);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        String errorMsg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败 - {} - 请求路径: {}", errorMsg, request.getRequestURI());
        return Result.fail(ResultCode.VALIDATION_FAILED, errorMsg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMissingParamException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少必需参数: {} - 请求路径: {}", e.getParameterName(), request.getRequestURI());
        return Result.fail(ResultCode.BAD_REQUEST, "缺少必需参数: " + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型不匹配: {} 期望类型: {} - 请求路径: {}",
                e.getName(), e.getRequiredType(), request.getRequestURI());
        return Result.fail(ResultCode.BAD_REQUEST, "参数类型错误: " + e.getName());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e,
                                                        HttpServletRequest request) {
        log.warn("不支持的请求方法: {} - 请求路径: {}", e.getMethod(), request.getRequestURI());
        return Result.fail(ResultCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMessageNotReadableException(HttpMessageNotReadableException e,
                                                        HttpServletRequest request) {
        log.warn("请求体解析失败 - 请求路径: {} - 原因: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, "请求数据格式错误，请检查JSON格式");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        log.warn("数据重复 - 请求路径: {} - 详情: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(ResultCode.DUPLICATE_KEY, "数据已存在，请勿重复提交");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityException(DataIntegrityViolationException e, HttpServletRequest request) {
        log.error("数据完整性约束冲突 - 请求路径: {} - 详情: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(ResultCode.DATABASE_ERROR, "数据操作违反完整性约束");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleUnknownException(Exception e, HttpServletRequest request) {
        log.error("未知异常 - 请求路径: {} - 类型: {} - 消息: {}",
                request.getRequestURI(), e.getClass().getName(), e.getMessage(), e);
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }
}
