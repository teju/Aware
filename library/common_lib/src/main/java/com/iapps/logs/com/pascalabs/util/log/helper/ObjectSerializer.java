package com.iapps.logs.com.pascalabs.util.log.helper;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;


public class ObjectSerializer {

    //private static final Log log = LogFactory.getLog(ObjectSerializer.class);

    public static String serialize(Serializable obj) throws IOException {
        if (obj == null) return "";
        try {
            ByteArrayOutputStream serialObj = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(serialObj);
            objStream.writeUnshared(obj);
            objStream.close();
            String str = Base64.encodeToString(serialObj.toByteArray(), Base64.DEFAULT);
//            String str = String.valueOf(Base64Coder.encode(serialObj.toByteArray()));
            return str;
        } catch (Exception e) {
            e.printStackTrace();
            //throw WrappedIOException.wrap("Serialization error: " + e.getMessage(), e);
        }
        return null;
    }

    public static Object deserialize(String str) throws IOException {
        if (str == null || str.length() == 0) return null;
        try {
            byte[] bytes = Base64.decode(str, Base64.DEFAULT);
//            byte[] bytes = Base64Coder.decode(str);
            ByteArrayInputStream serialObj = new ByteArrayInputStream(bytes);
            ObjectInputStream objStream = new ObjectInputStream(serialObj);
            return objStream.readUnshared();
        } catch (Exception e) {
            e.printStackTrace();
            //throw WrappedIOException.wrap("Deserialization error: " + e.getMessage(), e);
        }
        return null;
    }

    public static String serializeWithLimitSize(Serializable obj, int limitSize) throws IOException {
        if (obj == null) return "";
        try {
            ByteArrayOutputStream serialObj = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(serialObj);
            objStream.writeUnshared(obj);
            objStream.close();
            String str = null;
            if(serialObj.toByteArray().length >= limitSize){
            }else {
                str = Base64.encodeToString(serialObj.toByteArray(), Base64.DEFAULT);
            }

//            str = String.valueOf(Base64Coder.encode(serialObj.toByteArray()));
            return str;
        } catch (Exception e) {
            e.printStackTrace();
            //throw WrappedIOException.wrap("Serialization error: " + e.getMessage(), e);
        }
        return null;
    }
    
//    public static String serialize(Serializable obj) throws IOException {
//        if (obj == null) return "";
//        try {
//            ByteArrayOutputStream serialObj = new ByteArrayOutputStream();
//            ObjectOutputStream objStream = new ObjectOutputStream(serialObj);
//            objStream.writeObject(obj);
//            objStream.close();
//            return encodeBytes(serialObj.toByteArray());
//        } catch (Exception e) {
//            e.printStackTrace();
//            //throw WrappedIOException.wrap("Serialization error: " + e.getMessage(), e);
//        }
//        return null;
//    }
//
//    public static Object deserialize(String str) throws IOException {
//        if (str == null || str.length() == 0) return null;
//        try {
//            ByteArrayInputStream serialObj = new ByteArrayInputStream(decodeBytes(str));
//            ObjectInputStream objStream = new ObjectInputStream(serialObj);
//            return objStream.readObject();
//        } catch (Exception e) {
//            e.printStackTrace();
//            //throw WrappedIOException.wrap("Deserialization error: " + e.getMessage(), e);
//        }
//        return null;
//    }
    
//    public static String encodeBytes(byte[] bytes) {
//        StringBuffer strBuf = new StringBuffer();
//
//        for (int i = 0; i < bytes.length; i++) {
//            strBuf.append((char) (((bytes[i] >> 4) & 0xF) + ((int) 'a')));
//            strBuf.append((char) (((bytes[i]) & 0xF) + ((int) 'a')));
//        }
//
//        return strBuf.toString();
//    }
//
//    public static byte[] decodeBytes(String str) {
//        byte[] bytes = new byte[str.length() / 2];
//        for (int i = 0; i < str.length(); i+=2) {
//            char c = str.charAt(i);
//            bytes[i/2] = (byte) ((c - 'a') << 4);
//            c = str.charAt(i+1);
//            bytes[i/2] += (c - 'a');
//        }
//        return bytes;
//    }

}