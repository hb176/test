package com.gmp.file.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author hb176
 * @time 2023/3/27 10:54
 * @email zhaoxuan.hou@abioplus.cn
 * @Description: TODO
 */
public class Md5Utils {

    public static String getMD5(String filePath) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(filePath);
        FileChannel channel = fis.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);

        while (channel.read(buffer) != -1) {
            buffer.flip();
            md.update(buffer);
            buffer.clear();
        }

        channel.close();
        fis.close();

        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    //计算文件(MultipartFile)的MD5

    public static String getMd5ByMultipartFile(MultipartFile file) {

        byte[] uploadBytes = new byte[0];
        StringBuilder sb = new StringBuilder();
//        String fileMd5 = null;
        try {
            uploadBytes = file.getBytes();
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(uploadBytes);
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static String getMD5ByInputStream(FileInputStream fis) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileChannel channel = fis.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);

        while (channel.read(buffer) != -1) {
            buffer.flip();
            md.update(buffer);
            buffer.clear();
        }

        channel.close();
        fis.close();

        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
