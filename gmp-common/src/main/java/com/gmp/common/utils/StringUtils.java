package com.gmp.common.utils;

import cn.hutool.core.convert.Convert;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hb176
 * @time 2019-04-17 10:26
 * @email xinhui.chen@abioplus.cn
 * @Description: 字符串处理工具类
 */
public class StringUtils {
    public static String TIME_UNIT_YEAR = "年";
    public static String TIME_UNIT_MONTH = "月";
    public static String TIME_UNIT_DAY = "日";

    /**
     * 判断一个字符串是否为数字
     * 可以判断正负、整数小数
     * ?:0或1个, *:0或多个, +:1或多个
     */
    public static boolean isNumber(String str) {
        return str.matches("-?[0-9]+.*[0-9]*");
    }

    /**
     * 根据正则表达式获取索引
     */
    public static List<String> getIndxsByRegex(String str, String regex) {
        List<String> resultList = new ArrayList<String>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        while (m.find()) {
            resultList.add(m.start() + "_" + m.end());
        }
        return resultList;
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return str != null && str.length() > 0;
    }

    /**
     * 日期计算函数(添加年、月、日)
     */
    public static LocalDateTime getResultDate(final LocalDateTime dateVal, final Integer addNum, final String addType, String dateFormat) {
        String strDate = DateUtil.getStringByLocalDateTime(dateVal, "yyyy-MM-dd HH:mm:ss");
        return DateUtil.parseLocalDateTime(getResultDate(strDate, addNum, addType) + strDate.substring(10), dateFormat);
    }

    public static LocalDateTime getResultDate(final LocalDateTime dateVal, final Integer addNum, final String addType) {
        String dateFormat = "yyyy-MM-dd HH:mm:ss";
        return getResultDate(dateVal, addNum, addType, dateFormat);
    }

    public static String getResultDate(final String dateVal, final Integer addNum, final String addType) {
        String resultDate = "";
        final String[] dateArray = dateVal.substring(0, 10).split("-");
        if (dateArray.length == 3) {
            final Calendar c = Calendar.getInstance();
            final int resultYear = Integer.parseInt(dateArray[0]) + (addType.equals(TIME_UNIT_YEAR) ? addNum : 0);
            final int resultMonth = Integer.parseInt(dateArray[1]) - 1 + (addType.equals(TIME_UNIT_MONTH) ? addNum : 0);
            int resultDay = Integer.parseInt(dateArray[2]) + (addType.equals(TIME_UNIT_DAY) ? addNum : 0);
            c.set(resultYear, resultMonth, 1);
            final int maxDay = c.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
            if (!addType.equals("日") && resultDay > maxDay) {
                resultDay = maxDay;
            }
            c.set(resultYear, resultMonth, resultDay);
            resultDate = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
        }
        return resultDate;
    }

    /**
     * 字符窜转日期
     *
     * @param date
     * @param format
     * @return date
     */
    public static Date stringToDate(final String date, final String format) {
        if (StringUtils.isEmpty(date)) return null;
        final SimpleDateFormat dateformat = new SimpleDateFormat(format);
        try {
            return dateformat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期转字符串
     *
     * @param date
     * @param format
     * @return string
     */
    public static String dateToString(final Date date, final String format) {
        if (date == null) {
            return "";
        }
        final SimpleDateFormat dateformat = new SimpleDateFormat(format);
        return dateformat.format(date);
    }

    /**
     * 去除重复
     *
     * @param list
     * @return
     */
    public static List distinctList(List list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = list.size() - 1; j > i; j--) {
                if (list.get(j).equals(list.get(i))) {
                    list.remove(j);
                }
            }
        }
        return list;
    }

    /**
     * 获取request对象中obj对象以数组返回
     *
     * @param obj
     * @return
     */
    public static String[] getArray(Object obj) {
        obj = parseRequestObj(obj);
        if (obj != null) {
            if (obj instanceof String[]) {
                return (String[]) obj;
            } else {
                return new String[]{obj.toString()};
            }
        }
        return null;
    }

    /**
     * 获取request对象中obj对象以字符串返回
     *
     * @param obj
     * @return
     */
    public static String getStrByObj(Object obj) {
        if (obj == null) {
            return null;
        } else {
            return getArray(obj)[0];
        }
    }

    /**
     * 把从request中获取的对象转换成真正类型
     *
     * @param value
     * @return
     */
    public static Object parseRequestObj(Object value) {
        if (value != null && value instanceof String[]) {
            final String[] toValue = (String[]) value;
            if (toValue.length == 1) {
                return convertSpecialChar(toValue[0]);
            } else {
                for (int i = 0; i < toValue.length; i++) {
                    toValue[i] = convertSpecialChar(toValue[i]);
                }
                return toValue;
            }
        } else {
            if (value != null && value instanceof String) {
                return convertSpecialChar(value.toString());
            } else {
                return value;
            }
        }
    }

    /**
     * obj转list
     *
     * @param obj
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> objCastList(Object obj, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }


    /**
     * 查询object中是否为""
     *
     * @param value
     * @return
     */
    public static boolean objExistEmpty(Object value) {
        if (value instanceof Collection<?>) {
            if (CollectionUtils.isEmpty(Convert.toList(value)) || "".equals(Convert.toList(value).get(0))) {
                return true;
            } else {
                return false;
            }
        } else {
            if (org.springframework.util.StringUtils.isEmpty(value)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 字符串 千位符  保留两位小数点后两位
     *
     * @param num
     * @return
     */
    public static String num2thousand00(String num) {
        if (!isNumeric(num)) {//如果不是数字则直接返回
            return num;
        }

        DecimalFormat df = new DecimalFormat("#,##0.00");
//使用BigDecimal 进行格式化 防止出现数据过大 进行千分位格式化 并且保留2位小数
        String numStr = df.format(new BigDecimal(num));
        return numStr;
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("-?[0-9]+.?[0-9]+");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {//先按照正则表达式判断是否符合数字规则
            try {
                Double.parseDouble(str);//为防止出现科学计数法匹配不上正则的问题这里试着转化下
                return true;
            } catch (Exception e) {
                return false;   //既不是普通数字 也不是科学计数法则返回false
            }
        }
        return true;
    }

    /**
     * 合并两个list
     *
     * @param str
     * @return
     */
    public static Object collectOneList(Object str) {
        if (str instanceof Collection<?>) {
            List<Object> objects = objCastList(str, Object.class);
            if (null != objects && objects.size() > 1) {
                List<Object> objects1 = new LinkedList<>();
                for (int i = 0; i < objects.size(); i++) {
                    objects1 = Stream.of(objects1, objCastList(objects.get(i), Object.class)).flatMap(Collection::stream).collect(Collectors.toList());
                }
                return objects1;
            }
            return objects.get(0);
        }
        return str;
    }

    /**
     * 转换特殊字符：解决页面特殊字符的Bug
     *
     * @param str
     * @return str
     */
    public static String convertSpecialChar(String str) {
        if (str != null) {
            // 双引号
            if (str.indexOf("\"") > -1) {
                str = str.replaceAll("\"", "&quot;");
            }
            // 大于号
            if (str.indexOf(">") > -1) {
                str = str.replaceAll(">", "&gt;");
            }
            // 小于号
            if (str.indexOf("<") > -1) {
                str = str.replaceAll("<", "&lt;");
            }
        }
        return str;
    }

    /**
     * 转换特殊字符：解决页面特殊字符的Bug
     *
     * @param str
     * @return str
     */
    public static String convertHtmlChar(String str) {
        if (str != null) {
            // 双引号
            if (str.indexOf("&quot;") > -1) {
                str = str.replaceAll("&quot;", "\"");
            }
            // 大于号
            if (str.indexOf("&gt;") > -1) {
                str = str.replaceAll("&gt;", ">");
            }
            // 小于号
            if (str.indexOf("&lt;") > -1) {
                str = str.replaceAll("&lt;", "<");
            }
        }
        return str;
    }




    // 将字Clob转成String类型
    public static String ClobToString(Clob sc) throws SQLException, IOException {
        String reString = "";
        Reader is = sc.getCharacterStream();// 得到流
        BufferedReader br = new BufferedReader(is);
        String s = br.readLine();
        StringBuffer sb = new StringBuffer();
        while (s != null) {// 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING
            sb.append(s);
            s = br.readLine();
        }
        br.close();
        is.close();
        reString = sb.toString();
        return reString;
    }

    //判断数组中是否存在某字符串
    public static boolean contains(String[] array, String str) {
        boolean result = false;
        for (String tmpStr : array) {
            if (tmpStr.trim().equals(str.trim())) {
                result = true;
                break;
            }
        }
        return result;
    }


    /**
     * 校验字符是否为空或者null.
     *
     * <pre>
     *      StringUtils.isBlank(null)      = true
     *      StringUtils.isBlank("")        = true
     *      StringUtils.isBlank(" ")       = true
     *      StringUtils.isBlank("bob")     = false
     *      StringUtils.isBlank("  bob  ") = false
     * </pre>
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取数字最后一个元素
     * <pre>
     *      String[]
     * </pre>
     */
    public static String resultMsg(String str) {
        String msg = null;
        if (str != null) {
            String[] strings = str.split("。");
            for (int count = 0; count < strings.length; count++) {
                msg = strings[strings.length - 1];
            }
            return msg;
        }
        return str;
    }

    public static String[] objectToStringArray(Object obj) {
        int length = Array.getLength(obj);
        String[] stringArray = new String[length];
        for (int i = 0; i < stringArray.length; i++) {
            stringArray[i] = (String) Array.get(obj, i);
        }
        return stringArray;
    }

    public static Map<String, Object> getStringToMap(String str) {
        //根据逗号截取字符串数组
        String[] str1 = str.split(",");
        //创建Map对象
        Map<String, Object> map = new HashMap<>();
        //循环加入map集合
        for (int i = 0; i < str1.length; i++) {
            //根据":"截取字符串数组
            String[] str2 = str1[i].split("=");
            //str2[0]为KEY,str2[1]为值
            //map.put(str2[0],str2[1]);

            if (str2.length == 2) {
                map.put(str2[0].trim(), str2[1]);
            } else {
                map.put(str2[0].trim(), "");
            }
        }
        return map;
    }

    public static String deleteElement(String inputString){
        String[] elements = inputString.split(",");

        StringBuilder result = new StringBuilder();
        for (String element : elements) {
            if (!element.equals(inputString)) {
                result.append(element).append(",");
            }
        }

        // Remove the trailing comma if any
        if (result.length() > 0) {
            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }


    /**
     * 判断object中是否包含null或者空串
     *
     * @param object
     * @return
     */
    public static boolean isBlankOrEmpty(Object object) {
        return null == object || object.toString().equals("");
    }

}
