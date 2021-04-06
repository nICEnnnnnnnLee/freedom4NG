package man.who.scan.my.app.die.a.mother.ui.items;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.ui.FileChooserActivity;

public class FileFragment extends Fragment implements View.OnClickListener {

    int type;
    String display;
    String file;
    FileChooserActivity activity;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        type = args.getInt("type");
        display = args.getString("display");
        file = args.getString("file");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (FileChooserActivity) getActivity();
        View view = inflater.inflate(R.layout.base_folder, container, false);
        TextView texvView = view.findViewById(R.id.tv_folder);
        texvView.setText(display);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        switch (type) {
            case 0:
                texvView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
                texvView.setHeight(50);
                params.setMargins(5, 15, 0, 0);
                texvView.setLayoutParams(params);
                view.findViewById(R.id.tv_divider).setVisibility(View.GONE);
                break;
            case 1:
                texvView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                texvView.setHeight(50);
                params.setMargins(15, 15, 0, 0);
                texvView.setLayoutParams(params);
                view.findViewById(R.id.tv_divider).setVisibility(View.GONE);
                texvView.setOnClickListener(this);
                break;
            case 3:
                texvView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            case 2:
                texvView.setOnClickListener(this);
                break;
            default:
                break;
        }
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (type) {
            case 1:
            case 2:
                File folder = new File(file);
                if (folder.listFiles() == null) {
                    activity.toast("该文件夹不存在或者没有访问权限");
                } else {
                    activity.showFolder(folder, true);
                }
                break;
            case 3:
                if("file".equals(activity.typeStr)){
                    activity.currentPath = file;
                    activity.onClick(v);
                }else {
                    activity.toast("当前只能返回文件夹类型！！");
                }
                break;
            default:
                break;
        }
    }
}
