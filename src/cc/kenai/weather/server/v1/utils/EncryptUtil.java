package cc.kenai.weather.server.v1.utils;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptUtil {
    private static final String UTF8 = "utf-8";

    /**
     * MD5数字签名
     *
     * @param src
     * @return
     * @throws Exception
     */
    public String md5Digest(String src) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // 定义数字签名方法, 可用：MD5, SHA-1
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] b = md.digest(src.getBytes(UTF8));
        return this.byte2HexStr(b);
    }
    public String sh1Digest(String src) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // 定义数字签名方法, 可用：MD5, SHA-1
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] b = md.digest(src.getBytes(UTF8));
        return this.byte2HexStr(b);
    }


    /**
     * 字节数组转化为大写16进制字符串
     *
     * @param b
     * @return
     */
    private String byte2HexStr(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String s = Integer.toHexString(b[i] & 0xFF);
            if (s.length() == 1) {
                sb.append("0");
            }
            sb.append(s.toUpperCase());
        }
        return sb.toString();
    }
}
