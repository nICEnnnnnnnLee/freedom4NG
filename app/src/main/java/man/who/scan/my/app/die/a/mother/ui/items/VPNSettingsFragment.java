package man.who.scan.my.app.die.a.mother.ui.items;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import man.who.scan.my.app.die.a.mother.Config;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.model.VPNConfig;
import man.who.scan.my.app.die.a.mother.ui.FileChooserActivity;
import man.who.scan.my.app.die.a.mother.ui.FragmetActivity;
import man.who.scan.my.app.die.a.mother.ui.MultiFragmetActivity;
import man.who.scan.my.app.die.a.mother.vpn.util.ResourcesUtil;

import static android.app.Activity.RESULT_OK;
import static man.who.scan.my.app.die.a.mother.ui.base.BaseFragment.SELECT_FILE;

public class VPNSettingsFragment extends Fragment implements View.OnClickListener {

    ImageView imgSave, imgDelete, imgReload, imgShare;
    Button btnSelectPacPath;
    FragmetActivity activity;
    File configFile;
    View view;
    VPNConfig config;
    Resources resources;
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
        resources = this.getResources();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.vpn_settings, container, false);
        config.initView(view);
        config.updateView(view);
        TextView title = activity.findViewById(R.id.tv_title);
        title.setText(R.string.vpn_settings);
        imgSave = activity.findViewById(R.id.iv_save);
        imgDelete = activity.findViewById(R.id.iv_delete);
        imgReload = activity.findViewById(R.id.iv_reload);
        imgShare = activity.findViewById(R.id.iv_share);
        btnSelectPacPath = view.findViewById(R.id.btnSelectPacPath);
        imgSave.setVisibility(View.VISIBLE);
        imgDelete.setVisibility(View.VISIBLE);
        imgReload.setVisibility(View.VISIBLE);
        imgShare.setVisibility(View.VISIBLE);
        imgSave.setOnClickListener(this);
        imgDelete.setOnClickListener(this);
        imgReload.setOnClickListener(this);
        imgShare.setOnClickListener(this);
        btnSelectPacPath.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        String tips = resources.getString(R.string.tips_undealt_click);
        if (v == imgShare) {
            try {
                String content = ResourcesUtil.readAll(configFile);
                content = Base64.encodeToString(content.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
                ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", content);
                cm.setPrimaryClip(mClipData);
                tips = resources.getString(R.string.tips_settings_copied);
            } catch (Exception e) {
                tips = resources.getString(R.string.tips_setting_copy_err);
            }
        } else if (v == imgSave) {
            config.getFromView(view);
//            System.out.println("保存的Host配置 :" + config);
            if (Config.toFile(config.toMap(), configFile)) {
                if (config.remark.equals(configFile.getName())) {
                    tips = resources.getString(R.string.tips_save_ok);
                } else {
                    File newConfigFile = new File(configFile.getParent(), config.remark);
                    if (configFile.renameTo(newConfigFile)) {
                        configFile = newConfigFile;
                        activity.configPath = newConfigFile.getAbsolutePath();
                        tips = resources.getString(R.string.tips_save_and_rename_ok);
                    } else
                        tips = resources.getString(R.string.tips_save_ok_rename_err);
                }
            } else
                tips = resources.getString(R.string.tips_save_not_ok);
        } else if (v == imgDelete) {
            boolean result = new File(activity.configPath).delete();
            tips = result ? resources.getString(R.string.tips_delete_ok) : resources.getString(R.string.tips_delete_not_ok);
        } else if (v == imgReload) {
            Map<String, String> map = Config.fromFile(activity.configPath);
            if (map != null) {
                config.fromMap(map);
//                System.out.println("读取的Host配置 :" + config);
                config.updateView(view);
                tips = resources.getString(R.string.tips_reloaded);
            } else {
                tips = resources.getString(R.string.tips_no_vpn_settings);
            }
        }else if(v == btnSelectPacPath){
            Intent intent = new Intent(activity, FileChooserActivity.class);
            intent.putExtra("type", "file");
            startActivityForResult(intent, SELECT_FILE);
            return;
        }
        Toast toast = Toast.makeText(activity, tips, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_FILE:
                if (resultCode == RESULT_OK) {
                    String gfw_path = data.getStringExtra("path");
                    TextView tvGfwPath = this.getView().findViewById(R.id.gfwPath);
                    tvGfwPath.setText(gfw_path);
                }
                break;
        }
    }
}
