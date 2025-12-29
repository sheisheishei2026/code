package com.alicloud.databox.opensdk.io;

import java.io.FileInputStream;
import java.security.MessageDigest;

public class MessageDigestHelper {
    private static final String SHA1 = "SHA-1";
    private static final String SHA256 = "SHA-256";
    private static final String MD5 = "MD5";

    /**
     * Gen sha1
     * @param path 读取文件全量算出sha1
     * @return SHA1 hash string
     */
    public static String getFileSHA1(String path) throws Exception {
        if (path.isEmpty()) {
            return null;
        }
        
        try (FileInputStream inputStream = new FileInputStream(path)) {
            byte[] buffer = new byte[1024 * 100];
            MessageDigest digest = MessageDigest.getInstance(SHA1);
            int numRead;
            
            while ((numRead = inputStream.read(buffer)) != -1) {
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            }
            
            byte[] sha1Bytes = digest.digest();
            return bytesToHex(sha1Bytes);
        }
    }

    /**
     * Gen pre sha1
     * @param path 读取文件前1024字节 算出预备sha1
     * @return Pre-SHA1 hash string
     */
    public static String getFilePreSHA1(String path) throws Exception {
        if (path.isEmpty()) {
            return null;
        }
        
        try (FileInputStream inputStream = new FileInputStream(path)) {
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance(SHA1);
            int numRead = inputStream.read(buffer);
            
            if (numRead > 0) {
                digest.update(buffer, 0, numRead);
            }
            
            byte[] sha1Bytes = digest.digest();
            return bytesToHex(sha1Bytes);
        }
    }

    public static String getMD5(String input) throws Exception {
        return bytesToHex(getMD5(input.getBytes()));
    }

    public static byte[] getMD5(byte[] source) throws Exception {
        MessageDigest md = MessageDigest.getInstance(MD5);
        md.update(source);
        return md.digest();
    }

    public static String getSHA256(String input) throws Exception {
        return bytesToHex(getSHA256(input.getBytes()));
    }

    public static byte[] getSHA256(byte[] source) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(SHA256);
        digest.update(source);
        return digest.digest();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
} 