package com.gmp.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gmp.workflow.model.flowable.ModelInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ModelInfoMapper extends BaseMapper<ModelInfo> {
    IPage<ModelInfo> getPagerModel(IPage<ModelInfo> page, @Param("modelInfo") ModelInfo modelInfo);
}
