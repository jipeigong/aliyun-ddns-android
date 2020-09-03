package net.jipg.ddns;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * <p>ClassName:LogCacheUtil</p>
 * <p>Description: 日志文件信息缓存</p>
 *
 * @author JiPeigong
 * @date 2020 -09-01 11:11:29
 */
public class LogCacheUtil {
    public static final String docCache = "log.txt";

    /**
     * 设置缓存
     * content是要存储的内容，可以是任意格式的，不一定是字符串。
     */
    public static void addLog(Context context, String content) {
        FileOutputStream fos = null;
        try {
            //打开文件输出流，接收参数是文件名和模式
            fos = context.openFileOutput(docCache, Context.MODE_APPEND);
            fos.write(content.getBytes());
//            fos.write("\n".getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void clearLog(Context context) {
        FileOutputStream fos = null;
        try {
            //打开文件输出流，接收参数是文件名和模式
            fos = context.openFileOutput(docCache, Context.MODE_PRIVATE);
            fos.write("".getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getLog(Context context) {
        FileInputStream fis = null;
        StringBuffer sBuf = new StringBuffer();
        try {
            fis = context.openFileInput(docCache);
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = fis.read(buf)) != -1) {
                sBuf.append(new String(buf, 0, len));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (sBuf != null) {
            return sBuf.toString();
        }
        return null;
    }

    public static String getCachePath(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }
}
