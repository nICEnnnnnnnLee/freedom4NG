package man.who.scan.my.app.die.a.mother.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import man.who.scan.my.app.die.a.mother.Config;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.vpn.LocalVpnService;
@Deprecated // 现在没有在使用了
public class GlobalConfigActivity extends Activity {
    //    Intent serviceIntent;
    public static String rPath;
    String configPath = "./config.ini";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rPath = getFilesDir().getAbsolutePath();
        setContentView(R.layout.activity_main);
        initView();
    }

    void initView() {
        Config.file2Global(configPath);
        Global.vpnConfig.initView(this.getWindow().getDecorView());
        Global.dnsConfig.initView(this.getWindow().getDecorView());
        global2View();

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVPN();
            }
        });

        Button btnEnd = findViewById(R.id.btnEnd);
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVPN();
            }
        });

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view2Global();
                String tips = Config.global2File(configPath) ? "保存成功！" : "保存失败！";
                Toast toast = Toast.makeText(GlobalConfigActivity.this, tips, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        Button btnReload = findViewById(R.id.btnReload);
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = Config.file2Global(configPath);
                String tips = result ? "加载成功！" : "加载失败！";
                Toast toast = Toast.makeText(GlobalConfigActivity.this, tips, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                if (result)
                    global2View();
            }
        });

        Button btnTest = findViewById(R.id.btnHostRead);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(GlobalConfigActivity.this, FragmentActivity.class);
//                intent.putExtra("configPath",rPath + "/config.ini");
//                intent.putExtra("configType", 3);
                Intent intent = new Intent(GlobalConfigActivity.this, MultiFragmentActivity.class);
                startActivity(intent);
            }
        });
    }

    void global2View() {
        Global.vpnConfig.updateView(this.getWindow().getDecorView());
        Global.dnsConfig.updateView(this.getWindow().getDecorView());
    }

    void view2Global() {
        Global.vpnConfig.getFromView(this.getWindow().getDecorView());
        Global.dnsConfig.getFromView(this.getWindow().getDecorView());
        Global.vpnConfig.init();
    }

    private void stopVPN() {
        LocalVpnService.Instance.stopVPN();
    }

    private void startVPN() {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            startActivityForResult(intent, 0);
        } else {
            onActivityResult(0, RESULT_OK, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent serviceIntent = new Intent(this, LocalVpnService.class);
        startService(serviceIntent);
    }


}
