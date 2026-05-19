package com.gmp.workflow.service.flowable;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gmp.workflow.model.flowable.CommentInfo;

import java.util.List;

public interface ICommentInfoService extends IService<CommentInfo> {
    void saveComment(CommentInfo commentInfo);
    List<CommentInfo> getCommentInfosByProcessInstanceId(String processInstanceId);
}
