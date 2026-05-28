package com.gmp.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.framework.base.CommonService;
import com.gmp.file.entity.FileInfo;
import com.gmp.file.mapper.FileInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FileInfoService extends CommonService<FileInfoMapper, FileInfo> {

    public List<FileInfo> listByRecordId(Long recordId) {
        return lambdaQuery()
                .eq(FileInfo::getRecordId, recordId)
                .eq(FileInfo::getDeleted, 0)
                .orderByDesc(FileInfo::getCreateTime)
                .list();
    }

    public List<FileInfo> listByRecordIdAndType(Long recordId, String recordType) {
        return lambdaQuery()
                .eq(FileInfo::getRecordId, recordId)
                .eq(FileInfo::getRecordType, recordType)
                .eq(FileInfo::getDeleted, 0)
                .orderByDesc(FileInfo::getCreateTime)
                .list();
    }
}
