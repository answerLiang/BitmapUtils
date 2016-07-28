package com.lda.lrucache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by answer on 2014/7/2 0002.
 */
public class MD5Utils {


    public static String MD5Encryption(String msg, String salt){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String code;
        if(salt != null){
            code =  msg+salt;
        }else{
            code = msg;
        }
       byte[] b = digest.digest(code.getBytes());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            int value = b[i]&0xff;
            if(value <16){
                builder.append("0");
            }
            builder.append(Integer.toHexString(value));
        }
        return builder.toString();
    }
}
