package man.who.scan.my.app.die.a.mother.model;

public class DNSConfig extends BaseConfig{

    public boolean useCunstomDNS = true;
    public boolean useHost = true;
    public String dns1 = "114.114.114.114";
    //    public String dns2 = "";
    public boolean useDoH = true;
    public String dohDomain = "dns.alidns.com";
    public String dohHost = "223.6.6.6";
    public String dohPath = "/dns-query";
}
