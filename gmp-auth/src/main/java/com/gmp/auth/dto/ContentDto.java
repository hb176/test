package com.gmp.auth.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentDto {

    /**
     * 操作内容		contents: [
     *          {
     *              acoItem:  section名称(eg.Section 02：批记录打印分发),
     *              proNode:  控件名称(eg.文件简介*),
     *              oldValue: 旧值(eg.批记录文件),
     *              newsValue: 新值(eg.批记录文件1111)
     *          },
     *          {
     *              acoItem:  section名称(eg.Section 02：批记录打印分发),
     *              proNode:  控件名称(eg.文件简介*),
     *              oldValue: 旧值(eg.批记录文件),
     *              newsValue: 新值(eg.批记录文件1111)
     *          }
     *      ]
     */

    //栏目/section name
    private String acoItem;

    //控件名称/key-title
    private String proNode;

    //旧值 /value
    private String oldValue;

    //新值  /value
    private String newsValue;

    //新值  /value
    private String actionTime;

}
