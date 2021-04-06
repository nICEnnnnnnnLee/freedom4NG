package man.who.scan.my.app.die.a.mother.vpn.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.model.VPNConfig;
import man.who.scan.my.app.die.a.mother.vpn.server.handler.BackendAuthHandler;

public class TestUtil {

    public static int testVPNConfig(VPNConfig vpn) throws Exception {
        HttpURLConnection conn = null;
        try {
            String scheme = vpn.useSSL ? "https://" : "http://";
            String path = vpn.path;
            String domain = vpn.useSSL ? vpn.domain : vpn.remoteHost;
            String port = "" + vpn.remotePort;
            String host = String.format("%s:%s", domain, port);
            String origin = scheme + host;
            URL realUrl = new URL(origin + path);
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Host", host);
            conn.setRequestProperty("User-Agent", vpn.userAgent);
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Sec-WebSocket-Version", "13");
            conn.setRequestProperty("Origin", origin);
            HashMap<String, String> newCookie = new HashMap<>(Global.cookies);
            newCookie.put("my_username", vpn.username);
            newCookie.put("my_token", CommonUtil.MD5(vpn.password + vpn.salt));
            conn.setRequestProperty("Cookie", BackendAuthHandler.genCookie(newCookie));
            conn.connect();
            return (conn.getResponseCode()); // && "ok".equals(conn.getHeaderField("auth"))
        } finally {
            conn.disconnect();
        }

    }
}
