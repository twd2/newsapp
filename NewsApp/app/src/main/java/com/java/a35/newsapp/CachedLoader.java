package com.java.a35.newsapp;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class CachedLoader {
    private Context context;
    private MessageDigest messageDigest = null;
    private File cacheDir;
    private Map<String, String> memoryCachedData = new HashMap<>();
    private static final int BUFFER_SIZE = 4096;

    public CachedLoader(Context context) {
        this.context = context;
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

    private String getUuid(String targetUrl, String queryString,
                           Map<String, String> headerPayload) {
        StringBuilder sb = new StringBuilder();
        sb.append(targetUrl);
        sb.append(queryString);
        sb.append("\n");
        for (Map.Entry<String, String> entry : headerPayload.entrySet()) {
            sb.append(entry.getKey());
            sb.append(": ");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        return Utils.byteArrayToHexString(messageDigest.digest(sb.toString().getBytes()));
    }

    public String fetch(String targetURL, String queryString, Map<String, String> headerPayload,
                        boolean asJsonString) throws IOException {
        if (queryString.length() > 0 && queryString.charAt(0) != '?') {
            queryString = "?" + queryString;
        }

        String uuid = getUuid(targetURL, queryString, headerPayload);

        if (asJsonString && memoryCachedData.containsKey(uuid)){
            return memoryCachedData.get(uuid);
        }

        File cacheFile = new File(cacheDir, uuid);
        if (!cacheFile.exists()) {
            try {
                URL url = new URL(targetURL + queryString);
                URLConnection conn = url.openConnection();
                for (Map.Entry<String, String> entry : headerPayload.entrySet()) {
                    conn.addRequestProperty(entry.getKey(), entry.getValue());
                }
                BufferedInputStream ins = new BufferedInputStream(conn.getInputStream());
                BufferedOutputStream fos = new BufferedOutputStream(
                        new FileOutputStream(cacheFile), BUFFER_SIZE);
                byte[] data = new byte[BUFFER_SIZE];
                int len;
                while ((len = ins.read(data, 0, data.length)) >= 0){
                    fos.write(data, 0, len);
                }
                fos.flush();
                fos.close();
                ins.close();
            } catch (IOException e) {
                cacheFile.delete();
                throw e;
            }
        }

        if (!asJsonString) {
            return cacheFile.getAbsolutePath();
        }

        FileInputStream fin = new FileInputStream(cacheFile);
        String data = Utils.readAllString(fin);
        fin.close();
        memoryCachedData.put(uuid, data);

        try {
            new JSONObject(data); // try parse
        } catch (JSONException e) {
            e.printStackTrace();
            cacheFile.delete();
            memoryCachedData.remove(uuid);
        }

        return data;
    }

}
