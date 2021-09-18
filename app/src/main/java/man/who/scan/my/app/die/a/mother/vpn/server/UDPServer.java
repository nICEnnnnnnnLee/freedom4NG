package man.who.scan.my.app.die.a.mother.vpn.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.vpn.LocalVpnService;
import man.who.scan.my.app.die.a.mother.vpn.dns.DnsPacket;
import man.who.scan.my.app.die.a.mother.vpn.dns.Question;
import man.who.scan.my.app.die.a.mother.vpn.dns.ResourcePointer;
import man.who.scan.my.app.die.a.mother.vpn.ip.CommonMethods;
import man.who.scan.my.app.die.a.mother.vpn.server.nat.NATSession;
import man.who.scan.my.app.die.a.mother.vpn.server.nat.NATSessionManager;

public class UDPServer extends Thread {
    public int port;
    public int sendPort;

    final int MAX_LENGTH = 2048;
    byte[] receMsgs = new byte[MAX_LENGTH];

    DatagramSocket udpSocket;
    DatagramSocket udpSocketSend;
    DatagramPacket packet;

    volatile boolean isStop = false;

    DoH doh;

    public static void main(String[] args) throws UnknownHostException, SocketException {
        UDPServer uServer = new UDPServer();
        uServer.start();
    }

    public UDPServer() {
        try {
            init();
        } catch (Exception e) {

        }
    }

    public void init() throws UnknownHostException, SocketException {
        udpSocket = new DatagramSocket();
        port = udpSocket.getLocalPort();
        udpSocketSend = new DatagramSocket();
        sendPort = udpSocketSend.getPort();
        LocalVpnService.Instance.protect(udpSocket);
        packet = new DatagramPacket(receMsgs, 28, receMsgs.length - 28);
        if (Global.dnsConfig.useCunstomDNS && Global.dnsConfig.useDoH) {
            doh = new DoH(Global.dnsConfig.dohDomain, Global.dnsConfig.dohHost, Global.dnsConfig.dohPath);
            doh.init();
        }
//        System.out.println("UDP服务器启动, 端口为: " + port);
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
                if (LocalVpnService.Instance.uniqueIp.equals(addr.getHostString())) {
//                    System.out.println("收到本地UDP消息" + packet.getSocketAddress().toString());
                    dealPacketFromLocal();
                    continue;
                } else {
//                    System.out.println("收到外部UDP消息" + packet.getSocketAddress().toString());
                    // 如果消息来自外部, 转进来
                    NATSession session = new NATSession();
                    session.RemoteIP = CommonMethods.ipStringToInt(addr.getHostString());
                    session.RemotePort = (short) packet.getPort();
                    Integer portKey = NATSessionManager.getPort("udp", session);
                    if (portKey == null) {
                        continue;
                    }

                    DatagramPacket sendPacket = new DatagramPacket(receMsgs, 28, packet.getLength(),
                            new InetSocketAddress(LocalVpnService.Instance.uniqueIp, portKey));
//                    udpSocket.send(sendPacket);
                    synchronized (udpSocketSend) {
                        udpSocketSend.send(sendPacket);
                    }
//                    IPHeader ipHeader = new IPHeader(receMsgs, 0);
//                    ipHeader.Default();
//                    ipHeader.setDestinationIP(LocalVpnService.Instance.intLocalIP);
//                    ipHeader.setSourceIP(session.RemoteIP);
//                    ipHeader.setTotalLength(20 + 8 + packet.getLength());
//                    ipHeader.setHeaderLength(20);
//                    ipHeader.setProtocol(IPHeader.UDP);
//                    ipHeader.setTTL((byte)30);
//
//                    UDPHeader udpHeader = new UDPHeader(receMsgs, 20);
//                    udpHeader.setDestinationPort((short)(int)portKey);
//                    udpHeader.setSourcePort(session.RemotePort);
//                    udpHeader.setTotalLength(8 + packet.getLength());
//
//                    CommonMethods.ComputeUDPChecksum(ipHeader, udpHeader);
//                    LocalVpnService.Instance.vpnOutput.write(ipHeader.m_Data, ipHeader.m_Offset, ipHeader.getTotalLength());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        udpSocketSend.close();
        udpSocket.close();
        if (doh != null)
            doh.stop();
    }

    private void dealPacketFromLocal() throws IOException {
        NATSession session = NATSessionManager.getSession("udp", packet.getPort());
        if (session == null) {
            System.out.println("UDP server 没有找到相关UDP session" + packet.getSocketAddress().toString());
            return;
        }

        if (session.RemotePort == 53) {
            if (Global.dnsConfig.useHost) {
                // 解析DNS 报文
                DnsPacket dnsPacket = DnsPacket.FromBytes(ByteBuffer.wrap(receMsgs, 28, packet.getLength()));
                Question question = dnsPacket.Questions[0];
                String domain = question.Domain;
                String ipAddr = Global.hostConfig.get(domain);
                if(ipAddr == null && Global.hostTableRuntime != null){
                    // 这个host表是PAC解析的时候必须要维护的，范围更大。
                    ipAddr = Global.hostTableRuntime.get(domain);
                }
                // 如果查询的域名在host当中，直接构造回复
                if (ipAddr != null) {
                    dnsPacket.Header.setResourceCount((short) 1);
                    dnsPacket.Header.setAResourceCount((short) 0);
                    dnsPacket.Header.setEResourceCount((short) 0);
                    ResourcePointer rPointer = new ResourcePointer(receMsgs, question.Offset() + question.Length());
                    rPointer.setDomain((short) 0xC00C);
                    rPointer.setType(question.Type);
                    rPointer.setClass(question.Class);
                    rPointer.setTTL(300);
                    rPointer.setDataLength((short) 4);
                    rPointer.setIP(CommonMethods.ipStringToInt(ipAddr));
                    dnsPacket.Size = 12 + question.Length() + 16;
                    DatagramPacket sendPacket = new DatagramPacket(receMsgs, 28, dnsPacket.Size,
                            new InetSocketAddress(LocalVpnService.Instance.uniqueIp, packet.getPort()));//
                    synchronized (udpSocketSend) {
                        udpSocketSend.send(sendPacket);
                    }
                    return;
                }
            }

            if (doh != null) {
                // 如果使用DoH功能，转DoH
                doh.dealDNSRequest(udpSocketSend, packet.getPort(), session, packet, receMsgs);
                return;
            }
            // 其它情况直接转发给外部即可
        }
//                        System.out.println("UDP server 转发本地udp消息到" + CommonMethods.ipIntToInet4Address(session.RemoteIP) + ":" + (int) session.RemotePort);
        DatagramPacket sendPacket = new DatagramPacket(receMsgs, 28, packet.getLength(),
                CommonMethods.ipIntToInet4Address(session.RemoteIP), (int) session.RemotePort);
        udpSocket.send(sendPacket);
    }
}
