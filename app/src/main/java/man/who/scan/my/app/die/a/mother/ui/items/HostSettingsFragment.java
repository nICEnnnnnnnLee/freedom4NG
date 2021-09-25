package man.who.scan.my.app.die.a.mother.ui.items;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import man.who.scan.my.app.die.a.mother.Config;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.ui.FragmentActivity;
import man.who.scan.my.app.die.a.mother.ui.base.BaseFragment;

public class HostSettingsFragment extends BaseFragment implements View.OnClickListener {

//    ImageView iv_save, iv_reload;
    EditText textHost;
//    FragmentActivity activity;
//    View view;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (FragmentActivity) getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.host_settings, container, false);
        TextView title = activity.findViewById(R.id.tv_title);
        title.setText(R.string.host_settings_title);
        textHost = view.findViewById(R.id.hostConfig);
        reloadFromFile();
        iv_save.setVisibility(View.VISIBLE);
        iv_reload.setVisibility(View.VISIBLE);
        iv_save.setOnClickListener(this);
        iv_reload.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        String tips = resources.getString(R.string.tips_undealt_click);
        if(Global.isRun){
            tips = resources.getString(R.string.tips_vpn_not_shut_down);
        }else if (v == iv_save) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(Global.HOST_FILE))) {
                writer.write(textHost.getText().toString());
                tips = resources.getString(R.string.tips_host_setings_saved);
            } catch (Exception e) {
                e.printStackTrace();
                tips = resources.getString(R.string.tips_host_save_not_ok);
            }
        } else if (v == iv_reload) {
            tips = reloadFromFile();
        }
        Config.fromHostFile(Global.hostConfig, Global.HOST_FILE);
//        System.out.println("接下来打印host: ");
//        for(Map.Entry entry: Global.hostConfig.entrySet()){
//            System.out.printf("%s -> %s\n", entry.getKey(), entry.getValue());
//        }
//        Toast toast = Toast.makeText(activity, tips, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
//        toast.show();
        toast(tips);
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
            return resources.getString(R.string.tips_host_load_ok);
        } catch (Exception e) {
            return resources.getString(R.string.tips_host_load_not_ok);
        }
    }
}
