package com.gmp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gmp.business.entity.BusinessRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 业务记录 Mapper — 标准 CRUD 由 MyBatis-Plus BaseMapper 提供
 *
 * @author hb176
 * @since 1.0.0
 */
@Mapper
public interface BusinessRecordMapper extends BaseMapper<BusinessRecord> {

    /** 根据业务编号查询（已过滤逻辑删除） */
    @Select("SELECT * FROM business_record WHERE business_no = #{businessNo} AND deleted = 0")
    BusinessRecord selectByBusinessNo(@Param("businessNo") String businessNo);
}
