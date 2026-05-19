package com.gmp.business.event;

/**
 * 业务事件监听器标记接口
 *
 * 具体监听器使用 Spring @EventListener + condition 过滤事件类型。
 * 实现此接口以明确表示该类是业务事件消费者。
 *
 * @author hb176
 * @since 1.0.0
 */
public interface BusinessEventListener {
}
