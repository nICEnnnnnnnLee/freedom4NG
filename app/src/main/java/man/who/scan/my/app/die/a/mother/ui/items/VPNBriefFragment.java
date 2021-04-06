package man.who.scan.my.app.die.a.mother.ui.items;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import man.who.scan.my.app.die.a.mother.Config;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.model.BaseConfig;
import man.who.scan.my.app.die.a.mother.model.VPNConfig;
import man.who.scan.my.app.die.a.mother.ui.FragmetActivity;
import man.who.scan.my.app.die.a.mother.ui.MultiFragmetActivity;
import man.who.scan.my.app.die.a.mother.ui.base.ToastHandler;
import man.who.scan.my.app.die.a.mother.vpn.util.TestUtil;

public class VPNBriefFragment extends Fragment implements View.OnClickListener {

    public ImageView imgEdit, imgDelete, imgSelect, imgTest;
    public VPNConfig config;
    private Handler toastHandler;
    TextView addr, remark;
    MultiFragmetActivity activity;
    View view;
    String configPath, configName;
    int index;
    boolean isLast;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        configPath = args.getString("configPath");
        configName = args.getString("configName");
        index = args.getInt("index");
        config = new VPNConfig();
        config.fromMap(Config.fromFile(configPath));
        isLast = args.getBoolean("isLast");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MultiFragmetActivity) getActivity();
        toastHandler = new ToastHandler(activity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.vpn_brief, container, false);
        addr = view.findViewById(R.id.tv_addr);
        remark = view.findViewById(R.id.tv_remark);
        imgEdit = view.findViewById(R.id.iv_edit);
        imgDelete = view.findViewById(R.id.iv_delete);
        imgSelect = view.findViewById(R.id.iv_selected);
        imgTest = view.findViewById(R.id.iv_test);
        imgEdit.setOnClickListener(this);
        imgDelete.setOnClickListener(this);
        imgSelect.setOnClickListener(this);
        imgTest.setOnClickListener(this);
        addr.setText(String.format("%s:%s", config.remoteHost, config.remotePort));
        remark.setText(configName);
        if (config.remark != null && config.remark.equals(Global.currentVPNConfigRemark))
            imgSelect.setImageResource(android.R.drawable.radiobutton_on_background);
        if(isLast){
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            lp.bottomMargin = 150;
            view.setLayoutParams(lp);
        }
        return view;
    }


    @Override
    public void onClick(View v) {
        if (v == imgEdit) {
            Intent intent = new Intent(activity, FragmetActivity.class);
            intent.putExtra("configPath", configPath);
            intent.putExtra("configType", BaseConfig.VPN);
            startActivity(intent);
        } else if (v == imgDelete) {
            new File(configPath).delete();
            activity.refreshView();
        } else if (v == imgSelect) {
            activity.setSelect(index);
        } else if (v == imgTest) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    try {
                        int code = TestUtil.testVPNConfig(config);
                        if (code == 101)
                            msg.obj = "连接成功";
                        else if (code == 403)
                            msg.obj = "鉴权失败";
                        else if (code == 200)
                            msg.obj = "返回200 OK，很可能是path 设置错误";
                        else
                            msg.obj = "未知原因: " + code;
                    } catch (Exception e) {
                        e.printStackTrace();
                        msg.obj = "连接失败";
                    }
                    toastHandler.sendMessage(msg);
                }
            }).start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
