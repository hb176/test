package com.gmp.common.enums;


import java.time.LocalDateTime;

public interface AccessoryInfo {

    Long getId();
    String getCode();
    String getName();
    String getOriginalFileName();
    String getAlias();
    String getBucket();
    String getSystemModule();
    String getStoragePath();
    String getUserId();
    LocalDateTime getUploadTime();
    String getRecordId();
    String getSectionNode();
    String getMd5Hex();
    String getDetail();
    String getPdfPath();
    String getStatus();

    void setId(Long id);
    void setCode(String code);
    void setName(String name);
    void setOriginalFileName(String originalFilename);
    void setAlias(String alias);
    void setBucket(String bucket);
    void setSystemModule(String systemModule);
    void setStoragePath(String storagePath);
    void setUserId(String userId);
    void setUploadTime(LocalDateTime uploadTime);
    void setRecordId(String recordId);
    void setSectionNode(String sectionNode);
    void setMd5Hex(String md5Hex);
    void setDetail(String detail);
    void setPdfPath(String detail);
    void setStatus(String detail);
}