package man.who.scan.my.app.die.a.mother.ui.items;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

import man.who.scan.my.app.die.a.mother.Config;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.ui.FragmetActivity;

public class HostSettingsFragment extends Fragment implements View.OnClickListener {

    ImageView imgSave, imgReload;
    EditText textHost;
    FragmetActivity activity;
    View view;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (FragmetActivity) getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.host_settings, container, false);
        TextView title = activity.findViewById(R.id.tv_title);
        title.setText("Host 设置");
        textHost = view.findViewById(R.id.hostConfig);
        reloadFromFile();
        imgSave = activity.findViewById(R.id.iv_save);
        imgReload = activity.findViewById(R.id.iv_reload);
        imgSave.setVisibility(View.VISIBLE);
        imgReload.setVisibility(View.VISIBLE);
        imgSave.setOnClickListener(this);
        imgReload.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        String tips = "未处理的点击";
        if(Global.isRun){
            tips = "VPN 尚未关闭";
        }else if (v == imgSave) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(Global.HOST_FILE))) {
                writer.write(textHost.getText().toString());
                tips = "Host 文件已保存";
            } catch (Exception e) {
                e.printStackTrace();
                tips = "Host 保存失败";
            }
        } else if (v == imgReload) {
            tips = reloadFromFile();
        }
        Config.fromHostFile(Global.hostConfig, Global.HOST_FILE);
//        System.out.println("接下来打印host: ");
//        for(Map.Entry entry: Global.hostConfig.entrySet()){
//            System.out.printf("%s -> %s\n", entry.getKey(), entry.getValue());
//        }
        Toast toast = Toast.makeText(activity, tips, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public String reloadFromFile(){
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(Global.HOST_FILE))) {
            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                line = reader.readLine();
            }
            textHost.setText(sb.toString());
            return "Host 加载成功";
        } catch (Exception e) {
            return "Host 读取失败";
        }
    }
    @Override
    public void onPause() {
        super.onPause();
    }
}
