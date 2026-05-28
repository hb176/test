package com.gmp.workflow.controller;

import com.gmp.workflow.model.flowable.CommentInfo;
import com.gmp.workflow.service.flowable.ICommentInfoService;
import com.gmp.workflow.tools.common.ReturnCode;
import com.gmp.workflow.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程评论控制器
 *
 * 接口路径: /flow/flowable/commentInfo/*
 * 参考: flow-admin-rest CommentInfoResource
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/flow/flowable/commentInfo")
public class CommentInfoController {

    private final ICommentInfoService commentInfoService;

    /**
     * 根据流程实例 ID 获取评论列表
     */
    @GetMapping(value = "/getCommentInfosByProcessInstanceId/{processInstanceId}", produces = "application/json")
    public ReturnVo<List> getCommentInfosByProcessInstanceId(@PathVariable String processInstanceId) {
        ReturnVo<List> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        List<CommentInfo> commentInfos = commentInfoService.getCommentInfosByProcessInstanceId(processInstanceId);
        returnVo.setData(commentInfos);
        return returnVo;
    }
}
