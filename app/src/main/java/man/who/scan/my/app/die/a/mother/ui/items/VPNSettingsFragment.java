package man.who.scan.my.app.die.a.mother.ui.items;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Map;

import man.who.scan.my.app.die.a.mother.Config;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.model.VPNConfig;
import man.who.scan.my.app.die.a.mother.ui.FragmetActivity;
import man.who.scan.my.app.die.a.mother.vpn.util.ResourcesUtil;

public class VPNSettingsFragment extends Fragment implements View.OnClickListener {

    ImageView imgSave, imgDelete, imgReload, imgShare;
    FragmetActivity activity;
    File configFile;
    View view;
    VPNConfig config;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (FragmetActivity) getActivity();
        configFile = new File(activity.configPath);
        Map<String, String> map = Config.fromFile(configFile);
        config = new VPNConfig();
        config.remark = configFile.getName();
        if (map != null) {
            config.fromMap(map);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.vpn_settings, container, false);
        config.initView(view);
        config.updateView(view);
        TextView title = activity.findViewById(R.id.tv_title);
        title.setText("VPN 设置");
        imgSave = activity.findViewById(R.id.iv_save);
        imgDelete = activity.findViewById(R.id.iv_delete);
        imgReload = activity.findViewById(R.id.iv_reload);
        imgShare = activity.findViewById(R.id.iv_share);
        imgSave.setVisibility(View.VISIBLE);
        imgDelete.setVisibility(View.VISIBLE);
        imgReload.setVisibility(View.VISIBLE);
        imgShare.setVisibility(View.VISIBLE);
        imgSave.setOnClickListener(this);
        imgDelete.setOnClickListener(this);
        imgReload.setOnClickListener(this);
        imgShare.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        String tips = "未处理的点击";
        if (v == imgShare) {
            try {
                String content = ResourcesUtil.readAll(configFile);
                content = Base64.encodeToString(content.getBytes("utf-8"), Base64.DEFAULT);
                ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", content);
                cm.setPrimaryClip(mClipData);
                tips = "配置已经复制到剪贴板";
            } catch (Exception e) {
                tips = "复制配置出现错误";
            }
        } else if (v == imgSave) {
            config.getFromView(view);
//            System.out.println("保存的Host配置 :" + config);
            if (Config.toFile(config.toMap(), configFile)) {
                if (config.remark.equals(configFile.getName())) {
                    tips = "保存成功";
                } else {
                    File newConfigFile = new File(configFile.getParent(), config.remark);
                    if (configFile.renameTo(newConfigFile)) {
                        configFile = newConfigFile;
                        activity.configPath = newConfigFile.getAbsolutePath();
                        tips = "保存并重命名文件成功！";
                    } else
                        tips = "配置保存成功，但重命名失败";
                }
            } else
                tips = "保存失败！";
        } else if (v == imgDelete) {
            boolean result = new File(activity.configPath).delete();
            tips = result ? "删除成功！" : "删除失败！";
        } else if (v == imgReload) {
            Map<String, String> map = Config.fromFile(activity.configPath);
            if (map != null) {
                config.fromMap(map);
//                System.out.println("读取的Host配置 :" + config);
                config.updateView(view);
                tips = "已重新加载";
            } else {
                tips = "没有VPN配置文件！";
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
