package com.java.a35.newsapp.dummy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by wuhaozhe on 2017/9/5.
 */

public class Utility {                     //some methods used in API nad PictureAPI
    public static String readAllString(InputStream stream) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream);
        char[] buffer = new char[4096];
        StringBuffer sb = new StringBuffer();
        int count;
        while ((count = reader.read(buffer)) >= 0) {
            sb.append(buffer, 0, count);
        }
        return sb.toString();
    }
}
