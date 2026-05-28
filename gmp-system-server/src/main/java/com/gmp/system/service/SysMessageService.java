package com.gmp.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.framework.base.CommonService;
import com.gmp.system.entity.SysMessage;
import com.gmp.system.entity.SysMessageTemplate;
import com.gmp.system.mapper.SysMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.framework.base.PageResult;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysMessageService extends CommonService<SysMessageMapper, SysMessage> {

    private final SysMessageTemplateService messageTemplateService;

    private static final Pattern TEMPLATE_VAR_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

    /**
     * 发送站内信
     */
    public void send(Long receiverId, String receiverName, String title, String content,
                     String msgType, String businessType, Long businessId) {
        SysMessage msg = new SysMessage();
        msg.setReceiverId(receiverId);
        msg.setReceiverName(receiverName);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setMsgType(msgType != null ? msgType : "NOTIFICATION");
        msg.setReadFlag(0);
        msg.setBusinessType(businessType);
        msg.setBusinessId(businessId);
        msg.setSenderId(0L);
        msg.setSenderName("系统");
        save(msg);
        log.info("站内信已发送: receiver={}, title={}", receiverId, title);
    }

    /**
     * 使用模板发送站内信
     * @param templateCode 模板编码
     * @param receiverId 接收人ID
     * @param receiverName 接收人名称
     * @param variables 模板变量
     * @param businessType 关联业务类型
     * @param businessId 关联业务ID
     */
    public void sendByTemplate(String templateCode, Long receiverId, String receiverName,
                               Map<String, String> variables, String businessType, Long businessId) {
        SysMessageTemplate template = messageTemplateService.getByCode(templateCode);
        if (template == null) {
            log.warn("消息模板不存在: code={}", templateCode);
            return;
        }
        if (template.getEnabled() != 1) {
            log.warn("消息模板已禁用: code={}", templateCode);
            return;
        }

        String title = renderTemplate(template.getTitleTemplate(), variables);
        String content = renderTemplate(template.getContentTemplate(), variables);

        send(receiverId, receiverName, title, content, template.getMsgType(), businessType, businessId);
    }

    /**
     * 渲染模板，替换 {{变量}} 占位符
     */
    private String renderTemplate(String template, Map<String, String> variables) {
        if (template == null || variables == null) return template;
        Matcher matcher = TEMPLATE_VAR_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1);
            String value = variables.getOrDefault(varName, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 标记已读
     */
    public void markAsRead(Long msgId) {
        SysMessage msg = getById(msgId);
        if (msg != null && msg.getReadFlag() == 0) {
            msg.setReadFlag(1);
            msg.setReadTime(LocalDateTime.now());
            updateById(msg);
        }
    }

    /**
     * 查询用户未读消息数
     */
    public long getUnreadCount(Long userId) {
        return lambdaQuery()
                .eq(SysMessage::getReceiverId, userId)
                .eq(SysMessage::getReadFlag, 0)
                .count();
    }

    /**
     * 查询用户消息列表（按创建时间倒序）
     */
    public PageResult<SysMessage> getUserMessages(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getReceiverId, userId)
                .orderByDesc(SysMessage::getCreateTime);
        return pageQuery(pageNum, pageSize, wrapper);
    }

    /**
     * 标记用户所有消息为已读
     */
    public void markAllAsRead(Long userId) {
        lambdaUpdate()
                .eq(SysMessage::getReceiverId, userId)
                .eq(SysMessage::getReadFlag, 0)
                .set(SysMessage::getReadFlag, 1)
                .set(SysMessage::getReadTime, LocalDateTime.now())
                .update();
    }
}
