package com.java.a35.newsapp;

import android.content.Context;
import android.provider.MediaStore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class CachedLoader {
    private MessageDigest messageDigest = null;
    private File cacheDir;
    private Map<String, String> cachedData = new HashMap<String, String>();
    public CachedLoader(Context context){
        cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    private static String byteArrayToHexString(byte[] b) {
        String result = "";
        for (byte aB : b) {
            result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
    private String getUuid(String targetURL, String queryString, Map<String, String> headerPayload){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(targetURL);
        stringBuilder.append(queryString);
        stringBuilder.append("\n");
        for (Iterator it = headerPayload.entrySet().iterator(); it.hasNext(); ){
            Map.Entry pair = (Map.Entry)it.next();
            stringBuilder.append(pair.getKey());
            stringBuilder.append(":");
            stringBuilder.append(pair.getValue());
            stringBuilder.append("\n");
        }
        return byteArrayToHexString(messageDigest.digest(stringBuilder.toString().getBytes()));
    }
    public String fetch(String targetURL, String queryString, Map<String, String> headerPayload, boolean asString) throws IOException{
        String uuid = getUuid(targetURL, queryString, headerPayload);
        if (asString && cachedData.containsKey(uuid)){
            return cachedData.get(uuid);
        }
        File cacheFile = new File(cacheDir, uuid);
        //if (!cacheFile.exists())
        {
            URL url = new URL(targetURL + queryString);
            URLConnection conn = url.openConnection();
            for (Iterator it = headerPayload.entrySet().iterator(); it.hasNext(); ){
                Map.Entry pair = (Map.Entry)it.next();
                conn.addRequestProperty((String)pair.getKey(), (String)pair.getValue());
            }
            BufferedInputStream ins = new BufferedInputStream(conn.getInputStream());
            BufferedOutputStream fos = new BufferedOutputStream(
                    new FileOutputStream(cacheDir + "/" + uuid), 1024);
            byte[] data = new byte[1024];
            int x = 0;
            while ((x = ins.read(data, 0, 1024)) >= 0){
                fos.write(data, 0, x);
            }
            fos.flush();
            fos.close();
            ins.close();
        }
        if (!asString)
            return cacheDir + "/" + uuid;
        Scanner scanner = new Scanner(new BufferedReader(new FileReader(cacheDir + "/" + uuid)));
        scanner.useDelimiter("\\Z");
        String data = scanner.next();
        return data;
    }

    public String fetch(String targetURL, Map<String, String> getPayload, Map<String, String> headerPayload, boolean asString) throws IOException{
        StringBuilder payloadBuilder = new StringBuilder();
        for (Iterator it = getPayload.entrySet().iterator(); it.hasNext(); ){
            Map.Entry pair = (Map.Entry)it.next();
            payloadBuilder.append(URLEncoder.encode((String)pair.getKey()));
            payloadBuilder.append(":");
            payloadBuilder.append(URLEncoder.encode((String)pair.getValue()));
            payloadBuilder.append("\n");
        }
        return fetch(targetURL, "?" + payloadBuilder.toString().substring(1), headerPayload, asString);
        //URL url = new URL(targetURL);
    }

}
