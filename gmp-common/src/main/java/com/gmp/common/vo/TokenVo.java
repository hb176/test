package com.gmp.common.vo;


import lombok.Data;

@Data
public class TokenVo extends Token {


    private String dbUrl;
    private String compName;

    public TokenVo(){super(); }

    public TokenVo(Long id, String userId, String username, String companyCode, String dbUrl) {
        this.setId(id);
        this.setUserId(userId);
        this.setUserName(username);
        this.setCompCode(companyCode);
        this.dbUrl = dbUrl;
    }
    public TokenVo(Long id, String userId, String username, String companyCode, String dbUrl,String compName,Long compId) {
        this.setId(id);
        this.setUserId(userId);
        this.setUserName(username);
        this.setCompCode(companyCode);
        this.dbUrl = dbUrl;
        this.compName = compName;
        this.setCompId(compId);
    }
}

