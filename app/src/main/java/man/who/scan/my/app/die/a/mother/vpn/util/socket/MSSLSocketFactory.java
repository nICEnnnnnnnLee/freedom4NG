package man.who.scan.my.app.die.a.mother.vpn.util.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

import man.who.scan.my.app.die.a.mother.vpn.LocalVpnService;

public class MSSLSocketFactory extends SSLSocketFactory {

    final SSLSocketFactory socketFactory;

    public MSSLSocketFactory(SSLSocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return socketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return socketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String sni, int port, boolean b) throws IOException {
//        if (LocalVpnService.Instance != null){
//            boolean r = LocalVpnService.Instance.protect(socket);
//        }
        Socket socket1 = socketFactory.createSocket(socket, sni, port, b);
        return socket1;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket socket = MSocketFactory.getDefault().createSocket(host, port);
        return this.createSocket(socket, null, port, true);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException, UnknownHostException {
        Socket socket = MSocketFactory.getDefault().createSocket(host, port, localAddress, localPort);
        return this.createSocket(socket, null, port, true);
    }

    @Override
    public Socket createSocket(InetAddress hostAddress, int port) throws IOException {
        Socket socket = MSocketFactory.getDefault().createSocket(hostAddress, port);
        return this.createSocket(socket, null, port, true);
    }

    @Override
    public Socket createSocket(InetAddress hostAddress, int port, InetAddress localAddress, int localPort) throws IOException {
        Socket socket = MSocketFactory.getDefault().createSocket(hostAddress, port, localAddress, localPort);
        return this.createSocket(socket, null, port, true);
    }
}
