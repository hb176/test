package com.gmp.framework.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

/**
 * 通用 Service 基类 — 继承 MyBatis-Plus ServiceImpl，封装分页/批量/软删
 *
 * @param <M> Mapper 类型
 * @param <T> 实体类型
 * @author hb176
 * @since 1.0.0
 */
public abstract class CommonService<M extends BaseMapper<T>, T extends CommonEntity>
        extends ServiceImpl<M, T> {

    public PageResult<T> pageQuery(long pageNum, long pageSize, LambdaQueryWrapper<T> wrapper) {
        IPage<T> page = new Page<>(pageNum, pageSize);
        IPage<T> result = this.page(page, wrapper);
        return PageResult.of(result);
    }

    @Override
    public T getById(Serializable id) {
        return super.getById(id);
    }

    protected List<T> listByWrapper(LambdaQueryWrapper<T> wrapper) {
        return this.list(wrapper);
    }

    protected T getOneByWrapper(LambdaQueryWrapper<T> wrapper) {
        return this.getOne(wrapper);
    }

    protected long countByWrapper(LambdaQueryWrapper<T> wrapper) {
        return this.count(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchWithTransaction(List<T> entityList) {
        return this.saveBatch(entityList);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateBatchWithTransaction(List<T> entityList) {
        return this.updateBatchById(entityList);
    }

    public boolean softDeleteById(Long id) {
        return this.removeById(id);
    }

    public boolean softDeleteByIds(List<Long> ids) {
        return this.removeBatchByIds(ids);
    }
}
