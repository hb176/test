package com.gmp.system.controller;

import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.PageResult;
import com.gmp.framework.base.Result;
import com.gmp.system.entity.SysMessage;
import com.gmp.system.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/message")
public class SysMessageController extends CommonController<SysMessageService, SysMessage> {

    private final SysMessageService messageService;

    @Override
    protected SysMessageService getService() {
        return messageService;
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@RequestHeader("X-User-Id") Long userId) {
        return Result.ok(messageService.getUnreadCount(userId));
    }

    @GetMapping("/list")
    public Result<PageResult<SysMessage>> getUserMessages(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(messageService.getUserMessages(userId, pageNum, pageSize));
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestHeader("X-User-Id") Long userId) {
        messageService.markAllAsRead(userId);
        return Result.okMsg("全部已读");
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.okMsg("已标记为已读");
    }

    @PreAuthorize("hasAuthority('system:message:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        messageService.removeById(id);
        return Result.okMsg("删除成功");
    }

    /**
     * 使用模板发送站内信（供内部 Feign 调用）
     */
    @PostMapping("/send-by-template")
    public Result<Void> sendByTemplate(@RequestBody SendByTemplateRequest request) {
        messageService.sendByTemplate(
                request.getTemplateCode(),
                request.getReceiverId(),
                request.getReceiverName(),
                request.getVariables(),
                request.getBusinessType(),
                request.getBusinessId());
        return Result.okMsg("发送成功");
    }

    @lombok.Data
    public static class SendByTemplateRequest {
        private String templateCode;
        private Long receiverId;
        private String receiverName;
        private java.util.Map<String, String> variables;
        private String businessType;
        private Long businessId;
    }
}
