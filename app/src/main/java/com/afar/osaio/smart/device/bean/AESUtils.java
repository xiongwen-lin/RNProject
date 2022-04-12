package com.afar.osaio.smart.device.bean;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    /**
     * AES加密
     *
     * @param data
     *            将要加密的内容
     * @param key
     *            密钥
     * @return 已经加密的内容
     */
    public static byte[] encrypt(byte[] data, byte[] key) {
        //不足16字节，补齐内容为差值
        int len = 16 - data.length % 16;
        for (int i = 0; i < len; i++) {
            byte[] bytes = { (byte) len };
            data = concat(data, bytes);
        }
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[] {};
    }

    /**
     * AES解密
     *
     * @param data
     *            将要解密的内容
     * @param key
     *            密钥
     * @return 已经解密的内容
     */
    public static byte[] decrypt(byte[] data, byte[] key) {
        data = noPadding(data, -1);
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] decryptData = cipher.doFinal(data);
            int len = 2 + byteToInt(decryptData[4]) + 3;
            return noPadding(decryptData, len);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[] {};
    }

    private static int byteToInt(byte b) {
        return (b) & 0xff;
    }

    /**
     * 合并数组
     *
     * @param firstArray
     *            第一个数组
     * @param secondArray
     *            第二个数组
     * @return 合并后的数组
     */
    private static byte[] concat(byte[] firstArray, byte[] secondArray) {
        if (firstArray == null || secondArray == null) {
            return null;
        }
        byte[] bytes = new byte[firstArray.length + secondArray.length];
        System.arraycopy(firstArray, 0, bytes, 0, firstArray.length);
        System.arraycopy(secondArray, 0, bytes, firstArray.length,
                secondArray.length);
        return bytes;
    }

    /**
     * 去除数组中的补齐
     *
     * @param paddingBytes
     *            源数组
     * @param dataLength
     *            去除补齐后的数据长度
     * @return 去除补齐后的数组
     */
    private static byte[] noPadding(byte[] paddingBytes, int dataLength) {
        if (paddingBytes == null) {
            return null;
        }

        byte[] noPaddingBytes = null;
        if (dataLength > 0) {
            if (paddingBytes.length > dataLength) {
                noPaddingBytes = new byte[dataLength];
                System.arraycopy(paddingBytes, 0, noPaddingBytes, 0, dataLength);
            } else {
                noPaddingBytes = paddingBytes;
            }
        } else {
            int index = paddingIndex(paddingBytes);
            if (index > 0) {
                noPaddingBytes = new byte[index];
                System.arraycopy(paddingBytes, 0, noPaddingBytes, 0, index);
            }
        }

        return noPaddingBytes;
    }

    /**
     * 获取补齐的位置
     *
     * @param paddingBytes
     *            源数组
     * @return 补齐的位置
     */
    private static int paddingIndex(byte[] paddingBytes) {
        for (int i = paddingBytes.length - 1; i >= 0; i--) {
            if (paddingBytes[i] != 0) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * 32位MD5加密
     * @param content -- 待加密内容
     * @return
     */
    public static String md5Decode32(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException",e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }
        //对生成的16字节数组进行补零操作
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10){
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    /**
     *
     * @param stringMsg  uid+uuid+timestamp+param
     * @param secret  device_secret
     * @return
     */
    public static String signBASE64(String stringMsg, String secret) {
        /*String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            hash = Base64.encodeBase64String(sha256_HMAC.doFinal(stringMsg.getBytes()));
            //hash = URLEncoder.encode(hash);
            System.out.println(hash);
        }
        catch (Exception e){
            System.out.println("Error");
        }
        return hash;*/

        byte[] keyBytes = secret.getBytes();
        byte[] plainBytes = stringMsg.getBytes();

        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(keyBytes, "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hashs = sha256_HMAC.doFinal(plainBytes);
            StringBuilder sb = new StringBuilder();
            byte[] var8 = hashs;
            int var9 = hashs.length;

            for(int var10 = 0; var10 < var9; ++var10) {
                byte x = var8[var10];
                String b = Integer.toHexString(x & 255);
                if (b.length() == 1) {
                    b = '0' + b;
                }

                sb.append(b);
            }

            //Base64.encodeToString(sb.toString().getBytes(), 2);
            return Base64.encodeToString(sb.toString().getBytes(), 2);
        } catch (Exception var13) {
            var13.printStackTrace();
            return new String();
        }


    }
}
