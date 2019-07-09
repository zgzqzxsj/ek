package com.zm.ek;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2019/6/22 0022.
 */

public class getVersion {




    public static String getURL(String url, String code, int outtime,int number) {
        String result = "";
        for (int i = 0; i < number; i++) {
            try {
                URL U = new URL(url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) U.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");
//				httpURLConnection.setRequestProperty("Content-Type", "text/plain");
                httpURLConnection.setConnectTimeout(outtime);
                httpURLConnection.setReadTimeout(outtime);
                httpURLConnection.connect();
//				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream()));
//				out.flush();
//				out.close();

                BufferedReader in = new BufferedReader(new InputStreamReader((InputStream) httpURLConnection.getInputStream(), code));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
                httpURLConnection.disconnect();
                result = sb.toString();
                break;
            } catch (Exception e) {
                result = "TimeOut";
                System.out.println("[ERROR getURL]   " + url);
                e.printStackTrace();
            }
        }
        return result;
    }
}
