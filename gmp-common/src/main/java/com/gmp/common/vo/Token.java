package com.gmp.common.vo;

import com.gmp.common.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hb176
 * @time 2020-12-25 20:20
 * @email xinhui.chen@abioplus.cn
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    private Long id;
    private String userId;
    private String userName;

    private Long deptId;
    private String deptCode;
    private Long subId;
    private String subCode;
    private Long compId;
    private String compCode;

    private String evn;
    private String signMethod;
    private String domainId;
    private String dateFormat;
    public String getDateTimeFormat(){
        if(StringUtils.isEmpty(this.dateFormat)){
            this.dateFormat = "yyyy-MM-dd";
        }
        return this.dateFormat;
    }
}
