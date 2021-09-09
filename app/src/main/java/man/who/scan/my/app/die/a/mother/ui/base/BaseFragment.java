package man.who.scan.my.app.die.a.mother.ui.base;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.ui.FragmetActivity;

public class BaseFragment extends Fragment {

    public final static int SELECT_FOLDER = 1;
    public final static int SELECT_FILE = 2;
    protected ImageView iv_add, iv_more, iv_delete, iv_save, iv_reload;
    protected TextView tv_title;
    protected FragmetActivity activity;
    protected Resources resources;
    public View view;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (FragmetActivity) getActivity();
        tv_title = activity.findViewById(R.id.tv_title);
        iv_more = activity.findViewById(R.id.iv_more);
        iv_add = activity.findViewById(R.id.iv_add);
        iv_delete = activity.findViewById(R.id.iv_delete);
        iv_save = activity.findViewById(R.id.iv_save);
        iv_reload = activity.findViewById(R.id.iv_reload);
        resources = this.getResources();
    }

    public void toast(String msg) {
        ToastHandler.show(activity, msg);
    }
}
