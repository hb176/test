package com.gmp.common.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author hb176
 * @time 2019-11-05 16:59
 * @email yuanfeng@abioplus.cn
 * @Description: 时间工具类
 */
public class DateUtil {


    public static final String FORMATTER_EMPTY = "0000-00-00 00:00:00.000";
    public static final String FORMATTER_DATE_CN = "EEE MMM d HH:mm:ss 'CST' yyyy";
    public static final String FORMATTER_DATE_TN = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String FORMAT_2YM = "yyMM";
    public static final String FORMAT_2YMD = "yyMMdd";
    public static final String FORMAT_4YMD = "yyyyMMdd";
    public static final String FORMAT_4YMD_OF_LINE = "yyyy-MM-dd";
    public static final String FORMAT_2YMD_HMS_OF_LINE = "yy-MM-dd HH:mm:ss";
    public static final String FORMAT_4YMD_HMS_OF_LINE = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_4YMD_HMS_SSS_OF_LINE = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String FORMAT_4YMD_HM_OF_LINE = "yyyy-MM-dd HH:mm";
    public static final String FORMAT_4YMD_HMS = "yyyyMMddHHmmss";


    /**
     * 获取当前时间的默认时间字符串(yy-MM-dd HH:mm:ss )
     * @return
     */
    public static String curLocalDataTimeStr() {
        return DateTimeFormatter.ofPattern(FORMAT_4YMD_HMS_OF_LINE).format(LocalDateTime.now());
    }

    /**
     * 获取当前时间的默认日期字符串(yyyy-MM-dd)
     * @return
     */
    public static String curLocalDataStr() {
        return DateTimeFormatter.ofPattern(FORMAT_4YMD_OF_LINE).format(LocalDateTime.now());
    }

    /**
     * 获取指定时间的时间字符串(yy-MM-dd HH:mm:ss)
     * @return
     */
    public static String strLocalDataTimeWithDesignated(LocalDateTime localDateTime) {
        return DateTimeFormatter.ofPattern(FORMAT_4YMD_HMS_OF_LINE).format(localDateTime);
    }

    /**
     * 获取指定时间 的日期字符串(yyyy-MM-dd)
     * @return
     */
    public static String strLocalDataWithDesignated(LocalDateTime localDateTime) {
        return DateTimeFormatter.ofPattern(FORMAT_4YMD_OF_LINE).format(localDateTime);
    }

    /**
     * 获取指定日期字符串(yyyy-MM-dd)
     * @return
     */
    public static String strLocalDataWithDesignated(LocalDate localDate,String format) {
        return DateTimeFormatter.ofPattern(format).format(localDate);
    }

    public static void main(String[] args) {
        System.out.println("curLocalDataTimeStr() = " + curLocalDataTimeStr()); //2024-11-18 19:25:07
        System.out.println("curLocalDataStr() = " + curLocalDataStr());   // 2024-11-18
        String str = "2025-01-01 00:00:00";
        LocalDateTime localDateTime = parseDefLocalDateTime(str);
        System.out.println("localDateTime = " + localDateTime);
//        LocalDate localDate = parseDefLocalDate(str);
//        System.out.println("localDate = " + localDate);
        String  s = "2025-01-01 00:00:00";
        LocalDateTime localDateTime1 = parseDefLocalDateTime(s);
        System.out.println("localDateTime1 = " + localDateTime1);
        String s1 = strLocalDataWithDesignated(LocalDate.now(), FORMAT_4YMD_OF_LINE);
        System.out.println("strLocalDataWithDesignated = " + s1);
    }

    /**
     * 将指定的时间格式，转化为LocalDate
     *
     * @param dateStr
     * @return
     */
    public static LocalDate parseDefLocalDate(String dateStr) {
        return  LocalDate.parse(dateStr,DateTimeFormatter.ofPattern(FORMAT_4YMD_OF_LINE));
    }

    /**
     * 将指定的LocalDateTime时间字符串，转化为LocalDateTime
     *
     * @param dateStr
     * @return
     */
    public static LocalDateTime parseDefLocalDateTime(String dateStr) {
        String dateFormat = getFormatterStr(dateStr);
        if (dateFormat != null) {
            return parseLocalDateTime(dateStr, dateFormat);
        }
        return parseLocalDateTime(dateStr, FORMAT_4YMD_HMS_OF_LINE);
    }

    /**
     * 将指定的 LocalDateTime 时间字符串，转化为指定格式的 LocalDateTime
     *
     * @param  dateStr
     * @param  format
     * @return
     */
    public static LocalDateTime parseLocalDateTime(String dateStr, String format) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(dateStr)) {
            if (format.equals(FORMATTER_DATE_CN)) {
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(FORMATTER_DATE_CN));
            }
            if (format.equals(FORMATTER_DATE_TN)) {
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(FORMATTER_DATE_TN));
            }

            if (dateStr.length() > format.length()) {
                dateStr = dateStr.substring(0, format.length());
            }
            if (dateStr.length() < FORMAT_4YMD_HMS_SSS_OF_LINE.length()) {
                dateStr = dateStr + FORMATTER_EMPTY.substring(dateStr.length(), FORMAT_4YMD_HMS_SSS_OF_LINE.length());
            }
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(FORMAT_4YMD_HMS_SSS_OF_LINE));
        } else {
            return null;
        }
    }


    /**
     * 将LocalDate转化为指定格式字符串
     */
    public static String getStringByLocalDate(LocalDate localDate, String format) {
        if (localDate == null) return null;
        return localDate.format(DateTimeFormatter.ofPattern(format));
    }
    /**
     * 将LocalDateTime转化为指定格式字符串
     */
    public static String getStringByLocalDate(String localDateTime, String format) {
        if (StringUtils.isEmpty(localDateTime)) return null;
        return parseLocalDateTime(localDateTime, FORMAT_4YMD_OF_LINE.substring(0, localDateTime.length())).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 将LocalDateTime转化为指定格式字符串
     */
    public static String getStringByLocalDateTime(LocalDateTime localDateTime, String format) {
        if (localDateTime == null) return null;
        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }


    /**
     * 将LocalDateTime转化为指定格式字符串
     */
    public static String getStringByLocalDateTime(String localDateTime, String format) {
        if (StringUtils.isEmpty(localDateTime)) return null;
        return parseLocalDateTime(localDateTime, FORMAT_4YMD_HMS_SSS_OF_LINE.substring(0, localDateTime.length())).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 将日期类型字符串转化为日期,然后按照指定格式字符串返回
     */
    public static String getDefaultStringByLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return getStringByLocalDateTime(localDateTime, FORMAT_4YMD_HMS_OF_LINE);
    }


    /**
     * 将日期类型字符串转化为日期,然后按照指定格式字符串返回
     */
    public static String getFormatterStr(String date) {
        if (date.indexOf("'CST'") > 0) {
            return FORMATTER_DATE_CN;
        }
        if (date.indexOf("'T'") > 0) {
            return FORMATTER_DATE_TN;
        }
        if (date.length() > 19) {
            return FORMAT_4YMD_HMS_SSS_OF_LINE;
        }
        switch (date.length()) {
            case 19:
                return FORMAT_4YMD_HMS_OF_LINE;
            case 16:
                return FORMAT_4YMD_HM_OF_LINE;
            case 10:
                return FORMAT_4YMD_OF_LINE;
            default:
                return null;

        }
    }


    /**
     * 获取当前LocalDateTime并转化为指定格式字符串
     */
    public static String getCurrentLocalDateTime(String format) {
        return getStringByLocalDateTime(LocalDateTime.now(), format);
    }


    /**
     * 比较两个LocalDateTime的时间大小
     *
     * @param localDateTime1：时间1
     * @param localDateTime2：时间2
     * @return 是否过期
     */
    public static boolean getCompareLocalDateTime(LocalDateTime localDateTime1, LocalDateTime localDateTime2) {
        if (localDateTime1.isBefore(localDateTime2)) {
            return true;
        }
        return false;
    }


    /**
     * 比较两个LocalDate的时间大小
     *
     * @param localDate1：时间1
     * @param localDate2：时间2
     * @return 是否过期
     */
    public static boolean getCompareLocalDate(LocalDate localDate1, LocalDate localDate2) {
        if (localDate1.isBefore(localDate2)) {
            return true;
        }
        return false;
    }


    /**
     * 将LocalDateTime转化为Date
     */
    public static Date getDateByLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将LocalDate转化为Date
     */
    public static Date getDateByLocalDateTime(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将Date转化为LocalDateTime
     */
    public static LocalDate getLocalDateByDate(Date date) {
        if (date == null) return null;
        return getLocalDateTimeByDate(date).toLocalDate();
    }

    /**
     * 将Date转化为LocalDateTime
     */
    public static LocalDateTime getLocalDateTimeByDate(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 获取两个LocalDateTime的天数差
     */
    public static Long getCompareDayLocalDateTime(LocalDateTime localDateTime1, LocalDateTime localDateTime2) {
        if (getCompareLocalDateTime(localDateTime1, localDateTime2)) {
            Duration duration = Duration.between(localDateTime1, localDateTime2);
            return duration.toDays();
        } else {
            Duration duration = Duration.between(localDateTime2, localDateTime1);
            return duration.toDays();
        }
    }

    /**
     * 获取两个LocalDateTime的小时差
     */
    public static Long getCompareYearLocalDateTime(LocalDateTime localDateTime1, LocalDateTime localDateTime2) {
        if (getCompareLocalDateTime(localDateTime1, localDateTime2)) {
            Duration duration = Duration.between(localDateTime1, localDateTime2);
            return duration.toHours();
        } else {
            Duration duration = Duration.between(localDateTime2, localDateTime1);
            return duration.toHours();
        }
    }


    /**
     * =====================以下为Date的时间转换==========================
     */


    /**
     * 返回服务器当前时间
     *
     * @return 服务器当前时间
     */
    public static Date now() {
        return new Date();
    }

    /**
     * date格式的时间 转成 字符串
     *
     * @param date:时间
     * @return 字符串的时间
     */
    public static String dateToString(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_4YMD_HMS_OF_LINE);
        return sdf.format(date);
    }

    public static String dateToString(Date date, String format) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }


    /**
     * 验证Date时间是否大于当前时间
     *
     * @param endTime：时间
     * @return 是否过期
     */
    public static boolean isEndTime(Date endTime) {

        boolean isAppEndTime = true;
        if (endTime != null) {
            //判断是否过期
            if (endTime.getTime() >= new Date().getTime()) {
                isAppEndTime = false;
            }
        }
        return isAppEndTime;
    }


    /**
     * 字符串时间 转 date 格式
     *
     * @param time：字符串 时间
     * @return date 时间
     * @throws ParseException 转换错误将抛出异常
     */
    public static Date stringToDate(String time) {
        String dateFormat = getFormatterStr(time);
        if (dateFormat != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            try {
                return sdf.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (time.length() > 10) {
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_4YMD_OF_LINE);
            try {
                return sdf.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 把时间转成几分钟前，几小时前的形式，这里暂时保留7天
     *
     * @param date：时间
     * @return 传进来的时间距离当前时间 多久
     */

    public static String getTimeFormatText(Date date) {

        String timeFormatText = "";

        long minute = 60 * 1000;// 1分钟
        long hour = 60 * minute;// 1小时
        long day = 24 * hour;// 1天

        /**
         * 实现:
         *  如果时间是短期 就显示为（1分钟前、一天前之类的）
         *  7 天以上显示日期格式
         */


        if (date == null) {
            return null;
        }

        long diff = new Date().getTime() - date.getTime();
        long r;

        if (diff > day) {
            r = (diff / day);
            if (r <= 7) {
                timeFormatText = r + "天前";
            }

        } else if (diff > hour) {
            r = (diff / hour);
            timeFormatText = r + "个小时前";

        } else if (diff > minute) {
            r = (diff / minute);
            timeFormatText = r + "分钟前";
        } else {
            timeFormatText = "刚刚";
        }
        return timeFormatText;
    }

    /**
     * 把过期时间转成几小时、几天过期的形式
     *
     * @param date：要转换的时间
     * @return 转换之后的时间
     */

    public static String getEndTimeFormatText(Date date) {

        String endTimeFormatText = dateToString(date);

        long minute = 60 * 1000;// 1分钟
        long hour = 60 * minute;// 1小时
        long day = 24 * hour;// 1天

        //实现 如果 时间 是短期内的话 就显示 为（1分钟前、一天前之类的）、7 天以上显示日期格式
        if (date == null) {
            return null;
        }

        long diff = date.getTime() - new Date().getTime();
        long r;
        if (diff > day) {
            r = (diff / day);
            if (r <= 7) {
                endTimeFormatText = r + "天后过期";
            }

        } else if (diff > hour) {
            r = (diff / hour);
            endTimeFormatText = r + "个小时后过期";

        } else if (diff > minute) {
            r = (diff / minute);
            endTimeFormatText = r + "分钟后过期";

        } else {
            endTimeFormatText = "已过期";
        }
        return endTimeFormatText;

    }

    public static boolean isBefore(LocalDateTime leftDate, LocalDateTime rightDate, String format) {
        String leftDateStr = DateUtil.getStringByLocalDateTime(leftDate, format);
        String rightDateStr = DateUtil.getStringByLocalDateTime(rightDate, format);
        return DateUtil.parseLocalDateTime(leftDateStr, format).isBefore(DateUtil.parseLocalDateTime(rightDateStr, format));
    }

    public static boolean isBefore(LocalDate leftDate, LocalDate rightDate, String format) {
        String leftDateStr = DateUtil.getStringByLocalDate(leftDate, format);
        String rightDateStr = DateUtil.getStringByLocalDate(rightDate, format);
        return DateUtil.parseLocalDateTime(leftDateStr, format).isBefore(DateUtil.parseLocalDateTime(rightDateStr, format));
    }

    /**
     * author dyf
     * 日期计算函数(添加年、月、日)
     * localDateTime 指定日期
     * addNum 指定数值
     * addType 指定数值单位
     * return 返回下一个日期
     */
    public static LocalDateTime getResultDate(LocalDateTime localDateTime, final Integer addNum, final String addType) {
        String resultDate = "";
        if (localDateTime != null) {
            String[] dateArray = getStringByLocalDateTime(localDateTime, "yyyy-MM-dd").split("-");
            final Calendar c = Calendar.getInstance();
            final int resultYear = Integer.parseInt(dateArray[0]) + (addType.equals("年") ? addNum : 0);
            final int resultMonth = Integer.parseInt(dateArray[1]) - 1 + (addType.equals("月") ? addNum : 0);
            int resultDay = Integer.parseInt(dateArray[2]) + (addType.equals("日") ? addNum : 0);
            c.set(resultYear, resultMonth, 1);
            final int maxDay = c.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
            if (!addType.equals("日") && resultDay > maxDay) {
                resultDay = maxDay;
            }
            c.set(resultYear, resultMonth, resultDay);
            return DateUtil.getLocalDateTimeByDate(c.getTime());
        } else {
            return null;
        }

    }

    /**
     * 日期计算函数(添加年、月、日)
     */
    public static LocalDateTime getCurrentResultDate(LocalDateTime localDateTime, final Integer addNum, final String addType) {
        String resultDate = "";
        if (localDateTime == null) {
            localDateTime = LocalDateTime.now();
        }
        String[] dateArray = getStringByLocalDateTime(localDateTime, "yyyy-MM-dd").split("-");
        final Calendar c = Calendar.getInstance();
        final int resultYear = Integer.parseInt(dateArray[0]) + (addType.equals("年") ? addNum : 0);
        final int resultMonth = Integer.parseInt(dateArray[1]) - 1 + (addType.equals("月") ? addNum : 0);
        int resultDay = Integer.parseInt(dateArray[2]) + (addType.equals("日") ? addNum : 0);
        c.set(resultYear, resultMonth, 1);
        final int maxDay = c.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        if (!addType.equals("日") && resultDay > maxDay) {
            resultDay = maxDay;
        }
        c.set(resultYear, resultMonth, resultDay);
        return DateUtil.getLocalDateTimeByDate(c.getTime());

    }

    public static String getResultDate(String localDateTime, String dateFormat, final Integer addNum, final String addType) {
        return DateUtil.getStringByLocalDateTime(getResultDate(DateUtil.parseLocalDateTime(localDateTime, dateFormat), addNum, addType), dateFormat);
    }

    public static String getResultDateFromCurrent(String localDateTime, String dateFormat, final Integer addNum, final String addType) {
        return DateUtil.getStringByLocalDateTime(getCurrentResultDate(DateUtil.parseLocalDateTime(localDateTime, dateFormat), addNum, addType), dateFormat);
    }

    /**
     * 获取指定日期最后一天日期
     *
     * @param localDateTime
     * @return
     */
    public static LocalDateTime getLastDayOfMonth(LocalDateTime localDateTime) {
        return localDateTime.with(TemporalAdjusters.lastDayOfMonth());
    }

    public static String getLastDayOfMonth(String dateStr, String dateFormat) {
        LocalDateTime localDateTime = LocalDateTime.now();
        if (StringUtils.isNotEmpty(dateStr)) {
            localDateTime = DateUtil.parseLocalDateTime(dateStr, dateFormat);
        }
        return DateUtil.getStringByLocalDateTime(getLastDayOfMonth(localDateTime), dateFormat);
    }


    public static String getCurrentDayOfLastMonth(String dateStr, String dateFormat, Integer count) {
        LocalDateTime localDateTime = LocalDateTime.now();
        if (StringUtils.isNotEmpty(dateStr)) {
            localDateTime = DateUtil.parseLocalDateTime(dateStr, dateFormat) != null ? DateUtil.parseLocalDateTime(dateStr, dateFormat) : localDateTime;
        }
        return DateUtil.getStringByLocalDateTime(localDateTime.minus(count, ChronoUnit.MONTHS), dateFormat);
    }

    public static String getCurrentDayOfNextWeek(String dateStr, String dateFormat, Integer count) {
        LocalDateTime localDateTime = LocalDateTime.now();
        if (StringUtils.isNotEmpty(dateStr)) {
            localDateTime = DateUtil.parseLocalDateTime(dateStr, dateFormat) != null ? DateUtil.parseLocalDateTime(dateStr, dateFormat) : localDateTime;
        }
        return DateUtil.getStringByLocalDateTime(localDateTime.minus(count, ChronoUnit.WEEKS), dateFormat);
    }



}
