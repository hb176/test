package com.gmp.workflow.feign;

import com.gmp.framework.base.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 业务服务Feign客户端 - 流程结束时回调更新业务记录状态
 */
@FeignClient(name = "gmp-business-server", path = "/dms")
public interface BusinessFeignClient {

    @PutMapping("/document/by-no/{businessNo}/status")
    Result<Void> updateStatusByBusinessNo(@PathVariable("businessNo") String businessNo,
                                           @RequestParam("status") String status);
}
