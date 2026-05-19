package com.gmp.common.utils;

import com.gmp.common.base.BasicConstant;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

/**
 * @author hb176
 * @time 2019-07-30 10:26
 * @email xinhui.chen@abioplus.cn
 * @Description: 文件处理工具类
 */
public class FileUtils {
    private static Logger logger = LoggerFactory.getLogger(FileUtils.class);
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("YYMMddHHmmsssss");
    private static SimpleDateFormat rootPathFormate = new SimpleDateFormat("yyyy/MM/");//上传目录以年月为层级

    // 加密/解密文件的密钥
    public static final int CRYPTO_SECRET_KEY = 0x99;
    public static int FILE_DATA = 0;

    /**
     * 文件处理，把文件转为二进制流
     *
     * @param tmpFile 文件
     * @return
     */
    public static byte[] fileToByte(File tmpFile) {
        byte len[];
        FileInputStream in = null;
        BufferedInputStream inFile = null;
        try {
            in = new FileInputStream(tmpFile);
            inFile = new BufferedInputStream(in);
            final int a = Integer.parseInt(String.valueOf(tmpFile.length()));
            len = new byte[a];
            while (inFile.read(len) != -1) {

            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (inFile != null) {
                try {
                    inFile.close();
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return len;
    }

    /**
     * 文件处理，把二进制文件流转为文件
     *
     * @param bytes    文件流
     * @param filePath 文件存储位置
     * @return
     */
    public static File byteToFile(byte[] bytes, String filePath) throws IOException {
        //保存文件
        File fileToSave = new File(filePath);
        if (!fileToSave.exists()) {
            fileToSave.createNewFile();
        }
        FileCopyUtils.copy(bytes, fileToSave);
        return fileToSave;
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

        //保存文件
        File fileToSave = new File(filePath);
        Files.copy(inputStream, fileToSave.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return fileToSave;
    }

    /**
     * 删除文件夹以及内部文件
     *
     * @param directory
     */
    public static void delDirs(File directory) {
        if (!directory.isDirectory()) {
            directory.delete();
        } else {
            File[] files = directory.listFiles();
            // 空文件夹
            if (files.length == 0) {
                directory.delete();
                return;
            }

            // 删除子文件夹和子文件
            for (File file : files) {
                if (file.isDirectory()) {
                    delDirs(file);
                } else {
                    file.delete();
                }
            }
            // 删除文件夹本身
            directory.delete();
        }
    }

    /**
     * 上传文件转换成字符串
     */
    public static String multipartFileToStr(MultipartFile file) {
        String tmpFolderPath = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        //存储上传文件
        File attachedFile = null;
        try {
            attachedFile = FileUtils.byteToFile(file.getBytes(), tmpFolderPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileStr = null;
        if (attachedFile != null && attachedFile.exists()) {
            fileStr = Base64.encodeBase64String(FileUtils.fileToByte(attachedFile));
            attachedFile.delete();
        }
        return fileStr;
    }


    /**
     * 加密/解密 文件
     *
     * @param srcFile 原文件
     * @param encFile 加密/解密后的文件
     * @throws Exception
     */
    public static void cryptoFile(File srcFile, File encFile) throws Exception {

        InputStream inputStream = new FileInputStream(srcFile);
        OutputStream outputStream = new FileOutputStream(encFile);
        while ((FILE_DATA = inputStream.read()) > -1) {
            outputStream.write(FILE_DATA ^ CRYPTO_SECRET_KEY);
        }
        inputStream.close();
        outputStream.flush();
        outputStream.close();
    }


    /**
     * 上传文件加密
     *
     * @param file
     * @return 加密后的文件
     */
    public static File uploadByJersey(File file) throws Exception {
        String suffix = file.getName().substring(file.getName().lastIndexOf("."));
        // 创建加密文件
        File cryptoFile = new File(file.getPath().substring(0, file.getPath().lastIndexOf(".")) + "en" + file.getPath().substring(file.getPath().lastIndexOf(".")));
        // 执行加密
//        cryptoFile(file, cryptoFile);
        copyFileUsingFileChannels(file, cryptoFile);
        // 保存加密后的文件
        return cryptoFile;
    }

    /**
     * 文件解密
     *
     * @param file
     * @return 解密后的文件
     */
    public static File downloadByURL(File file) throws Exception {
        String absolutePath = file.getAbsolutePath();
        String path = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
        String suffix = file.getName().substring(file.getName().lastIndexOf("."));
        String substring = file.getName().substring(0, file.getName().lastIndexOf("."));
        // 创建解密文件
        File cryptoFile = new File((path + "/" + substring + suffix));
        // 执行解密
//        cryptoFile(file, cryptoFile);
        copyFileUsingFileChannels(file, cryptoFile);
        // 删除下载的原文件
//        file.delete();
        // 保存解密后的文件
        return cryptoFile;
    }

    /**
     * MultipartFile 转 File
     *
     * @param multipartFile
     * @throws Exception
     */
    public static File multipartFileToFile(MultipartFile multipartFile) throws Exception {
        String filePath = multipartFile.getOriginalFilename();
        InputStream ins = multipartFile.getInputStream();
        assert filePath != null;
        File toFile = new File(filePath);
        inputStreamToFile(ins, toFile);
        ins.close();
        return toFile;
    }

    //获取流文件
    private static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除本地临时文件
     *
     * @param file
     */
    public static void delteTempFile(File file) {
        if (file != null) {
            File del = new File(file.toURI());
            del.delete();
        }
    }

    /**
     * 文件复制
     *
     * @param source
     * @param dest
     * @throws IOException
     */
    public static void copyFileUsingFileChannels(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            try {
                inputChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        if (!descDirNames.endsWith(File.separator)) {
            descDirNames = descDirNames + File.separator;
        }
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
     * 压缩文件或目录
     *
     * @param srcDirName   压缩的根目录
     * @param fileName     根目录下的待压缩的文件名或文件夹名，其中*或""表示跟目录下的全部文件
     * @param descFileName 目标zip文件
     */
    public static void zipFiles(String srcDirName, String fileName,
                                String descFileName) {
        // 判断目录是否存在
        if (srcDirName == null) {
            logger.debug("文件压缩失败，目录 " + srcDirName + " 不存在!");
            return;
        }
        File fileDir = new File(srcDirName);
        if (!fileDir.exists() || !fileDir.isDirectory()) {
            logger.debug("文件压缩失败，目录 " + srcDirName + " 不存在!");
            return;
        }
        String dirPath = fileDir.getAbsolutePath();
        File descFile = new File(descFileName);
        try {
            ZipOutputStream zouts = new ZipOutputStream(new FileOutputStream(
                    descFile));
            if ("*".equals(fileName) || "".equals(fileName)) {
                FileUtils.zipDirectoryToZipFile(dirPath, fileDir, zouts);
            } else {
                File file = new File(fileDir, fileName);
                if (file.isFile()) {
                    FileUtils.zipFilesToZipFile(dirPath, file, zouts);
                } else {
                    FileUtils.zipDirectoryToZipFile(dirPath, file, zouts);
                }
            }
            zouts.close();
            logger.debug(descFileName + " 文件压缩成功!");
        } catch (Exception e) {
            logger.debug("文件压缩失败：" + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * 解压缩ZIP文件，将ZIP文件里的内容解压到descFileName目录下
     *
     * @param zipFileName  需要解压的ZIP文件
     * @param descFileName 目标文件
     */
    public static boolean unZipFiles(String zipFileName, String descFileName) {
        String descFileNames = descFileName;
        if (!descFileNames.endsWith(File.separator)) {
            descFileNames = descFileNames + File.separator;
        }
        try {
            // 根据ZIP文件创建ZipFile对象
            ZipFile zipFile = new ZipFile(zipFileName);
            ZipEntry entry = null;
            String entryName = null;
            String descFileDir = null;
            byte[] buf = new byte[4096];
            int readByte = 0;
            // 获取ZIP文件里所有的entry
            @SuppressWarnings("rawtypes")
            Enumeration enums = zipFile.getEntries();
            // 遍历所有entry
            while (enums.hasMoreElements()) {
                entry = (ZipEntry) enums.nextElement();
                // 获得entry的名字
                entryName = entry.getName();
                descFileDir = descFileNames + entryName;
                if (entry.isDirectory()) {
                    // 如果entry是一个目录，则创建目录
                    new File(descFileDir).mkdirs();
                    continue;
                } else {
                    // 如果entry是一个文件，则创建父目录
                    new File(descFileDir).getParentFile().mkdirs();
                }
                File file = new File(descFileDir);
                // 打开文件输出流
                OutputStream os = new FileOutputStream(file);
                // 从ZipFile对象中打开entry的输入流
                InputStream is = zipFile.getInputStream(entry);
                while ((readByte = is.read(buf)) != -1) {
                    os.write(buf, 0, readByte);
                }
                os.close();
                is.close();
            }
            zipFile.close();
            logger.debug("文件解压成功!");
            return true;
        } catch (Exception e) {
            logger.debug("文件解压失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 将目录压缩到ZIP输出流
     *
     * @param dirPath 目录路径
     * @param fileDir 文件信息
     * @param zouts   输出流
     */
    public static void zipDirectoryToZipFile(String dirPath, File fileDir, ZipOutputStream zouts) {
        if (fileDir.isDirectory()) {
            File[] files = fileDir.listFiles();
            // 空的文件夹
            if (files.length == 0) {
                // 目录信息
                ZipEntry entry = new ZipEntry(getEntryName(dirPath, fileDir));
                try {
                    zouts.putNextEntry(entry);
                    zouts.closeEntry();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    // 如果是文件，则调用文件压缩方法
                    FileUtils.zipFilesToZipFile(dirPath, files[i], zouts);
                } else {
                    // 如果是目录，则递归调用
                    FileUtils.zipDirectoryToZipFile(dirPath, files[i],
                            zouts);
                }
            }
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
                flag = FileUtils.deleteFile(files[i].getAbsolutePath());
                // 如果删除文件失败，则退出循环
                if (!flag) {
                    break;
                }
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = FileUtils.deleteDirectory(files[i]
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
     * 将文件压缩到ZIP输出流
     *
     * @param dirPath 目录路径
     * @param file    文件
     * @param zouts   输出流
     */
    public static void zipFilesToZipFile(String dirPath, File file, ZipOutputStream zouts) {
        FileInputStream fin = null;
        ZipEntry entry = null;
        // 创建复制缓冲区
        byte[] buf = new byte[4096];
        int readByte = 0;
        if (file.isFile()) {
            try {
                // 创建一个文件输入流
                fin = new FileInputStream(file);
                // 创建一个ZipEntry
                entry = new ZipEntry(getEntryName(dirPath, file));
                // 存储信息到压缩文件
                zouts.putNextEntry(entry);
                // 复制字节到压缩文件
                while ((readByte = fin.read(buf)) != -1) {
                    zouts.write(buf, 0, readByte);
                }
                zouts.closeEntry();
                fin.close();
                System.out
                        .println("添加文件 " + file.getAbsolutePath() + " 到zip文件中!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取待压缩文件在ZIP文件中entry的名字，即相对于跟目录的相对路径名
     *
     * @param dirPath 目录名
     * @param file    entry文件名
     * @return
     */
    private static String getEntryName(String dirPath, File file) {
        String dirPaths = dirPath;
        if (!dirPaths.endsWith(File.separator)) {
            dirPaths = dirPaths + File.separator;
        }
        String filePath = file.getAbsolutePath();
        // 对于目录，必须在entry名字后面加上"/"，表示它将以目录项存储
        if (file.isDirectory()) {
            filePath += "/";
        }
        int index = filePath.indexOf(dirPaths);

        return filePath.substring(index + dirPaths.length());
    }

    /**
     * 根据“文件名的后缀”获取文件内容类型（而非根据File.getContentType()读取的文件类型）
     *
     * @param returnFileName 带验证的文件名
     * @return 返回文件类型
     */
    public static String getContentType(String returnFileName) {
        String contentType = "application/octet-stream";
        if (returnFileName.lastIndexOf(".") < 0) {
            return contentType;
        }
        returnFileName = returnFileName.toLowerCase();
        returnFileName = returnFileName.substring(returnFileName.lastIndexOf(".") + 1);
        if ("html".equals(returnFileName) || "htm".equals(returnFileName) || "shtml".equals(returnFileName)) {
            contentType = "text/html";
        } else if ("apk".equals(returnFileName)) {
            contentType = "application/vnd.android.package-archive";
        } else if ("sis".equals(returnFileName)) {
            contentType = "application/vnd.symbian.install";
        } else if ("sisx".equals(returnFileName)) {
            contentType = "application/vnd.symbian.install";
        } else if ("exe".equals(returnFileName)) {
            contentType = "application/x-msdownload";
        } else if ("msi".equals(returnFileName)) {
            contentType = "application/x-msdownload";
        } else if ("css".equals(returnFileName)) {
            contentType = "text/css";
        } else if ("xml".equals(returnFileName)) {
            contentType = "text/xml";
        } else if ("gif".equals(returnFileName)) {
            contentType = "image/gif";
        } else if ("jpeg".equals(returnFileName) || "jpg".equals(returnFileName)) {
            contentType = "image/jpeg";
        } else if ("js".equals(returnFileName)) {
            contentType = "application/x-javascript";
        } else if ("atom".equals(returnFileName)) {
            contentType = "application/atom+xml";
        } else if ("rss".equals(returnFileName)) {
            contentType = "application/rss+xml";
        } else if ("mml".equals(returnFileName)) {
            contentType = "text/mathml";
        } else if ("txt".equals(returnFileName)) {
            contentType = "text/plain";
        } else if ("jad".equals(returnFileName)) {
            contentType = "text/vnd.sun.j2me.app-descriptor";
        } else if ("wml".equals(returnFileName)) {
            contentType = "text/vnd.wap.wml";
        } else if ("htc".equals(returnFileName)) {
            contentType = "text/x-component";
        } else if ("png".equals(returnFileName)) {
            contentType = "image/png";
        } else if ("tif".equals(returnFileName) || "tiff".equals(returnFileName)) {
            contentType = "image/tiff";
        } else if ("wbmp".equals(returnFileName)) {
            contentType = "image/vnd.wap.wbmp";
        } else if ("ico".equals(returnFileName)) {
            contentType = "image/x-icon";
        } else if ("jng".equals(returnFileName)) {
            contentType = "image/x-jng";
        } else if ("bmp".equals(returnFileName)) {
            contentType = "image/x-ms-bmp";
        } else if ("svg".equals(returnFileName)) {
            contentType = "image/svg+xml";
        } else if ("jar".equals(returnFileName) || "var".equals(returnFileName)
                || "ear".equals(returnFileName)) {
            contentType = "application/java-archive";
        } else if ("doc".equals(returnFileName)) {
            contentType = "application/msword";
        } else if ("pdf".equals(returnFileName)) {
            contentType = "application/pdf";
        } else if ("rtf".equals(returnFileName)) {
            contentType = "application/rtf";
        } else if ("xls".equals(returnFileName)) {
            contentType = "application/vnd.ms-excel";
        } else if ("ppt".equals(returnFileName)) {
            contentType = "application/vnd.ms-powerpoint";
        } else if ("7z".equals(returnFileName)) {
            contentType = "application/x-7z-compressed";
        } else if ("rar".equals(returnFileName)) {
            contentType = "application/x-rar-compressed";
        } else if ("swf".equals(returnFileName)) {
            contentType = "application/x-shockwave-flash";
        } else if ("rpm".equals(returnFileName)) {
            contentType = "application/x-redhat-package-manager";
        } else if ("der".equals(returnFileName) || "pem".equals(returnFileName)
                || "crt".equals(returnFileName)) {
            contentType = "application/x-x509-ca-cert";
        } else if ("xhtml".equals(returnFileName)) {
            contentType = "application/xhtml+xml";
        } else if ("zip".equals(returnFileName)) {
            contentType = "application/zip";
        } else if ("mid".equals(returnFileName) || "midi".equals(returnFileName)
                || "kar".equals(returnFileName)) {
            contentType = "audio/midi";
        } else if ("mp3".equals(returnFileName)) {
            contentType = "audio/mpeg";
        } else if ("ogg".equals(returnFileName)) {
            contentType = "audio/ogg";
        } else if ("m4a".equals(returnFileName)) {
            contentType = "audio/x-m4a";
        } else if ("ra".equals(returnFileName)) {
            contentType = "audio/x-realaudio";
        } else if ("3gpp".equals(returnFileName)
                || "3gp".equals(returnFileName)) {
            contentType = "video/3gpp";
        } else if ("mp4".equals(returnFileName)) {
            contentType = "video/mp4";
        } else if ("mpeg".equals(returnFileName)
                || "mpg".equals(returnFileName)) {
            contentType = "video/mpeg";
        } else if ("mov".equals(returnFileName)) {
            contentType = "video/quicktime";
        } else if ("flv".equals(returnFileName)) {
            contentType = "video/x-flv";
        } else if ("m4v".equals(returnFileName)) {
            contentType = "video/x-m4v";
        } else if ("mng".equals(returnFileName)) {
            contentType = "video/x-mng";
        } else if ("asx".equals(returnFileName) || "asf".equals(returnFileName)) {
            contentType = "video/x-ms-asf";
        } else if ("wmv".equals(returnFileName)) {
            contentType = "video/x-ms-wmv";
        } else if ("avi".equals(returnFileName)) {
            contentType = "video/x-msvideo";
        }
        return contentType;
    }


    /**
     * 修正路径，将 \\ 或 / 等替换为 File.separator
     *
     * @param path 待修正的路径
     * @return 修正后的路径
     */
    public static String path(String path) {
        String p = org.apache.commons.lang3.StringUtils.replace(path, "\\", "/");
        p = org.apache.commons.lang3.StringUtils.join(org.apache.commons.lang3.StringUtils.split(p, "/"), "/");
        if (!org.apache.commons.lang3.StringUtils.startsWithAny(p, "/") && org.apache.commons.lang3.StringUtils.startsWithAny(path, "\\", "/")) {
            p += "/";
        }
        if (!org.apache.commons.lang3.StringUtils.endsWithAny(p, "/") && org.apache.commons.lang3.StringUtils.endsWithAny(path, "\\", "/")) {
            p = p + "/";
        }
        if (path != null && path.startsWith("/")) {
            p = "/" + p; // linux下路径
        }
        return p;
    }

    /**
     * 获目录下的文件列表
     *
     * @param dir        搜索目录
     * @param searchDirs 是否是搜索目录
     * @return 文件列表
     */
    public static List<String> findChildrenList(File dir, boolean searchDirs) {
        List<String> files = new ArrayList<>();
        for (String subFiles : dir.list()) {
            File file = new File(dir + "/" + subFiles);
            if (((searchDirs) && (file.isDirectory())) || ((!searchDirs) && (!file.isDirectory()))) {
                files.add(file.getName());
            }
        }
        return files;
    }

    /**
     * 获取文件扩展名(返回小写)
     *
     * @param fileName 文件名
     * @return 例如：test.jpg  返回：  jpg
     */
    public static String getFileExtension(String fileName) {
        if ((fileName == null) || (fileName.lastIndexOf(".") == -1) || (fileName.lastIndexOf(".") == fileName.length() - 1)) {
            return null;
        }
        return StringUtils.lowerCase(fileName.substring(fileName.lastIndexOf(".") + 1));
    }

    /**
     * 获取文件名，不包含扩展名
     *
     * @param fileName 文件名
     * @return 例如：d:\files\test.jpg  返回：d:\files\test
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if ((fileName == null) || (fileName.lastIndexOf(".") == -1)) {
            return null;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    /**
     * 文件名是否包含乱码
     *
     * @param str
     * @return
     */
    public static boolean isMessyCode(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((int) c == 0xfffd) {
                // 存在乱码
                return true;
            }
        }
        return false;
    }

    /**
     * @param file
     * @return
     */
    public static String txt2String(File file) {
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String s = null;
            while ((s = br.readLine()) != null) {//使用readLine方法，一次读一行
                result.append("<br>" + s);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }


}
