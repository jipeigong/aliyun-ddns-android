package net.jipg.ddns.utils;

import android.util.Log;

import net.jipg.ddns.DdnsService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>ClassName:NetworkUtils</p>
 * <p>Description:网络信息获取工具类</p>
 * @author JiPeigong
 * @date 2020-09-01 11:16:07
 **/
public class NetworkUtils {
    private static final String TAG = DdnsService.class.getSimpleName();


    private static final int TIME_OUT_MILLS = 3000;
    private static final String PATTERN = "\\<dd class\\=\"fz24\">(.*?)\\<\\/dd>";
    private static Pattern pattern = Pattern.compile(PATTERN);


    public static void main(String[] args) {
        Log.d(TAG, getV4Ip());
    }

    public static boolean ping(String ipAddress) throws Exception {
        // 当返回值是true时，说明host是可用的，false则不可。
        return InetAddress.getByName(ipAddress).isReachable(TIME_OUT_MILLS);
    }

    /**
     * 获取本机的外网ip地址
     *
     * @return IP
     */
    public static String getV4Ip() {
        String ip = "";
        String chinaz = "http://ip.chinaz.com";
        StringBuilder inputLine = new StringBuilder();
        String read = "";
        URL url = null;
        HttpURLConnection urlConnection = null;
        BufferedReader in = null;
        try {
            url = new URL(chinaz);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.forName("UTF-8")));
            while ((read = in.readLine()) != null) {
                inputLine.append(read).append("\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Matcher m = pattern.matcher(inputLine.toString());
        if (m.find()) {
            ip = m.group(1);
        }
        return ip;
    }

}
