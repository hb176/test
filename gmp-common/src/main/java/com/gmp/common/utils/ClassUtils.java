package com.gmp.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author hb176
 * @time 2018-09-05 17:34
 * @email xinhui.chen@abioplus.cn
 * @Description: 通过包名扫描class
 */
public class ClassUtils {

    public static Field getAllDeclaredField(Class clas, String fieldName) throws NoSuchFieldException {
        Field[] fields = clas.getDeclaredFields();
        for (Field field: fields) {
            if(field.getName().equals(fieldName)){
                return field;
            }
        }
        Class clsSup = clas.getSuperclass();
        if(clsSup != null)
            return getAllDeclaredField(clsSup,fieldName);
        throw new NoSuchFieldException(fieldName);
    }

    public static Field[] getAllFieldsByClass(Class<?> clas){
        Field[] fields = clas.getDeclaredFields();
        // 判断是否有父类，如果有拉取父类的field，这里只支持多层继承
        fields = recursionParents(clas, fields);
        return fields;
    }

    /**
     * 递归扫描父类的fields
     *
     * @param clas
     *            类
     * @param fields
     *            属性
     */
    public static Field[] recursionParents(Class<?> clas, Field[] fields) {
        Class clsSup = clas.getSuperclass();
        if (clsSup != null) {
            List<Field> fieldList = new ArrayList<Field>();
            List<String> fieldNameList = new ArrayList<>();
            for (Field tmpField: fields) {
                fieldList.add(tmpField);
                fieldNameList.add(tmpField.getName());
            }
            for (Field tmpField: clsSup.getDeclaredFields()) {
                if(!fieldNameList.contains(tmpField.getName())){
                    fieldList.add(tmpField);
                }
            }
            fields = recursionParents(clsSup, fieldList.toArray(new Field[fieldList.size()]));
        }
        return fields;
    }

    /**
     * 获取实体类中的某个值
     * @param entity
     * @param fileName
     * @return
     */
    public static Object getFileValue(Object entity, String fileName){
        try {
            Class cla = entity.getClass();
            String tmpMethodName = fileName;
            if(!fileName.startsWith("is")){
                tmpMethodName = "get" + fileName.substring(0, 1).toUpperCase() + fileName.substring(1);
            }
            Method method = cla.getMethod(tmpMethodName);
            Object invoke = method.invoke(entity);
            return null == invoke ? null : invoke;
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置实体类中的某个属性值
     * @param entity
     * @param field
     * @param fileValue
     * @return
     */
    public static boolean setFileValue(Object entity, Field field, Object fileValue){
        Class<?> thisClass = entity.getClass();
        String fileName = field.getName();
        try {
            if ("ID".equalsIgnoreCase(fileName)) {
                try {
                    String calssName = field.getType().getName();
                    if (calssName.equals("int") || calssName.equals("java.lang.Integer")) {
                        if (Integer.MAX_VALUE >  new Integer("" + fileValue)) {
                            Integer val = new Integer("" + fileValue);
                            Method method = thisClass.getMethod("set" + fileName.substring(0, 1).toUpperCase() + fileName.substring(1), field.getType());
                            method.invoke(entity, val);
                            return true;
                        }else{
                            throw new RuntimeException("ID type is not a corresponding type at " + "set" + fileName.substring(0, 1).toUpperCase() + fileName.substring(1) + "\n"
                                    + "the will give value type is " + fileValue.getClass().getName() + "\n"
                                    + "the filed type type is " + field.getType().getName());
                        }
                    }else if(calssName.equals("long") || calssName.equals("java.lang.Long")){
                        Long val = new Long("" + fileValue);
                        Method method = thisClass.getMethod("set" + fileName.substring(0, 1).toUpperCase() + fileName.substring(1), field.getType());
                        method.invoke(entity, val);
                        return true;
                    }else{
                        Method method = thisClass.getMethod("set" + fileName.substring(0, 1).toUpperCase() + fileName.substring(1), field.getType());
                        method.invoke(entity, fileValue);
                        return true;
                    }
                }catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            if (null != fileValue) {
                String tmpMethodName = "set" + fileName.substring(0, 1).toUpperCase() + fileName.substring(1);
                try {
                    Method method = null;
                    if(field.getType().equals(LocalDateTime.class)){
                        method = thisClass.getMethod(tmpMethodName, LocalDateTime.class);
                        method.invoke(entity, DateUtil.parseDefLocalDateTime(fileValue+""));
                    }else if(field.getType().equals(Date.class)){
                        method = thisClass.getMethod(tmpMethodName, Date.class);
                        method.invoke(entity, DateUtil.stringToDate(fileValue+""));
                    }else{
                        method = thisClass.getMethod(tmpMethodName, field.getType());
                        method.invoke(entity, field.getType().getConstructor(String.class).newInstance(fileValue+""));
                        return true;
                    }
                    method.invoke(entity, fileValue);
                } catch (NoSuchMethodException e) {
                    try {
                        Method method = thisClass.getMethod(tmpMethodName, Boolean.class);
                        if(method == null && fileName.startsWith("is")){
                            method = thisClass.getMethod("set" + fileName.substring(2), Boolean.class);
                        }
                        method.invoke(entity,  (fileValue instanceof Integer)?(new Integer("" + fileValue) > 0 ? true : false):fileValue);
                    } catch (NoSuchMethodException e2) {
                        //e2.printStackTrace();
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }  catch (SecurityException e) {
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }
}
