package man.who.scan.my.app.die.a.mother.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.model.BaseConfig;
import man.who.scan.my.app.die.a.mother.ui.base.BaseFragment;
import man.who.scan.my.app.die.a.mother.ui.items.DNSSettingsFragment;
import man.who.scan.my.app.die.a.mother.ui.items.DexServiceFragment;
import man.who.scan.my.app.die.a.mother.ui.items.HostSettingsFragment;
import man.who.scan.my.app.die.a.mother.ui.items.VPNSettingsFragment;

public class FragmetActivity extends Activity {

    public String configPath;
    public int configType;

    BaseFragment frag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base);
//        Fragment frag = new VPNSettingsFragment(rPath + "/config.ini");
        Intent intent = getIntent();
        configPath = intent.getStringExtra("configPath");
        configType = intent.getIntExtra("configType", BaseConfig.DNS);
        FragmentManager manager = this.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        switch (configType) {
            case BaseConfig.DNS:
                DNSSettingsFragment dns = new DNSSettingsFragment();
                transaction.add(R.id.base, dns);
                break;
            case BaseConfig.VPN:
                VPNSettingsFragment vpn = new VPNSettingsFragment();
                transaction.add(R.id.base, vpn);
                break;
            case BaseConfig.HOST:
                HostSettingsFragment host = new HostSettingsFragment();
                transaction.add(R.id.base, host);
                break;
            case BaseConfig.DEX:
                frag = new DexServiceFragment();
                transaction.add(R.id.base, frag);
                break;
            default:
                break;
        }
        transaction.commit();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Intent serviceIntent = new Intent(this, LocalVpnService.class);
//        startService(serviceIntent);
    }
}