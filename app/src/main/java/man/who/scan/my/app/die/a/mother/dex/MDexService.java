package man.who.scan.my.app.die.a.mother.dex;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import man.who.scan.my.app.die.a.mother.model.DexConfig;
import man.who.scan.my.app.die.a.mother.ui.base.ToastHandler;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

public class MDexService extends Service implements Runnable {

    volatile boolean isRunning = false;

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
            new Thread(this).start();
            ToastHandler.show(this, "服务已启动");
            isRunning = true;
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
