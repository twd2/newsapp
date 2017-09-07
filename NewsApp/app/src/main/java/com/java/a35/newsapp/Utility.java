package com.java.a35.newsapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by wuhaozhe on 2017/9/5.
 */

public class Utility {

    private static final int BUFFER_SIZE = 4096;

    public static String readAllString(InputStream stream) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream);
        char[] buffer = new char[BUFFER_SIZE];
        StringBuilder sb = new StringBuilder();
        int count;
        while ((count = reader.read(buffer)) >= 0) {
            sb.append(buffer, 0, count);
        }
        return sb.toString();
    }

    public static String byteArrayToHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
