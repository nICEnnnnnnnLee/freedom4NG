package man.who.scan.my.app.die.a.mother;

import android.os.Bundle;

import java.io.File;
import java.util.Map;

import man.who.scan.my.app.die.a.mother.ui.MultiFragmetActivity;

//public class MainActivity extends GlobalConfigActivity {
//public class MainActivity extends MultiFragmetActivity {
public class MainActivity extends MultiFragmetActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Global.ROOT_DIR = getFilesDir();
        Global.ROOT_DIR = new File(getFilesDir(), "data");
        Global.VPN_DIR = new File(Global.ROOT_DIR, Global.VPN_DIR_PATH);
        Global.DEX_DIR = new File(Global.ROOT_DIR, Global.DEX_DIR_PATH);
        Global.DEX_CONFIG_FILE = new File(Global.ROOT_DIR, Global.DEX_CONFIG_PATH);
        Global.HOST_FILE = new File(Global.ROOT_DIR, Global.HOST_PATH);
        Global.DNS_FILE = new File(Global.ROOT_DIR, Global.DNS_PATH);

        Map<String, String> map = Config.fromFile(Global.DNS_FILE);
        if (map != null)
            Global.dnsConfig.fromMap(map);
        Config.fromHostFile(Global.hostConfig, Global.HOST_FILE);
        super.onCreate(savedInstanceState);
    }

}
