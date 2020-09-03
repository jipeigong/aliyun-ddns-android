package net.jipg.ddns.utils;

import android.util.Base64;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * <p>ClassName:AliDnsUtils</p>
 * <p>Description: 阿里云dns操作工具类</p>
 *
 * @author JiPeigong
 * @date 2020 -09-01 11:11:29
 */
public class AliDnsUtil {

    private static String accessKeyId = "xxxxxx";
    private static String accessKeySecret = "xxxxxx";

    public static final String UTF_8 = "UTF-8";
    public static final String ALIYUN_DNS_HOST = "http://alidns.aliyuncs.com/";

    private static String YYYY_MM_DD = "yyyy-MM-dd";
    private static String HH_MM_SS = "HH:mm:ss";
    private static String ZONE_GMT = "GMT";
    private static String DATETIME_SPLIT_T = "T";
    private static String DATETIME_SUFFIX_Z = "Z";

    // 特殊字符正则
    private static final Pattern pattern = Pattern.compile("[`!@#$%^&*()\\+={}':;,\\[\\]<>/?￥%…（）+|【】‘；：”“’。，、？\\s]");

    /**
     * 更新解析记录
     *
     * @param recordId the record id
     * @param type     the type
     * @param value    the value
     * @param rr       the rr
     */
    public static String updateResolveRecord(String recordId, String type, String value, String rr) {
        Map<String, String> params = new HashMap<>();
        params.put("Action", "UpdateDomainRecord");
        params.put("RR", rr);
        params.put("RecordId", recordId);
        params.put("Type", type);
        params.put("Value", value);
        return doHttpGet(signUrlParams(params));
    }

    /**
     * Gets domain records.
     *
     * @param rootDomain the root domain
     * @return the domain records
     */
    public static String getDomainRecords(String rootDomain) {
        Map<String, String> params = new HashMap<>();
        params.put("Action", "DescribeSubDomainRecords");
        params.put("SubDomain", rootDomain);
        return doHttpGet(signUrlParams(params));
    }

    /**
     * okhttp get请求
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String doHttpGet(String url) {
        String result = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 组装签名url参数
     *
     * @return
     */
    public static String signUrlParams(Map<String, String> params) {
        String url = null;
        try {
            // 公共参数
            assembleCommonParams(params);
            String urlStr = null;

            urlStr = assembleUrlParams(params);
            String StringToSign = "GET" + "&" + URLEncoder.encode("/", UTF_8) + "&" + URLEncoder.encode(urlStr, UTF_8);
            String signature = sign(accessKeySecret + "&", StringToSign, "HmacSHA1");
            params.put("Signature", signature);
            url = ALIYUN_DNS_HOST + "?" + assembleUrlParams(params);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * 组装公共参数
     *
     * @param params
     */
    private static void assembleCommonParams(Map<String, String> params) {
        params.put("Format", "json");
        params.put("Version", "2015-01-09");
        params.put("AccessKeyId", accessKeyId);
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("Timestamp", formatLocalDateTime(LocalDateTime.now()));
        params.put("SignatureVersion", "1.0");
        params.put("SignatureNonce", String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 参数组装为url字符串
     *
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String assembleUrlParams(Map<String, String> params) throws UnsupportedEncodingException {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            Matcher matcher = pattern.matcher(value);
            while (matcher.find()) {
                value = value.replace(matcher.group(), URLEncoder.encode(matcher.group(), UTF_8));
            }
            sb.append(key).append("=").append(value).append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }


    /**
     * 摘要签名
     *
     * @param keySecret
     * @param algorithm
     * @return
     */
    private static String sign(String keySecret, String signString, String algorithm) {
        String signature;
        try {
            Mac hmacSha256 = Mac.getInstance(algorithm);
            byte[] keyBytes = keySecret.getBytes(Charset.forName(UTF_8));
            hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, algorithm));
            //对字符串进行hmacSha256加密，然后再进行BASE64编码
            byte[] signResult = hmacSha256.doFinal(signString.getBytes(Charset.forName(UTF_8)));
            byte[] base64Bytes = Base64.encode(signResult, Base64.DEFAULT);
            signature = new String(base64Bytes, Charset.forName(UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return signature.replace("\n", "");
    }

    /**
     * 日期转换
     *
     * @param localDateTime
     * @return
     */
    private static String formatLocalDateTime(LocalDateTime localDateTime) {
        ZonedDateTime zonedDateTime = localDateTime.atZone((ZoneId.systemDefault()));
        Date date = DateTimeUtils.toDate(zonedDateTime.toInstant());
        StringBuilder dateStrBuilder = new StringBuilder();
        SimpleDateFormat dateSdf = new SimpleDateFormat(YYYY_MM_DD);
        SimpleDateFormat timeSdf = new SimpleDateFormat(HH_MM_SS);
        TimeZone gmtTimeZone = TimeZone.getTimeZone(ZONE_GMT);
        dateSdf.setTimeZone(gmtTimeZone);
        timeSdf.setTimeZone(gmtTimeZone);
        dateStrBuilder.append(dateSdf.format(date)).append(DATETIME_SPLIT_T).append(timeSdf.format(date)).append(DATETIME_SUFFIX_Z);
        return dateStrBuilder.toString();
    }
}
