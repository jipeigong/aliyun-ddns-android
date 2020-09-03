package net.jipg.ddns;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jakewharton.threetenabp.AndroidThreeTen;

import net.jipg.ddns.utils.AliDnsUtil;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static TextView textView;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

        context = getApplicationContext();

        //启动Service
        startService(new Intent(this, DdnsService.class));

        //查询域名recordId
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, AliDnsUtil.getDomainRecords("jipg.net"));
            }
        }).start();

    }

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            String log = LogCacheUtil.getLog(context);

            if (log.length() >= 4000) {
                Log.d(TAG, "do clear");
                LogCacheUtil.clearLog(context);
            }
            LogCacheUtil.addLog(context, msg.obj.toString());
            textView.setText(LogCacheUtil.getLog(context));
            int scrollHeight = textView.getLayout().getLineTop(textView.getLineCount()) - textView.getHeight();
            if (scrollHeight > 0) {
                textView.scrollTo(0, scrollHeight);
            }
        }
    };


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