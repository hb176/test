package com.gmp.framework.base;

import com.gmp.common.base.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通用 Controller 基类 — 泛型 CRUD REST 控制器
 *
 * 子类只需:
 * 1. extends CommonController<XxxService, XxxEntity>
 * 2. 实现 getService()
 * 即可自动获得完整 CRUD 端点（@RestController 已预置）
 *
 * @param <S> Service 类型
 * @param <T> 实体类型
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@SuppressWarnings({"unchecked", "boxing"})
@RestController
public abstract class CommonController<S extends CommonService<?, T>, T extends CommonEntity> {

    protected abstract S getService();

    // ---- 便捷响应方法 ----

    protected <D> Result<D> success() {
        return Result.ok();
    }

    protected <D> Result<D> success(D data) {
        return Result.ok(data);
    }

    protected <D> Result<D> success(String message, D data) {
        return Result.ok(message, data);
    }

    protected <D> Result<D> fail(ResultCode resultCode) {
        return Result.fail(resultCode);
    }

    protected <D> Result<D> fail(ResultCode resultCode, String message) {
        return Result.fail(resultCode, message);
    }

    protected <D> Result<D> fail(int code, String message) {
        return Result.fail(code, message);
    }

    // ---- 通用 CRUD 模板方法 ----

    protected Result<T> getById(Long id) {
        T entity = getService().getById(id);
        if (entity == null) {
            return fail(ResultCode.NOT_FOUND);
        }
        return success(entity);
    }

    protected Result<T> saveEntity(T entity) {
        boolean saved = getService().save(entity);
        return saved ? success(entity) : fail(ResultCode.INTERNAL_ERROR, "保存失败");
    }

    protected Result<T> updateEntity(T entity) {
        T existing = getService().getById(entity.getId());
        if (existing == null) return fail(ResultCode.NOT_FOUND);
        // 版本号自增
        try {
            var vf = existing.getClass().getMethod("getVersion");
            var vs = existing.getClass().getMethod("setVersion", Integer.class);
            Integer curVer = (Integer) vf.invoke(existing);
            vs.invoke(entity, curVer == null ? 1 : curVer + 1);
        } catch (Exception ignored) {}
        boolean updated = getService().updateById(entity);
        return updated ? success(entity) : fail(ResultCode.INTERNAL_ERROR, "更新失败");
    }

    protected Result<Void> deleteById(Long id) {
        boolean deleted = getService().softDeleteById(id);
        return deleted ? success() : fail(ResultCode.INTERNAL_ERROR, "删除失败");
    }

    protected Result<Void> deleteByIds(List<Long> ids) {
        boolean deleted = getService().softDeleteByIds(ids);
        return deleted ? success() : fail(ResultCode.INTERNAL_ERROR, "批量删除失败");
    }
}
