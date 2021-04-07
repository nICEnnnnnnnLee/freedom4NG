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
            String currentTime = String.valueOf(System.currentTimeMillis());
            System.out.println("vpn.password:" + vpn.password);
            System.out.println("vpn.salt:" + vpn.salt);
            String token = new StringBuilder(vpn.password).append(vpn.salt).append(currentTime).toString();
            newCookie.put("my_time", currentTime);
            newCookie.put("my_token", CommonUtil.MD5(token));
            newCookie.put("my_username", vpn.username);
            conn.setRequestProperty("Cookie", BackendAuthHandler.genCookie(newCookie));
            conn.connect();
            return (conn.getResponseCode()); // && "ok".equals(conn.getHeaderField("auth"))
        } finally {
            conn.disconnect();
        }

    }
}
