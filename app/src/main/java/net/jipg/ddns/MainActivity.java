package net.jipg.ddns;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jakewharton.threetenabp.AndroidThreeTen;

import net.jipg.ddns.utils.AliDnsUtil;
import net.jipg.ddns.utils.NetworkUtils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static String lastIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.textView);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        Timer timer = new Timer();
        timer.schedule(new DdnsTimerTask(), 0, 30 * 1000);

        // 查询域名recordId
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(AliDnsUtil.getDomainRecords("jipg.net"));
            }
        }).start();

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            TextView textView = findViewById(R.id.textView);
            if (textView.getLineCount() >= 200) {
                System.out.println("do clear");
                textView.setText("");
            }
            textView.append(msg.obj.toString());
            int scrollHeight = textView.getLayout().getLineTop(textView.getLineCount()) - textView.getHeight();
            if (scrollHeight > 0)
                textView.scrollTo(0, scrollHeight);
        }

        ;
    };

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
                System.out.println(result);
                sb.append("dns updated: " + ip);
                lastIp = ip;
            }
            sb.append("\n");
            Message message = Message.obtain(handler, 1, 2, 3, sb.toString());
            message.sendToTarget();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}