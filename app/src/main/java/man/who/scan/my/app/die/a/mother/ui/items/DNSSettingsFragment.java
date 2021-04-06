package man.who.scan.my.app.die.a.mother.ui.items;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.util.Map;

import man.who.scan.my.app.die.a.mother.Config;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.model.DNSConfig;
import man.who.scan.my.app.die.a.mother.ui.FragmetActivity;
import man.who.scan.my.app.die.a.mother.ui.base.ToastHandler;
import man.who.scan.my.app.die.a.mother.vpn.server.DoH;

public class DNSSettingsFragment extends Fragment implements View.OnClickListener {

    View view, imgSave, imgDelete, imgReload, btnAutofillDoh, btn_test_doh;
    EditText dohHost, doHDomain, dohPath;
    FragmetActivity activity;
    DNSConfig config;
    private Handler toastHandler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (FragmetActivity) getActivity();
        toastHandler = new ToastHandler(activity);
        Map<String, String> map = Config.fromFile(activity.configPath);
        config = Global.dnsConfig;
        if (map != null) {
            config.fromMap(map);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.dns_settings, container, false);
        config.initView(view);
        config.updateView(view);
        TextView title = activity.findViewById(R.id.tv_title);
        title.setText("DNS 设置");
        doHDomain = view.findViewById(R.id.dohDomain);
        dohHost = view.findViewById(R.id.dohHost);
        dohPath = view.findViewById(R.id.dohPath);
        btnAutofillDoh = view.findViewById(R.id.btn_autofill_doh);
        btn_test_doh = view.findViewById(R.id.btn_test_doh);
        imgSave = activity.findViewById(R.id.iv_save);
//        imgDelete = activity.findViewById(R.id.iv_delete);
        imgReload = activity.findViewById(R.id.iv_reload);
        imgSave.setVisibility(View.VISIBLE);
//        imgDelete.setVisibility(View.VISIBLE);
        imgReload.setVisibility(View.VISIBLE);
        btnAutofillDoh.setOnClickListener(this);
        btn_test_doh.setOnClickListener(this);
        imgSave.setOnClickListener(this);
//        imgDelete.setOnClickListener(this);
        imgReload.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        if (v == btnAutofillDoh) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    String domain = doHDomain.getText().toString();
                    try {
                        String ip = Inet4Address.getByName(domain).getHostAddress();
                        dohHost.setText(ip);
                        msg.obj = "IP 查询成功";
                    } catch (Exception e) {
                        e.printStackTrace();
                        msg.obj = "IP 查询失败： " + e;
                    }
                    toastHandler.sendMessage(msg);
                }
            }).start();
            return;
        } else if (v == btn_test_doh) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    try {
                        msg.obj = new DoH(doHDomain.getText().toString(),
                                dohHost.getText().toString(),
                                dohPath.getText().toString()
                        ).test();
                    } catch (Exception e) {
                        e.printStackTrace();
                        msg.obj = "doh 无效： " + e;
                    }
                    toastHandler.sendMessage(msg);
                }
            }).start();
            return;
        }
        String tips = "未处理的点击";
        if (Global.isRun) {
            tips = "VPN 尚未关闭";
        } else if (v == imgSave) {
            config.getFromView(view);
//            System.out.println("保存的DNS配置 :" + config);
            boolean result = Config.toFile(config.toMap(), activity.configPath);
            tips = result ? "保存成功！" : "保存失败！";
        } else if (v == imgReload) {
            Map<String, String> map = Config.fromFile(activity.configPath);
            if (map != null) {
                config.fromMap(map);
                config.updateView(view);
                tips = "已重新加载";
            } else {
                tips = "没有DNS配置文件！";
            }
        }
        Toast toast = Toast.makeText(activity, tips, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
