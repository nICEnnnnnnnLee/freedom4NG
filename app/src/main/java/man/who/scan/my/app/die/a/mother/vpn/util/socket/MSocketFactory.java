package man.who.scan.my.app.die.a.mother.vpn.util.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import man.who.scan.my.app.die.a.mother.vpn.LocalVpnService;

public class MSocketFactory extends SocketFactory {
    static MSocketFactory mSocketFactory;

    public static SocketFactory getDefault() {
        if (mSocketFactory == null) {
            mSocketFactory = new MSocketFactory();
        }
        return mSocketFactory;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket socket = new Socket();
        socket.bind(new InetSocketAddress(0));
        if (LocalVpnService.Instance != null) {
            LocalVpnService.Instance.protect(socket);
        }
        socket.connect(new InetSocketAddress(host, port));
        return socket;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException, UnknownHostException {
        Socket socket = new Socket();
        socket.bind(new InetSocketAddress(localAddress, localPort));
        if (LocalVpnService.Instance != null) {
            LocalVpnService.Instance.protect(socket);
        }
        socket.connect(new InetSocketAddress(host, port));
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress hostAddress, int port) throws IOException {
        Socket socket = new Socket();
        socket.bind(new InetSocketAddress(0));
        if (LocalVpnService.Instance != null) {
            LocalVpnService.Instance.protect(socket);
        }
        socket.connect(new InetSocketAddress(hostAddress, port));
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress hostAddress, int port, InetAddress localAddress, int localPort) throws IOException {
        Socket socket = new Socket();
        socket.bind(new InetSocketAddress(localAddress, localPort));
        if (LocalVpnService.Instance != null) {
            LocalVpnService.Instance.protect(socket);
        }
        socket.connect(new InetSocketAddress(hostAddress, port));
        return socket;
    }
}
