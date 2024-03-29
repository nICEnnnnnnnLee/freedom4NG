package man.who.scan.my.app.die.a.mother.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.vpn.grpc.FreedomGrpc;

public class VPNConfig extends BaseConfig {
    public String remark = "";
    public boolean directAll = false;
    public int localPort = 9527;
    public String remoteHost = "192.168.1.111";
    public int remotePort = 6666;
    public String salt = "salt";
    public String username = "username";
    public String password = "pwd";
    public boolean useSSL = true;
    public boolean verifySSL = true;
    // 用于HTTP头部
    public String path = "/";
    public String http_version = "1.1";
    public String domain = "yourdomain.com";
    public String port = "443";
    public String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0";

    public String proxyMode = "ws"; // ws grpc
    public String grpcServiceName = "freedomGo.grpc.Freedom"; // grpc模式时, path为 /{grpcServiceName}/Pipe
//    public boolean usePAC = true;
//    public String pacPath = "*";
    public boolean useGeoDomain = false;
    public String gfwPath = "*";
    public boolean transportSNI2Remote = false;
    public boolean detectSNI = false;
    public boolean directIfCN = true;

    public boolean exportHostCacheAfterServiceStop = false;

    public void init() {
        Global.cookies.put("my_type", "1");
        Global.cookies.put("my_domain", "");
        Global.cookies.put("my_port", "0");
        Global.cookies.put("my_username", username);

        FreedomGrpc.SERVICE_NAME = grpcServiceName;
        FreedomGrpc.resetPipeMethod();
    }
}
