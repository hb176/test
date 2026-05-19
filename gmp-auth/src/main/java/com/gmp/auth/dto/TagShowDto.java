package com.gmp.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hb176
 * @time 2022-04-24 10:29
 * @email
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TagShowDto implements Serializable {

    private static final long serialVersionUID =9071441856409706419L;

    //tag
    private String tag;

    //pn
    private String pn1;
    private String pn2;

    private String pv1;
    private String pv2;

    private String api;
    private String apiCode;
    private String apiName;
    private String showName;
    private String subCode;



}
