package man.who.scan.my.app.die.a.mother.model;

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

    public boolean usePAC = true;
    public boolean transeportSNI2Remote = true;
    public boolean detectSNI = true;
    public boolean directIfCN = true;
}
