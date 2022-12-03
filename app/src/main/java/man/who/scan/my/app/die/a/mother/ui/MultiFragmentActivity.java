package man.who.scan.my.app.die.a.mother.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Base64;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.model.BaseConfig;
import man.who.scan.my.app.die.a.mother.ui.base.BaseDrawerActivity;
import man.who.scan.my.app.die.a.mother.ui.base.EmptyFragment;
import man.who.scan.my.app.die.a.mother.ui.items.VPNBriefFragment;
import man.who.scan.my.app.die.a.mother.vpn.LocalVpnService;
import man.who.scan.my.app.die.a.mother.vpn.util.ResourcesUtil;

public class MultiFragmentActivity extends BaseDrawerActivity {

    final static int START_VPN = 0;
    final static int SELECT_FOLDER = 1;
    final static int SELECT_FILE = 2;
    final static int SELECT_FILE_BY_SYSTEM = 5;
    PopupMenu popupMenu;
    File configDir;
    List<VPNBriefFragment> frags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configDir = Global.VPN_DIR;
        tv_title.setText(R.string.settings);
        iv_more.setVisibility(View.VISIBLE);
        iv_add.setVisibility(View.VISIBLE);
        View[] onClicks = {iv_more, iv_add, tv_dns, tv_host, fab_start, fab_stop};
        setOnClickListener(onClicks, this);
        if (Global.isRun)
            fab_stop.setVisibility(View.VISIBLE);
        else
            fab_start.setVisibility(View.VISIBLE);
        frags = new ArrayList<>();

        popupMenu = new PopupMenu(MultiFragmentActivity.this, iv_more);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.base_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_export:
                        Intent intent = new Intent(MultiFragmentActivity.this, FileChooserActivity.class);
                        intent.putExtra("type", "dir");
                        startActivityForResult(intent, SELECT_FOLDER);
                        break;
                    case R.id.action_import:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("*/*");
                            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toURI());
                            startActivityForResult(intent, SELECT_FILE_BY_SYSTEM);
                        } else {
                            intent = new Intent(MultiFragmentActivity.this, FileChooserActivity.class);
                            intent.putExtra("type", "file");
                            startActivityForResult(intent, SELECT_FILE);
                        }
                        break;
                    case R.id.action_import_from_clipboard:
                        //获取剪贴板管理器：
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = cm.getPrimaryClip();
                        if (clipData.getItemCount() > 0) {
                            try {
                                String content = clipData.getItemAt(0).getText().toString();
                                String decodedContent = new String(Base64.decode(content, Base64.DEFAULT), StandardCharsets.UTF_8);
//                                System.out.println(decodedContent);
                                if (decodedContent.contains("remoteHost:") && decodedContent.contains("remotePort:")) {
                                    File file = null;
                                    int index = 0;
                                    do {
                                        String name = String.format("未命名 %02d", index);
                                        file = new File(Global.VPN_DIR, name);
                                        index++;
                                    } while (file.exists());
                                    File parent = file.getParentFile();
                                    if (!parent.exists())
                                        parent.mkdirs();
                                    try (FileWriter writer = new FileWriter(file)) {
                                        writer.write(decodedContent);
                                        MultiFragmentActivity.this.toast(resources.getString(R.string.tips_import_clipborad_ok));
                                        refreshView();
                                    } catch (Exception e) {
                                        MultiFragmentActivity.this.toast(resources.getString(R.string.tips_write_err) + e.toString());
                                    }

                                } else
                                    MultiFragmentActivity.this.toast(resources.getString(R.string.tips_clipborad_err_data));
                            } catch (Exception e) {
                                MultiFragmentActivity.this.toast(resources.getString(R.string.tips_clipborad_err_data));
                            }
                        } else {
                            MultiFragmentActivity.this.toast(resources.getString(R.string.tips_clipborad_no_data));
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshView();
    }

    public void setSelect(int index) {
        if (Global.currentVPNConfigIndex >= 0)
            frags.get(Global.currentVPNConfigIndex).imgSelect.setImageResource(android.R.drawable.radiobutton_off_background);
        if (index >= 0)
            frags.get(index).imgSelect.setImageResource(android.R.drawable.radiobutton_on_background);
        Global.currentVPNConfigIndex = index;
        Global.currentVPNConfigRemark = frags.get(Global.currentVPNConfigIndex).config.remark;
    }

    public void refreshView() {
        FragmentManager manager = this.getFragmentManager();
//        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = manager.beginTransaction();
//        System.out.println("\n=========================\n" + configDir.getAbsolutePath() + "\n=========================\n" );
        if (configDir.exists()) {
            transaction.replace(R.id.base, new EmptyFragment());
            frags.clear();
            File[] listFiles = configDir.listFiles();
            for (int index = 0; index < listFiles.length; index++) {
                File file = listFiles[index];
                VPNBriefFragment vpnBrief = new VPNBriefFragment();
                Bundle bundle = new Bundle();
                bundle.putString("configPath", file.getAbsolutePath());
                bundle.putString("configName", file.getName());
                bundle.putInt("index", index);
                bundle.putBoolean("isLast", index == listFiles.length - 1);
                vpnBrief.setArguments(bundle);
                transaction.add(R.id.base, vpnBrief);
                frags.add(vpnBrief);
            }
            transaction.commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        System.out.printf("--onActivityResult requestCode: %s, resultCode: %s, data: %s\n", requestCode, resultCode, data);
        switch (requestCode) {
            case START_VPN:
                if (resultCode == RESULT_OK) { // 用于开启VPN服务
                    Intent serviceIntent = new Intent(this, LocalVpnService.class);
                    startService(serviceIntent);
                }
                break;
            case SELECT_FOLDER:
                if (resultCode == RESULT_OK) { // 用于接收导出文件的结果
                    String folder = data.getStringExtra("path");
                    String name = String.format("Freedom.%s.zip", System.currentTimeMillis() / (1000 * 60));
                    File dstZip = new File(folder, name);
                    try {
                        ResourcesUtil.toZip(Global.ROOT_DIR, dstZip);
                        this.toast(resources.getString(R.string.tips_export_ok));
                    } catch (Exception e) {
                        this.toast(resources.getString(R.string.tips_export_not_ok));
                    }
                }
                break;
            case SELECT_FILE:
                if (resultCode == RESULT_OK) { // 用于接收导出文件的结果
                    String zipFile = data.getStringExtra("path");
                    try {
                        ResourcesUtil.unZip(new File(zipFile), Global.ROOT_DIR.getAbsolutePath());
                        this.toast(resources.getString(R.string.tips_import_ok_need_reboot));
                    } catch (Exception e) {
                        this.toast(resources.getString(R.string.tips_import_not_ok));
                    }
                }
                break;
            case SELECT_FILE_BY_SYSTEM:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    String zipFile = getRealPath(uri);
                    try {
                        ResourcesUtil.unZip(new File(zipFile), Global.ROOT_DIR.getAbsolutePath());
                        this.toast(resources.getString(R.string.tips_import_ok_need_reboot));
                    } catch (Exception e) {
                        this.toast(resources.getString(R.string.tips_import_not_ok));
                    }
                }
                break;
            default:
                break;
        }
    }

    // http://t.zoukankan.com/androidxiaoyang-p-4968663.html
    private String getRealPath(Uri fileUrl) {
        String fileName = null;
        if (fileUrl != null) {
            if (fileUrl.getScheme().toString().compareTo("content") == 0) // content://开头的uri
            {
                //把文件复制到沙盒目录
                ContentResolver contentResolver = this.getContentResolver();
                String displayName = "temp.zip";
                try {
                    InputStream is = contentResolver.openInputStream(fileUrl);
                    File cache = new File(this.getCacheDir().getAbsolutePath(), displayName);
                    FileOutputStream fos = new FileOutputStream(cache);
                    ResourcesUtil.copy(is, fos);
                    fileName = cache.getCanonicalPath();
                    fos.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (fileUrl.getScheme().compareTo("file") == 0) // file:///开头的uri
            {
                fileName = fileUrl.getPath();
            }
        }
        return fileName;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_more:
                popupMenu.show();
                break;
            case R.id.iv_add:
                final View view = layoutInflater.inflate(R.layout.base_prompt_edit_text, null);//这里必须是final的
                final EditText edit = (EditText) view.findViewById(R.id.editText);//获得输入框对象
                new AlertDialog.Builder(MultiFragmentActivity.this)
                        .setTitle(resources.getString(R.string.setting_name_remark))//提示框标题
                        .setView(view)
                        .setPositiveButton(resources.getString(R.string.OK),//提示框的两个按钮
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        File config = new File(configDir, edit.getText().toString());
                                        if (config.exists()) {
                                            MultiFragmentActivity.this.toast(resources.getString(R.string.tips_file_exists));
                                        } else {
                                            Intent intent = new Intent(MultiFragmentActivity.this, FragmentActivity.class);
                                            intent.putExtra("configPath", config.getAbsolutePath());
                                            intent.putExtra("configType", BaseConfig.VPN);
                                            startActivity(intent);
                                        }

                                    }
                                })
                        .setNegativeButton(resources.getString(R.string.CANCEL), null).create().show();
                break;
            case R.id.fab_start:
                if (Global.currentVPNConfigIndex < 0) {
                    this.toast(resources.getString(R.string.tips_setting_not_select));
                } else {
                    Global.vpnConfig = frags.get(Global.currentVPNConfigIndex).config;
                    startVPN();
                    fab_stop.setVisibility(View.VISIBLE);
                    fab_start.setVisibility(View.GONE);
                }
                break;
            case R.id.fab_stop:
                stopVPN();
                fab_stop.setVisibility(View.GONE);
                fab_start.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void stopVPN() {
        LocalVpnService.Instance.stopVPN();
        Global.isRun = false;
    }

    private void startVPN() {
        Intent intent = VpnService.prepare(this);
        Global.initCookies();
        if (intent != null) {
            startActivityForResult(intent, START_VPN);
//            System.out.println("startActivityForResult");
            Global.isRun = true;
        } else {
            onActivityResult(START_VPN, RESULT_OK, null);
//            System.out.println("onActivityResult");
            Global.isRun = true;
        }
    }

}
