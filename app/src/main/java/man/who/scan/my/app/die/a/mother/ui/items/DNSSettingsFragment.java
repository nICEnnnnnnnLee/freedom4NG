package man.who.scan.my.app.die.a.mother.ui.items;

import android.app.Fragment;
import android.content.res.Resources;
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
import man.who.scan.my.app.die.a.mother.ui.FragmentActivity;
import man.who.scan.my.app.die.a.mother.ui.base.ToastHandler;
import man.who.scan.my.app.die.a.mother.vpn.server.DoH;

public class DNSSettingsFragment extends Fragment implements View.OnClickListener {

    View view, imgSave, imgDelete, imgReload, btnAutofillDoh, btn_test_doh;
    EditText dohHost, doHDomain, dohPath;
    FragmentActivity activity;
    DNSConfig config;
    Resources resources;
    private Handler toastHandler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (FragmentActivity) getActivity();
        toastHandler = new ToastHandler(activity);
        Map<String, String> map = Config.fromFile(activity.configPath);
        config = Global.dnsConfig;
        resources = this.getResources();
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
        title.setText(R.string.dns_settings);
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
                        msg.obj = resources.getString(R.string.tips_ip_query_ok);
                    } catch (Exception e) {
                        e.printStackTrace();
                        msg.obj = resources.getString(R.string.tips_ip_query_not_ok) + e;
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
                        msg.obj = resources.getString(R.string.tips_doh_invalid) + e;
                    }
                    toastHandler.sendMessage(msg);
                }
            }).start();
            return;
        }
        String tips = resources.getString(R.string.tips_undealt_click);
        if (Global.isRun) {
            tips = resources.getString(R.string.tips_vpn_not_shut_down);
        } else if (v == imgSave) {
            config.getFromView(view);
//            System.out.println("保存的DNS配置 :" + config);
            boolean result = Config.toFile(config.toMap(), activity.configPath);
            tips = result ? resources.getString(R.string.tips_save_ok) : resources.getString(R.string.tips_save_not_ok);
        } else if (v == imgReload) {
            Map<String, String> map = Config.fromFile(activity.configPath);
            if (map != null) {
                config.fromMap(map);
                config.updateView(view);
                tips = resources.getString(R.string.tips_reloaded);
            } else {
                tips = resources.getString(R.string.tips_no_dns_setting_file);
            }
        }
        Toast toast = Toast.makeText(activity, tips, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
