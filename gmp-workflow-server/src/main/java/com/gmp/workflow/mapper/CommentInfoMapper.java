package com.gmp.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gmp.workflow.model.flowable.CommentInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentInfoMapper extends BaseMapper<CommentInfo> {
    List<CommentInfo> getCommentInfosByProcessInstanceId(String processInstanceId);
}
