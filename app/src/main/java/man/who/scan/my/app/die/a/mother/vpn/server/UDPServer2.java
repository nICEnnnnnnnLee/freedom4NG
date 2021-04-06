package man.who.scan.my.app.die.a.mother.vpn.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import man.who.scan.my.app.die.a.mother.vpn.LocalVpnService;
import man.who.scan.my.app.die.a.mother.vpn.dns.DnsPacket;
import man.who.scan.my.app.die.a.mother.vpn.dns.Question;
import man.who.scan.my.app.die.a.mother.vpn.ip.CommonMethods;
import man.who.scan.my.app.die.a.mother.vpn.ip.IPHeader;
import man.who.scan.my.app.die.a.mother.vpn.ip.UDPHeader;

// 用于测试UDP报文，没有实际用处
@Deprecated
public class UDPServer2 extends Thread {
    public int port;

    final int MAX_LENGTH = 2048;
    byte[] receMsgs = new byte[MAX_LENGTH];

    DatagramSocket udpSocket;
    DatagramPacket packet;

    volatile boolean isStop = false;

    public static void main(String[] args) throws UnknownHostException, SocketException {
        UDPServer2 uServer = new UDPServer2();
        uServer.start();
    }

    public UDPServer2() {
        try {
            init();
        } catch (Exception e) {

        }
    }

    public void init() throws UnknownHostException, SocketException {
        udpSocket = new DatagramSocket(6668);
        port = udpSocket.getLocalPort();
//        LocalVpnService.Instance.protect(udpSocket);
        packet = new DatagramPacket(receMsgs, receMsgs.length);
        System.out.println("UDP服务器2启动, 端口为: " + port);
    }

    public void cancel() {
        isStop = true;
    }

    @Override
    public void run() {
        while (!isStop) {
            try {

                udpSocket.receive(packet);

                InetSocketAddress addr = (InetSocketAddress) packet.getSocketAddress();

                // 如果消息来自本地, 转发出去
                System.out.println("udp2 收到消息" + packet.getSocketAddress().toString());
//                System.out.println("udp2 收到消息getLength: " + packet.getLength());
////                System.out.println("udp2 data: ");
////                for(int i = 0; i< packet.getLength(); i++){
////                    System.out.print(String.format("0x%02x ", receMsgs[i]));
////                }
////                System.out.println("udp2 data end");
                DnsPacket dnsPacket = DnsPacket.FromBytes(ByteBuffer.wrap(receMsgs, 0, packet.getLength()));
                Question question = dnsPacket.Questions[0];

                System.out.printf("udp2 DNS 查询的地址是%s %s\r\n", question.Domain, CommonMethods.ipBytesToString(dnsPacket.Resources[0].Data));
                udpSocket.send(packet);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 关闭socket
            }
        }
        if (udpSocket != null) {
            udpSocket.close();
        }
    }
}
