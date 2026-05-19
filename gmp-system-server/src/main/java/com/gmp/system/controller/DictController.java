package com.gmp.system.controller;

import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.PageResult;
import com.gmp.framework.base.Result;
import com.gmp.system.entity.SysDict;
import com.gmp.system.service.SysDictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dict")
public class DictController extends CommonController<SysDictService, SysDict> {

    private final SysDictService sysDictService;

    @Override
    protected SysDictService getService() {
        return sysDictService;
    }

    @GetMapping("/page")
    public Result<PageResult<SysDict>> page(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        return success(sysDictService.pageQuery(pageNum, pageSize, null));
    }

    @GetMapping("/{dictCode}")
    public Result<List<SysDict>> getByCode(@PathVariable String dictCode) {
        return success(sysDictService.lambdaQuery()
                .eq(SysDict::getDictCode, dictCode)
                .orderByAsc(SysDict::getSortOrder)
                .list());
    }

    @PostMapping
    public Result<SysDict> save(@RequestBody SysDict dict) {
        return saveEntity(dict);
    }

    @PutMapping
    public Result<SysDict> update(@RequestBody SysDict dict) {
        return updateEntity(dict);
    }
}
