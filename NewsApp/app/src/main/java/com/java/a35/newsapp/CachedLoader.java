package com.java.a35.newsapp;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class CachedLoader {
    private MessageDigest messageDigest = null;
    private File cacheDir;
    private Map<String, String> memoryCachedData = new HashMap<>();

    public CachedLoader(Context context) {
        cacheDir = context.getCacheDir();
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static String byteArrayToHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private String getUuid(String targetUrl, String queryString,
                           Map<String, String> headerPayload) {
        StringBuilder sb = new StringBuilder();
        sb.append(targetUrl);
        sb.append(queryString);
        sb.append("\n");
        for (Iterator<Map.Entry<String, String>> it = headerPayload.entrySet().iterator();
             it.hasNext(); ) {
            Map.Entry<String, String> pair = it.next();
            sb.append(pair.getKey());
            sb.append(":");
            sb.append(pair.getValue());
            sb.append("\n");
        }
        return byteArrayToHexString(messageDigest.digest(sb.toString().getBytes()));
    }

    public String fetch(String targetURL, String queryString, Map<String, String> headerPayload,
                        boolean asString) throws IOException {
        if (queryString.length() > 0 && queryString.charAt(0) != '?') {
            queryString = "?" + queryString;
        }

        String uuid = getUuid(targetURL, queryString, headerPayload);

        if (asString && memoryCachedData.containsKey(uuid)){
            return memoryCachedData.get(uuid);
        }

        File cacheFile = new File(cacheDir, uuid);
        if (!cacheFile.exists()) {
            URL url = new URL(targetURL + queryString);
            URLConnection conn = url.openConnection();
            for (Iterator<Map.Entry<String, String>> it = headerPayload.entrySet().iterator();
                 it.hasNext(); ) {
                Map.Entry<String, String> pair = it.next();
                conn.addRequestProperty(pair.getKey(), pair.getValue());
            }
            BufferedInputStream ins = new BufferedInputStream(conn.getInputStream());
            BufferedOutputStream fos = new BufferedOutputStream(
                    new FileOutputStream(cacheFile), 4096);
            byte[] data = new byte[4096];
            int len;
            while ((len = ins.read(data, 0, data.length)) >= 0){
                fos.write(data, 0, len);
            }
            fos.flush();
            fos.close();
            ins.close();
        }

        if (!asString) {
            return cacheFile.getAbsolutePath();
        }

        Scanner scanner = new Scanner(
                new BufferedReader(new FileReader(cacheFile.getAbsolutePath())));
        scanner.useDelimiter("\\Z"); // FIXME(twd2): strange code
        String data = scanner.next();
        return data;
    }

}
