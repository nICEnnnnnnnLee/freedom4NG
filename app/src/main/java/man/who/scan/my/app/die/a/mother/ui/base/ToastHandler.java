package man.who.scan.my.app.die.a.mother.ui.base;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

public class ToastHandler extends Handler {

    final Activity activity;
    public ToastHandler(Activity activity){
        this.activity = activity;
    }
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        show(activity, msg.obj.toString());
    }

    public static void show(Context context, String msg){
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
