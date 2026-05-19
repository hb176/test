package com.gmp.business.service.handler.tms;

import com.gmp.business.dto.CreateBusinessRequest;
import com.gmp.business.entity.BusinessRecord;
import com.gmp.business.service.handler.BusinessTypeHandler;
import com.gmp.common.constant.BusinessType;
import com.gmp.common.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class TmsRecordHandler implements BusinessTypeHandler {

    @Override
    public BusinessType getSupportedType() { return BusinessType.TMS_RECORD; }

    @Override
    public void validate(CreateBusinessRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BusinessException(400, "培训记录标题不能为空");
        }
    }

    @Override
    public String generateBusinessNo(BusinessRecord record) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = ThreadLocalRandom.current().nextInt(100, 999);
        return BusinessType.TMS_RECORD.getNoPrefix() + "-" + date + "-" + seq;
    }
}
