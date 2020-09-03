## 背景
由于家庭网络的公网IP会周期变化（电信大约3天左右），为了稳定访问家庭中一些网络设备，需动态更新阿里云域名的IP指向记录，DDNS就是为了解决这一问题，传统的花生壳DDNS及其他DDNS产品同样可以解决这个问题，但是免费域名长度较长，购买的DDNS域名价格较贵。

在此项目之前实现过两种方式，一种基于server的web应用，通过发起curl get请求更新ip，一种PC client（java），将jar包注册到windows服务器中定时执行，这两种方式的缺点是需要依赖PC开机或者其他linux设备开机运行，家庭内PC及其他设备长期运行会造成资源浪费。

android手机更加适合ddns场景，一般家里都有几台废弃的android手机，连接wifi即可作为DDNS的基础设备。
  
  
  阿里云控制台RAM中获取accessKyeId和accessKeySecret，记得授权dns权限。并没有使用官方的java sdk，引入有冲突，用的http通用接口，手撸参数摘要加签，加签撸起来酸爽...有一点不一样就不行。
```
public class AliDnsUtil {

    private static String accessKeyId = "xxxxxx";
    private static String accessKeySecret = "xxxxxx";
```

首先查询阿里云账号下域名的recordId（需要在阿里云控制台将指定域名设置一个初始解析记录）

``` 
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
```

替换recordId为自己的记录id
```
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
```

效果图

![image](https://cdn.foxxx.top/ddns.jpg)
