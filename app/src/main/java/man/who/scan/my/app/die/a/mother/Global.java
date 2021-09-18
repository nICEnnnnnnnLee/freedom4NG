package man.who.scan.my.app.die.a.mother;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import man.who.scan.my.app.die.a.mother.model.DNSConfig;
import man.who.scan.my.app.die.a.mother.model.DexConfig;
import man.who.scan.my.app.die.a.mother.model.VPNConfig;
import man.who.scan.my.app.die.a.mother.model.VPNGlobalConfig;
import man.who.scan.my.app.die.a.mother.vpn.util.CommonUtil;

public class Global {
    /**
     * 默认DNS 配置在 dns.ini
     * VPN 配置文件在 vpns 文件夹下
     */
    public final static String DNS_PATH = "/dns.ini";
    public final static String HOST_PATH = "/host.ini";
    public final static String DEX_CONFIG_PATH = "/dex.ini";
    public final static String VPN_DIR_PATH = "vpns";
    public final static String DEX_DIR_PATH = "dex";

    public static File ROOT_DIR;
    public static File VPN_DIR;
    public static File DEX_DIR;
    public static File DEX_CONFIG_FILE;
    public static File HOST_FILE;
    public static File DNS_FILE;

    public static boolean isRun = false;
    public static int currentVPNConfigIndex = -1;
    public static String currentVPNConfigRemark;
    public static VPNConfig vpnConfig = new VPNConfig();
    final public static DNSConfig dnsConfig = new DNSConfig();
    final public static DexConfig dexConfig = new DexConfig();
    final public static VPNGlobalConfig vpnGlobalConfig = new VPNGlobalConfig();
    final public static HashMap<String, String> hostConfig = new HashMap<>();
    public static ConcurrentHashMap<String, String> hostTableRuntime;


    final public static ConcurrentHashMap<String, String> cookies = new ConcurrentHashMap<>();

    public static void initCookies() {
        cookies.put("my_type", "1");
        cookies.put("my_domain", "");
        cookies.put("my_port", "0");
        cookies.put("my_username", vpnConfig.username);
//        cookies.put("my_token", CommonUtil.MD5(vpnConfig.password + vpnConfig.salt));
    }

    static {
        initCookies();
    }
}