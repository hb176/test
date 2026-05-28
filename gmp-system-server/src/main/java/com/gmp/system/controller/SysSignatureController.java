package com.gmp.system.controller;

import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.Result;
import com.gmp.system.entity.SysSignature;
import com.gmp.system.service.SysSignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/system/signature")
public class SysSignatureController extends CommonController<SysSignatureService, SysSignature> {

    private final SysSignatureService signatureService;

    @Override
    protected SysSignatureService getService() {
        return signatureService;
    }

    @PostMapping
    public Result<Long> saveSignature(@RequestBody SysSignature signature) {
        signatureService.save(signature);
        return Result.ok(signature.getId());
    }

    @GetMapping("/task/{taskId}")
    public Result<SysSignature> getByTaskId(@PathVariable String taskId) {
        return Result.ok(signatureService.getByTaskId(taskId));
    }
}
