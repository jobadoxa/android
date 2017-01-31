package com.telpo.tps550.api.demo.util;

public class OtherUtil {

    public static String byteToHexString( byte[] b,int length) {
        String a = "";
        for (int i = 0; i < length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            a = a+hex;
        }
        return a;
    }
}
