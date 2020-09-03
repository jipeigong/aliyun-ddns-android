package net.jipg.ddns;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;

import net.jipg.ddns.utils.AliDnsUtil;
import net.jipg.ddns.utils.NetworkUtils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>ClassName:DdnsService</p>
 * <p>Description: 息屏运行服务</p>
 *
 * @author JiPeigong
 * @date 2020 -09-01 11:11:29
 */
public class DdnsService extends Service {

    private static final String TAG = DdnsService.class.getSimpleName();
    private static String lastIp;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        Timer timer = new Timer();
        timer.schedule(new DdnsTimerTask(), 0, 30 * 1000);
    }

    class DdnsTimerTask extends TimerTask {
        public void run() {
            StringBuilder sb = new StringBuilder();
            String dateStr = (String) DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date());
            sb.append(dateStr).append(" ");
            String ip = NetworkUtils.getV4Ip();
            if (lastIp != null && lastIp.equals(ip)) {
                sb.append("No need to update");
            } else {
                String result = AliDnsUtil.updateResolveRecord("20322943158194176", "A", ip, "@");
                Log.d(TAG,result);
                sb.append("dns updated: " + ip);
                lastIp = ip;
            }
            sb.append("\n");
            Message message = Message.obtain(MainActivity.handler, 1, 2, 3, sb.toString());
            message.sendToTarget();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        // 在API11之后构建Notification的方式
        Notification.Builder builder = new Notification.Builder
                (this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class);

        builder.setContentIntent(PendingIntent.
                getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
//                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_large)) // 设置下拉列表中的图标(大图标)
                .setContentTitle("下拉列表中的Title") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("要显示的内容") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        startForeground(110, notification);// 开始前台服务
        return Service.START_STICKY_COMPATIBILITY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        super.onDestroy();
    }
}
