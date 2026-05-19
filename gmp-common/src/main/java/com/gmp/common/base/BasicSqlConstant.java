package com.gmp.common.base;


public class BasicSqlConstant extends BasicConstant {
    //待处理任务列表sql
    public static final String CUR_GROUP_USER_COUNT = "select count(userId) count,userType from(SELECT a.user_id AS userId, substring_index( substring_index( a.user_type, ',', b.help_topic_id + 1 ), ',',- 1 ) AS userType FROM( SELECT * FROM client_user_info where status='norm'  ) a JOIN mysql.help_topic b ON b.help_topic_id < ( length( a.user_type ) - length( REPLACE ( a.user_type, ',', '' ) ) + 1 ) ) user_count group by userType";
    public static final String CUR_USER_COUNT = "select count(id) as count from client_user_info where status='norm'";
    public static final String CUR_PROCESS_COUNT = "select count(id) as count from custom_process_info where status='CONF02'";
    public static final String CUR_FORM_COUNT = "select count(id)  as count from custom_form_info where status='CONF02'";
    public static final String CUR_DATA_SQL = "select count(id)  as count from custom_data_persistence where status='norm'";

    public static final String CUR_CHECK_UPPER_SQL = "SELECT (SELECT COUNT(*) FROM CLIENT_USER_INFO WHERE STATUS='NORM'), (SELECT COUNT(*) FROM CUSTOM_PROCESS_INFO WHERE STATUS='CONF02'), (SELECT COUNT(*) FROM CUSTOM_FORM_INFO WHERE STATUS='CONF02');";
    public static final String UP_TMS_USER_STATUS_SQL = "update tms_user_info set CLIENT_STATUS='%s' WHERE USER_ID='%s';";


}
