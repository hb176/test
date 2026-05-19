package com.gmp.workflow.service.flowable.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gmp.workflow.enums.flowable.runtime.CommentTypeEnum;
import com.gmp.workflow.mapper.CommentInfoMapper;
import com.gmp.workflow.model.flowable.CommentInfo;
import com.gmp.workflow.service.flowable.ICommentInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentInfoServiceImpl extends ServiceImpl<CommentInfoMapper, CommentInfo>
        implements ICommentInfoService {

    private final CommentInfoMapper commentInfoMapper;

    @Override
    public void saveComment(CommentInfo commentInfo) {
        commentInfo.setTime(new Date());
        this.saveOrUpdate(commentInfo);
    }

    @Override
    public List<CommentInfo> getCommentInfosByProcessInstanceId(String processInstanceId) {
        List<CommentInfo> commentInfos = commentInfoMapper.getCommentInfosByProcessInstanceId(processInstanceId);
        if (commentInfos != null) {
            commentInfos.forEach(c -> c.setTypeName(CommentTypeEnum.getEnumMsgByType(c.getType())));
        }
        return commentInfos;
    }
}
