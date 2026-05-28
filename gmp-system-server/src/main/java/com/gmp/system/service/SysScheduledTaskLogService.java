package com.gmp.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gmp.system.entity.SysScheduledTaskLog;
import com.gmp.system.mapper.SysScheduledTaskLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SysScheduledTaskLogService extends ServiceImpl<SysScheduledTaskLogMapper, SysScheduledTaskLog> {
}
