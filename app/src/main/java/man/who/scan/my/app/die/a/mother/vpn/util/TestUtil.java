package man.who.scan.my.app.die.a.mother.vpn.util;

import com.google.protobuf.ByteString;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import io.grpc.stub.StreamObserver;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.model.VPNConfig;
import man.who.scan.my.app.die.a.mother.vpn.grpc.Stream;
import man.who.scan.my.app.die.a.mother.vpn.grpc.TestClient;
import man.who.scan.my.app.die.a.mother.vpn.server.handler.BackendAuthHandler;

public class TestUtil {

    static Pattern statusCodePattern;

    public static int testVPNConfig(VPNConfig vpn) throws Exception {
        vpn.init();
//        System.out.println("\n\n-------------------\n" + vpn);
        switch (vpn.proxyMode) {
            case "ws":
                return testWebSocket(vpn);
            case "grpc":
                return testGrpc(vpn);
//                return testFactory(vpn);
            default:
                throw new RuntimeException("Proxy Mode invalie: " + vpn.proxyMode);
        }
    }

    public static int testGrpc(VPNConfig vpn) throws Exception {
        TestClient client = new TestClient("www.baidu.com", "80", vpn);
        try {
            StreamObserver<Stream.FreedomRequest> requestStreamObserver = client.connect();
            StringBuilder sb = new StringBuilder();
            sb.append("GET / HTTP/1.1\r\n");
            sb.append("Host: www.baidu.com:80\r\n");
            sb.append("Connection: closed\r\n");
            sb.append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n");
            sb.append(
                    "User-Agent: Mozilla/5.0 (Linux; Android 8.0; DUK-AL20 Build/HUAWEIDUK-AL20; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/48.0.2564.116 Mobile Safari/537.36 T7/9.1 baidubrowser/7.18.21.0 (Baidu; P1 8.0.0)\r\n");
            sb.append("Accept-Language: zh-CN,en-US;q=0.8\r\n\r\n");
            requestStreamObserver
                    .onNext(Stream.FreedomRequest.newBuilder().setData(ByteString.copyFromUtf8(sb.toString())).build());
            if (!client.finishLatch.await(20, TimeUnit.SECONDS)) {
                throw new RuntimeException(
                        "Could not finish rpc within 20 secs, the server is likely down");
            } else if (client.valid != null && client.valid) {
                requestStreamObserver.onCompleted();
                return 101;
            } else {
                throw new Exception(client.failed);
            }
        } finally {
            client.shutdown();
        }
    }

    public static int testWebSocket(VPNConfig vpn) throws Exception {
        Socket socket = null;
        try {
            try {
                socket = new Socket();
                socket.setSoTimeout(3000);
                socket.connect(new InetSocketAddress(vpn.remoteHost, vpn.remotePort), 3000);
                if (vpn.useSSL) {
                    SSLSocketFactory factory = TrustAllSSLUtil.getSSLContext(vpn.verifySSL).getSocketFactory();
                    socket = factory.createSocket(socket, vpn.domain, vpn.remotePort, true);
                    if (vpn.verifySSL && !isValid(vpn.domain, (SSLSocket) socket)) {
                        return -1;
                    }
                }
            } catch (Exception e) {
                return -1; // 连接出了问题
            }
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            try {
                HashMap<String, String> newCookie = new HashMap<>(Global.cookies);
                String currentTime = String.valueOf(System.currentTimeMillis());
                String token = new StringBuilder(vpn.password).append(vpn.salt).append(currentTime)
                        .toString();
                newCookie.put("my_domain", "www.baidu.com");
                newCookie.put("my_port", "443");
                newCookie.put("my_time", currentTime);
                newCookie.put("my_token", CommonUtil.MD5(token));
                newCookie.put("my_type", "1");
                newCookie.put("my_username", vpn.username);
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("GET %s HTTP/%s\r\n", vpn.path, vpn.http_version))
                        .append(String.format("Host: %s:%s\r\n", vpn.domain, vpn.port))
//					.append(String.format("Host: %s\r\n", vpn.domain))
                        .append(String.format("User-Agent: %s\r\n", vpn.userAgent))
                        .append("Accept: */*\r\n")
                        .append("Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2\r\n")
                        .append("Sec-WebSocket-Version: 13\r\n").append("Sec-WebSocket-Extensions: permessage-deflate\r\n")
                        .append(String.format("Origin: https://%s\r\n", vpn.domain))
                        .append("Upgrade: websocket\r\n").append("Connection: keep-alive, Upgrade\r\n")
                        .append("Pragma: no-cache\r\n").append("Cache-Control: no-cache\r\n")
                        .append(String.format("Cookie: %s\r\n", BackendAuthHandler.genCookie(newCookie))).append("Sec-WebSocket-Key: ")
                        .append(CommonUtil.getRandomString(24)).append("\r\n\r\n");
                System.out.println(sb.toString());
                out.write(sb.toString().getBytes(StandardCharsets.UTF_8));

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line = reader.readLine();
                if (statusCodePattern == null) {
                    statusCodePattern = Pattern.compile("^HTTP/[0-9.]{1,3} (\\d+) ");
                }
                Matcher m = statusCodePattern.matcher(line);
                if (m.find()) {
                    return Integer.parseInt(m.group(1));
                } else {
                    return -2; // 返回内容不符合预期
                }
            } catch (Exception e) {
                return -3; // 鉴权出了问题
            }

//            while (line != null && !line.isEmpty()) {
//                System.out.println(line);
//                line = reader.readLine();
//            }
        } finally {
            if (socket != null)
                socket.close();
        }
    }

    public static boolean isValid(final String host, final SSLSocket s) {
        try {
            final Certificate[] certs = s.getSession().getPeerCertificates();
            final X509Certificate x509 = (X509Certificate) certs[0];
            for (List<?> entry : x509.getSubjectAlternativeNames()) {
                if ((Integer) entry.get(0) == 2) {
                    String san = entry.get(1).toString();
                    if (san.equals(host))
                        return true;
                    if (san.startsWith("*") && host.endsWith(san.substring(1)))
                        return true;
                }
            }
            System.err.printf("Hostname: %s not match SubjectAlternativeNames: %s\n", host, x509.getSubjectAlternativeNames());
            return false;
        } catch (final Exception ex) {
            return false;
        }
    }


}
