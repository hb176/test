package com.gmp.file.utils;


import com.gmp.common.base.BasicConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件操作工具类
 *
 * @Description: 实现文件的创建、删除、复制、压缩、解压以及目录的创建、删除、复制、压缩解压等功能
 * @author hb176
 */
public class FileUtil extends org.apache.commons.io.FileUtils {

    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 复制单个文件，如果目标文件存在，则不覆盖
     *
     * @param srcFileName  待复制的文件名
     * @param descFileName 目标文件名
     * @return 如果复制成功，则返回true，否则返回false
     */
    public static boolean copyFile(String srcFileName, String descFileName) {
        return FileUtil.copyFileCover(srcFileName, descFileName, false);
    }

    /**
     * 判断文件是否存在
     *
     * @param srcFilePath
     * @return
     */
    public static boolean checkFileExists(String srcFilePath) {
        File srcFile = new File(srcFilePath);
        // 判断文件是否存在
        if (!srcFile.exists()) {
            return false;
        }
        return true;
    }



    /**
     * 复制单个文件
     *
     * @param srcFileName  待复制的文件名
     * @param descFileName 目标文件名
     * @param coverlay     如果目标文件已存在，是否覆盖
     * @return 如果复制成功，则返回true，否则返回false
     */
    public static boolean copyFileCover(String srcFileName,
                                        String descFileName, boolean coverlay) {
        File srcFile = new File(srcFileName);
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            logger.debug("复制文件失败，源文件 " + srcFileName + " 不存在!");
            return false;
        }
        // 判断源文件是否是合法的文件
        else if (!srcFile.isFile()) {
            logger.debug("复制文件失败，" + srcFileName + " 不是一个文件!");
            return false;
        }
        File descFile = new File(descFileName);
        // 判断目标文件是否存在
        if (descFile.exists()) {
            // 如果目标文件存在，并且允许覆盖
            if (coverlay) {
                logger.debug("目标文件已存在，准备删除!");
                if (!FileUtil.delFile(descFileName)) {
                    logger.debug("删除目标文件 " + descFileName + " 失败!");
                    return false;
                }
            } else {
                logger.debug("复制文件失败，目标文件 " + descFileName + " 已存在!");
                return false;
            }
        } else {
            if (!descFile.getParentFile().exists()) {
                // 如果目标文件所在的目录不存在，则创建目录
                logger.debug("目标文件所在的目录不存在，创建目录!");
                // 创建目标文件所在的目录
                if (!descFile.getParentFile().mkdirs()) {
                    logger.debug("创建目标文件所在的目录失败!");
                    return false;
                }
            }
        }

        // 准备复制文件
        // 读取的位数
        int readByte = 0;
        InputStream ins = null;
        OutputStream outs = null;
        try {
            // 打开源文件
            ins = new FileInputStream(srcFile);
            // 打开目标文件的输出流
            outs = new FileOutputStream(descFile);
            byte[] buf = new byte[1024];
            // 一次读取1024个字节，当readByte为-1时表示文件已经读取完毕
            while ((readByte = ins.read(buf)) != -1) {
                // 将读取的字节流写入到输出流
                outs.write(buf, 0, readByte);
            }
            logger.debug("复制单个文件 " + srcFileName + " 到" + descFileName
                    + "成功!");
            return true;
        } catch (Exception e) {
            logger.debug("复制文件失败：" + e.getMessage());
            return false;
        } finally {
            // 关闭输入输出流，首先关闭输出流，然后再关闭输入流
            if (outs != null) {
                try {
                    outs.close();
                } catch (IOException oute) {
                    oute.printStackTrace();
                }
            }
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ine) {
                    ine.printStackTrace();
                }
            }
        }
    }

    /**
     * 复制整个目录的内容，如果目标目录存在，则不覆盖
     *
     * @param srcDirName  源目录名
     * @param descDirName 目标目录名
     * @return 如果复制成功返回true，否则返回false
     */
    public static boolean copyDirectory(String srcDirName, String descDirName) {
        return FileUtil.copyDirectoryCover(srcDirName, descDirName,
                false);
    }

    /**
     * 复制整个目录的内容
     *
     * @param srcDirName  源目录名
     * @param descDirName 目标目录名
     * @param coverlay    如果目标目录存在，是否覆盖
     * @return 如果复制成功返回true，否则返回false
     */
    public static boolean copyDirectoryCover(String srcDirName,
                                             String descDirName, boolean coverlay) {
        File srcDir = new File(srcDirName);
        // 判断源目录是否存在
        if (!srcDir.exists()) {
            logger.debug("复制目录失败，源目录 " + srcDirName + " 不存在!");
            return false;
        }
        // 判断源目录是否是目录
        else if (!srcDir.isDirectory()) {
            logger.debug("复制目录失败，" + srcDirName + " 不是一个目录!");
            return false;
        }
        // 如果目标文件夹名不以文件分隔符结尾，自动添加文件分隔符
        String descDirNames = descDirName;
        if (!descDirNames.endsWith(File.separator)) {
            descDirNames = descDirNames + File.separator;
        }
        File descDir = new File(descDirNames);
        // 如果目标文件夹存在
        if (descDir.exists()) {
            if (coverlay) {
                // 允许覆盖目标目录
                logger.debug("目标目录已存在，准备删除!");
                if (!FileUtil.delFile(descDirNames)) {
                    logger.debug("删除目录 " + descDirNames + " 失败!");
                    return false;
                }
            } else {
                logger.debug("目标目录复制失败，目标目录 " + descDirNames + " 已存在!");
                return false;
            }
        } else {
            // 创建目标目录
            logger.debug("目标目录不存在，准备创建!");
            if (!descDir.mkdirs()) {
                logger.debug("创建目标目录失败!");
                return false;
            }

        }

        boolean flag = true;
        // 列出源目录下的所有文件名和子目录名
        File[] files = srcDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 如果是一个单个文件，则直接复制
            if (files[i].isFile()) {
                flag = FileUtil.copyFile(files[i].getAbsolutePath(),
                        descDirName + files[i].getName());
                // 如果拷贝文件失败，则退出循环
                if (!flag) {
                    break;
                }
            }
            // 如果是子目录，则继续复制目录
            if (files[i].isDirectory()) {
                flag = FileUtil.copyDirectory(files[i]
                        .getAbsolutePath(), descDirName + files[i].getName());
                // 如果拷贝目录失败，则退出循环
                if (!flag) {
                    break;
                }
            }
        }

        if (!flag) {
            logger.debug("复制目录 " + srcDirName + " 到 " + descDirName + " 失败!");
            return false;
        }
        logger.debug("复制目录 " + srcDirName + " 到 " + descDirName + " 成功!");
        return true;

    }

    /**
     * 删除文件，可以删除单个文件或文件夹
     *
     * @param fileName 被删除的文件名
     * @return 如果删除成功，则返回true，否是返回false
     */
    public static boolean delFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            logger.debug(fileName + " 文件不存在!");
            return true;
        } else {
            if (file.isFile()) {
                return FileUtil.deleteFile(fileName);
            } else {
                return FileUtil.deleteDirectory(fileName);
            }
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName 被删除的文件名
     * @return 如果删除成功，则返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                logger.debug("删除文件 " + fileName + " 成功!");
                return true;
            } else {
                logger.debug("删除文件 " + fileName + " 失败!");
                return false;
            }
        } else {
            logger.debug(fileName + " 文件不存在!");
            return true;
        }
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dirName 被删除的目录所在的文件路径
     * @return 如果目录删除成功，则返回true，否则返回false
     */
    public static boolean deleteDirectory(String dirName) {
        String dirNames = dirName;
        if (!dirNames.endsWith(File.separator)) {
            dirNames = dirNames + File.separator;
        }
        File dirFile = new File(dirNames);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            logger.debug(dirNames + " 目录不存在!");
            return true;
        }
        boolean flag = true;
        // 列出全部文件及子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = FileUtil.deleteFile(files[i].getAbsolutePath());
                // 如果删除文件失败，则退出循环
                if (!flag) {
                    break;
                }
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = FileUtil.deleteDirectory(files[i]
                        .getAbsolutePath());
                // 如果删除子目录失败，则退出循环
                if (!flag) {
                    break;
                }
            }
        }

        if (!flag) {
            logger.debug("删除目录失败!");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            logger.debug("删除目录 " + dirName + " 成功!");
            return true;
        } else {
            logger.debug("删除目录 " + dirName + " 失败!");
            return false;
        }

    }

    /**
     * 创建单个文件
     *
     * @param descFileName 文件名，包含路径
     * @return 如果创建成功，则返回true，否则返回false
     */
    public static boolean createFile(String descFileName) {
        File file = new File(descFileName);
        if (file.exists()) {
            logger.debug("文件 " + descFileName + " 已存在!");
            return false;
        }
        if (descFileName.endsWith(File.separator)) {
            logger.debug(descFileName + " 为目录，不能创建目录!");
            return false;
        }
        if (!file.getParentFile().exists()) {
            // 如果文件所在的目录不存在，则创建目录
            if (!file.getParentFile().mkdirs()) {
                logger.debug("创建文件所在的目录失败!");
                return false;
            }
        }

        // 创建文件
        try {
            if (file.createNewFile()) {
                logger.debug(descFileName + " 文件创建成功!");
                return true;
            } else {
                logger.debug(descFileName + " 文件创建失败!");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(descFileName + " 文件创建失败!");
            return false;
        }

    }

    /**
     * 创建目录
     *
     * @param descDirName 目录名,包含路径
     * @return 如果创建成功，则返回true，否则返回false
     */
    public static boolean createDirectory(String descDirName) {
        String descDirNames = descDirName;
//        if (!descDirNames.endsWith(File.separator)) {
//            descDirNames = descDirNames + File.separator;
//        }
        File descDir = new File(descDirNames);
        if (descDir.exists()) {
            logger.debug("目录 " + descDirNames + " 已存在!");
            return false;
        }
        // 创建目录
        if (descDir.mkdirs()) {
            logger.debug("目录 " + descDirNames + " 创建成功!");
            return true;
        } else {
            logger.debug("目录 " + descDirNames + " 创建失败!");
            return false;
        }

    }

    /**
     * 写入文件
     */
    public static void writeToFile(String fileName, String content, boolean append) {
        try {
            write(new File(fileName), content, "utf-8", append);
            logger.debug("文件 " + fileName + " 写入成功!");
        } catch (IOException e) {
            logger.debug("文件 " + fileName + " 写入失败! " + e.getMessage());
        }
    }

    /**
     * 写入文件
     */
    public static void writeToFile(String fileName, String content, String encoding, boolean append) {
        try {
            write(new File(fileName), content, encoding, append);
            logger.debug("文件 " + fileName + " 写入成功!");
        } catch (IOException e) {
            logger.debug("文件 " + fileName + " 写入失败! " + e.getMessage());
        }
    }

//    public static void main(String[] args) {
//        deleteFilesWithDays("D:\\workspace\\confhome\\xsd\\viewoffice", 1);
////        mergePdfFile(new String[]{"D:\\2222.pdf"}, "D:\\test2\\All.pdf");
//    }

    /**
     * 删除文件夹下几天前的文件
     *
     * @param folderPath
     */
    public static void deleteFilesWithDays(String folderPath, Integer days) {
        long t1 = LocalDate.now().minusDays(days).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        //转换成时间戳
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            for (File file : files) {
                if (!file.isDirectory()) {
                    long createTime = file.lastModified();
                    if (createTime < t1) {
                        boolean isDeleted = file.delete();
                        if (isDeleted) {
                            logger.debug("已删除文件：" + file.getName());
                        } else {
                            logger.debug("无法删除文件：" + file.getName());
                        }
                    }
                } else {
                    deleteFilesWithDays(file.getAbsolutePath(), 1);
                    boolean isDeleted = file.delete();
                    if (isDeleted) {
                        logger.debug("已删除文件夹：" + file.getName());
                    } else {
                        logger.debug("无法删除文件夹：" + file.getName());
                    }
                }
            }
        } else {
            logger.debug("文件夹不存在或者不是目录");
        }
    }

    /**
     * 获目录下的文件列表
     *
     * @param srcFilePath 文件地址
     * @param srcFileName 文件地址
     * @param targetPath  文件地址
     * @param count       是否是搜索目录
     * @return
     */
    public static String copyFileToArray(String srcFilePath, String srcFileName, String targetPath, String alias, Integer count) {
        File srcFile = new File(srcFilePath + srcFileName);
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            logger.debug("复制文件失败，源文件不存在!");
            return null;
        }
        List<String> fileList = new ArrayList<>();
        for (Integer i = 0; i < count; i++) {
            String srcFileNames = UUID.randomUUID().toString().replaceAll("-", "") + i + alias;
            //需要复制文件
            copyFileCover(srcFilePath + srcFileName, targetPath + srcFileNames, true);
            fileList.add(targetPath + srcFileNames);
        }
        String[] fileArray = fileList.toArray(new String[0]);
        String outputFileName = UUID.randomUUID().toString().replaceAll("-", "") + alias;
        String pdfPath = PdfUtils.mergePDF(fileArray, targetPath, outputFileName);
        for (String delePath : fileList) {
            deleteFile(delePath);
        }
        return pdfPath;
    }

    /**
     * 文件处理，把二进制文件流转为文件
     *
     * @param inputStream 文件流
     * @param filePath    文件存储位置
     * @return
     */
    public static File inputStreamToFile(InputStream inputStream, String filePath) throws IOException {
        String folderPath = filePath.substring(0, filePath.lastIndexOf(BasicConstant.SPLIT_REGEX06));
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        deleteFile(filePath);
        //保存文件
        File fileToSave = new File(filePath);
        copyInputStreamToFile(inputStream, fileToSave);
        return fileToSave;
    }

    public static void main(String[] args) {


        String str = "#QMS_CC_S2PB_name_202403291102389_startChildBySql ==+'yes'+?'path1094':'path1092'";
//        String str = "(#QMS_DEV_S2PC_Assess_no_startChildBySql != +'no'+ and #QMS_DEV_S2PC_Assess222_no_startChildBySql == +'yes'+ and #QMS_DEV_S2PC_Assess333_no_startChildBySql != +'no'+)?'path1094':'path1092'";
        String keyword = "_startChildBySql";

        // 定义匹配关键字之后的字符串的正则表达式
        String regex = Pattern.quote(keyword) + "\\s*([!=]{2})\\s*\\+?'([^']+)'\\+?";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);


        List<String> results = new ArrayList<>();
        while (matcher.find()) {

            String symbol = matcher.group(1).trim(); // 获取第一个括号内的字符串，并去除首尾空格
            String value = matcher.group(2).trim(); // 获取第二个括号内的字符串，并去除首尾空格

            // 提取QMS_DEV_S2PC_Assess_no
            int startIndex = str.lastIndexOf("#", matcher.start()) + 1;
            int endIndex = matcher.start();
            String prefix = str.substring(startIndex, endIndex).trim();
            results.add(prefix);
            results.add(symbol);
            results.add(value);
        }


        if (results.size() > 0) {
            System.out.println("提取到的字符串为: " + String.join(", ", results));
        } else {
            System.out.println("未找到匹配的字符串");
        }
    }

}

