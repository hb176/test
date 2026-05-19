package com.gmp.workflow.tools.utils;

public class DurationUtils {
    public static String getDuration(Long duration) {
        if (duration != null) {
            long nd = 1000 * 24 * 60 * 60;
            long nh = 1000 * 60 * 60;
            long hm = 1000 * 60;
            long sm = 1000;
            long day = duration / nd;
            long hour = duration % nd / nh;
            long minute = duration % nh / hm;
            long second = duration % hm / sm;
            StringBuilder durationStr = new StringBuilder();
            if (day > 0) durationStr.append(day).append("天 ");
            if (hour > 0) durationStr.append(hour).append("时 ");
            if (minute > 0) durationStr.append(minute).append("分 ");
            if (second > 0) durationStr.append(second).append("秒");
            return durationStr.toString();
        }
        return "";
    }
}
