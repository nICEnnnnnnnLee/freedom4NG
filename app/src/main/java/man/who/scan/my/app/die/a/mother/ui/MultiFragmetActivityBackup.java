package man.who.scan.my.app.die.a.mother.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.drawerlayout.widget.DrawerLayout;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.model.BaseConfig;
import man.who.scan.my.app.die.a.mother.ui.base.EmptyFragment;
import man.who.scan.my.app.die.a.mother.ui.items.VPNBriefFragment;
import man.who.scan.my.app.die.a.mother.vpn.LocalVpnService;

public class MultiFragmetActivityBackup extends Activity implements View.OnClickListener {

    View imgAdd, imgMore, fab_start, fab_stop, tv_dns, tv_host, menu;
    private DrawerLayout mDrawerLayout;
    PopupMenu popupMenu;
    File configDir;
    //    public int currentVPNConfigIndex = -1;
//    public String currentVPNConfigRemark;
    List<VPNBriefFragment> frags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_drawer);

        TextView title = findViewById(R.id.tv_title);
        title.setText("配置列表");
        mDrawerLayout = findViewById(R.id.drawerLayout);
        imgMore = findViewById(R.id.iv_more);
        imgMore.setVisibility(View.VISIBLE);
        imgMore.setOnClickListener(this);
        imgAdd = findViewById(R.id.iv_add);
        imgAdd.setVisibility(View.VISIBLE);
        imgAdd.setOnClickListener(this);
        tv_dns = findViewById(R.id.tv_dns);
        tv_dns.setOnClickListener(this);
        tv_host = findViewById(R.id.tv_host);
        tv_host.setOnClickListener(this);
        fab_start = findViewById(R.id.fab_start);
        fab_start.setOnClickListener(this);
        fab_stop = findViewById(R.id.fab_stop);
        fab_stop.setOnClickListener(this);
        if (Global.isRun)
            fab_stop.setVisibility(View.VISIBLE);
        else
            fab_start.setVisibility(View.VISIBLE);
        frags = new ArrayList<>();
        System.out.println("\n\n-----------------inflate menu-------------------------------\n\n" + menu);
        popupMenu = new PopupMenu(MultiFragmetActivityBackup.this, imgAdd);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.base_menu, popupMenu.getMenu());
//        popupMenu.show();
//        this.registerForContextMenu(iv_add);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("重新刷新");
        refreshView();
    }

    public void setSelect(int index) {
//        LinearLayout layout = findViewById(R.id.base);
//        ImageView img = layout.getChildAt(index + 1).findViewById(R.id.iv_selected);
//        img.setImageResource(android.R.drawable.radiobutton_on_background);
        if (Global.currentVPNConfigIndex >= 0)
            frags.get(Global.currentVPNConfigIndex).imgSelect.setImageResource(android.R.drawable.radiobutton_off_background);
        if (index >= 0)
            frags.get(index).imgSelect.setImageResource(android.R.drawable.radiobutton_on_background);
        Global.currentVPNConfigIndex = index;
        Global.currentVPNConfigRemark = frags.get(Global.currentVPNConfigIndex).config.remark;
    }

    public void refreshView() {
        mDrawerLayout.closeDrawers();
        FragmentManager manager = this.getFragmentManager();
//        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = manager.beginTransaction();
        configDir = Global.VPN_DIR;
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
        System.out.printf("--onActivityResult requestCode: %s, resultCode: %s, data: %s\n", requestCode, resultCode, data);
        if(requestCode == 0){
            Intent serviceIntent = new Intent(this, LocalVpnService.class);
            startService(serviceIntent);
        }else if(requestCode == 1 && resultCode == RESULT_OK){
            Uri uri = data.getData();
            System.out.println(uri.toString());
            Toast toast = Toast.makeText(MultiFragmetActivityBackup.this, uri.toString(), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == imgMore) {
            Intent intent = new Intent();
            intent.setType("*/*");//无类型限制
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 1);
        } else if (v == imgAdd) {
            LayoutInflater factory = LayoutInflater.from(MultiFragmetActivityBackup.this);//提示框
            final View view = factory.inflate(R.layout.base_prompt_edit_text, null);//这里必须是final的
            final EditText edit = (EditText) view.findViewById(R.id.editText);//获得输入框对象
            new AlertDialog.Builder(MultiFragmetActivityBackup.this)
                    .setTitle("配置名称(备注)")//提示框标题
                    .setView(view)
                    .setPositiveButton("确定",//提示框的两个按钮
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    File config = new File(configDir, edit.getText().toString());
                                    if (config.exists()) {
                                        Toast toast = Toast.makeText(MultiFragmetActivityBackup.this, "已经存在该文件", Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();
                                    } else {
                                        Intent intent = new Intent(MultiFragmetActivityBackup.this, FragmentActivity.class);
                                        intent.putExtra("configPath", config.getAbsolutePath());
                                        intent.putExtra("configType", BaseConfig.VPN);
                                        startActivity(intent);
                                    }

                                }
                            })
                    .setNegativeButton("取消", null).create().show();
        } else if (v == fab_start) {
            if (Global.currentVPNConfigIndex < 0) {
                Toast toast = Toast.makeText(MultiFragmetActivityBackup.this, "尚未选择配置！！", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } else {
                Global.vpnConfig = frags.get(Global.currentVPNConfigIndex).config;
                startVPN();
                fab_stop.setVisibility(View.VISIBLE);
                fab_start.setVisibility(View.GONE);
            }
        } else if (v == fab_stop) {
            stopVPN();
            fab_stop.setVisibility(View.GONE);
            fab_start.setVisibility(View.VISIBLE);
        } else if (v == tv_dns) {
            Intent intent = new Intent(MultiFragmetActivityBackup.this, FragmentActivity.class);
            intent.putExtra("configPath", Global.ROOT_DIR + Global.DNS_PATH);
            intent.putExtra("configType", BaseConfig.DNS);
            startActivity(intent);
        } else if (v == tv_host) {
            Intent intent = new Intent(MultiFragmetActivityBackup.this, FragmentActivity.class);
            intent.putExtra("configPath", Global.ROOT_DIR + Global.HOST_PATH);
            intent.putExtra("configType", BaseConfig.HOST);
            startActivity(intent);
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
            startActivityForResult(intent, 0);
//            System.out.println("startActivityForResult");
            Global.isRun = true;
        } else {
            onActivityResult(0, RESULT_OK, null);
//            System.out.println("onActivityResult");
            Global.isRun = true;
        }
    }

}
