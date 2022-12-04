package man.who.scan.my.app.die.a.mother.dex;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.PowerManager;

import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.model.DexConfig;
import man.who.scan.my.app.die.a.mother.ui.FragmentActivity;
import man.who.scan.my.app.die.a.mother.ui.base.ToastHandler;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

public class MDexService extends Service implements Runnable {

    private static final int NotificationID = 0x1315;
    private static final String NotificationTAG = "freedom";

    volatile boolean isRunning = false;
    PowerManager.WakeLock wakeLock;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (DexConfig.stopService == null) {
            ToastHandler.show(this, "请先测试入口类！");
        } else if (isRunning) {
            makeText(this, "", LENGTH_LONG).show();
            ToastHandler.show(this, "已经存在该服务！");
        } else {
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, MDexService.class.getName());
            wakeLock.acquire();

            Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(this.getPackageName(), NotificationTAG, NotificationManager.IMPORTANCE_HIGH);;
                channel.enableLights(true);
                channel.setShowBadge(true);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.createNotificationChannel(channel);
                builder.setChannelId(this.getPackageName());
            }
            Intent nfIntent = new Intent(this, FragmentActivity.class);
            builder.setContentIntent(PendingIntent.
                    getActivity(this, 0, nfIntent, PendingIntent.FLAG_IMMUTABLE)) // 设置PendingIntent
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                            R.drawable.ic_launcher)) // 设置下拉列表中的图标(大图标)
                    .setContentTitle("运行自定义Dex服务") // 设置下拉列表里的标题
                    .setSmallIcon(R.drawable.ic_launcher) // 设置状态栏内的小图标
                    .setContentText("Running...") // 设置上下文内容
                    .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
            Notification notification = builder.build(); // 获取构建好的Notification
            // 参数一：唯一的通知标识；参数二：通知消息。
            startForeground(110, notification);// 开始前台服务

            new Thread(this).start();
            ToastHandler.show(this, "服务已启动");
            isRunning = true;
            return START_NOT_STICKY;
        }

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        if(wakeLock != null){
            wakeLock.release();
            wakeLock=null;
        }
        try {
            DexConfig.stopService.invoke(null);
            isRunning = false;
            DexConfig.startService = null;
            DexConfig.stopService = null;
            ToastHandler.show(this, "服务已停止");
        } catch (Exception e) {
            ToastHandler.show(this, e.toString());
        }
    }

    @Override
    public void run() {
        try {
            DexConfig.startService.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
