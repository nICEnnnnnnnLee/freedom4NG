package man.who.scan.my.app.die.a.mother.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import man.who.scan.my.app.die.a.mother.R;

public class BaseActivity extends Activity {

    protected ImageView iv_add, iv_more, iv_delete, iv_save, iv_reload;
    protected TextView tv_title;
    protected LayoutInflater layoutInflater;

    boolean isLoadDefault = true;

    protected void disableDefaultLoad(){
        isLoadDefault = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutInflater = LayoutInflater.from(this);
        if(isLoadDefault){
            setContentView(R.layout.base);
            onLayoutLoded();
        }
    }

    protected void onLayoutLoded() {
        tv_title = findViewById(R.id.tv_title);
        iv_more = findViewById(R.id.iv_more);
        iv_add = findViewById(R.id.iv_add);
        iv_delete = findViewById(R.id.iv_delete);
        iv_save = findViewById(R.id.iv_save);
        iv_reload = findViewById(R.id.iv_reload);
    }

    public void toast(String msg) {
        ToastHandler.show(BaseActivity.this, msg);
    }

    public static void setVisible(View[] views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void setOnClickListener(View[] views, View.OnClickListener listener) {
        for (View view : views) {
            view.setOnClickListener(listener);
        }
    }

}
