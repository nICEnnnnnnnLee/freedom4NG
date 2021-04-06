package man.who.scan.my.app.die.a.mother.vpn;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.vpn.ip.CommonMethods;
import man.who.scan.my.app.die.a.mother.vpn.ip.IPHeader;
import man.who.scan.my.app.die.a.mother.vpn.ip.TCPHeader;
import man.who.scan.my.app.die.a.mother.vpn.ip.UDPHeader;
import man.who.scan.my.app.die.a.mother.vpn.server.TCPServer;
import man.who.scan.my.app.die.a.mother.vpn.server.UDPServer;
import man.who.scan.my.app.die.a.mother.vpn.server.nat.NATSession;
import man.who.scan.my.app.die.a.mother.vpn.server.nat.NATSessionManager;
import man.who.scan.my.app.die.a.mother.vpn.util.DNSUtil;

public class LocalVpnService extends VpnService implements Runnable {

    public static LocalVpnService Instance;
    public ParcelFileDescriptor fileDescriptor;
    public FileInputStream vpnInput;
    public FileOutputStream vpnOutput;

    //收到的IP报文Buffer
    byte[] m_Packet;

    //方便解析
    IPHeader m_IPHeader;
    TCPHeader m_TCPHeader;
    UDPHeader m_UDPHeader;
    ByteBuffer m_DNSBuffer;

    public String localIP = "168.168.168.168";
    public int intLocalIP = CommonMethods.ipStringToInt(localIP);
    public String uniqueIp = "222.222.222.222";
    public int intUniqueIp = CommonMethods.ipStringToInt(uniqueIp);
    TCPServer tcpServer;
    UDPServer udpServer;
//    UDPServer2 udpServer2;

    boolean isClosed = false;

    public void stopVPN() {
        if (isClosed)
            return;

//        System.out.println("销毁程序调用中...");
        udpServer.cancel();
        tcpServer.cancel();
//        udpServer2.cancel();
        try {
            vpnInput.close();
            vpnOutput.close();
            fileDescriptor.close();
            fileDescriptor = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopSelf();
        isClosed = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isClosed = false;
        m_Packet = new byte[20000];
        m_IPHeader = new IPHeader(m_Packet, 0);
        m_TCPHeader = new TCPHeader(m_Packet, 20);
        m_UDPHeader = new UDPHeader(m_Packet, 20);
        m_DNSBuffer = ((ByteBuffer) ByteBuffer.wrap(m_Packet).position(28)).slice();

        Builder builder = new Builder();
        //builder.setMtu(...);
        builder.addAddress(localIP, 32);
        builder.setSession("Freedom");
        builder.addRoute("0.0.0.0", 0);
        if (Global.dnsConfig.useCunstomDNS && !Global.dnsConfig.dns1.isEmpty()) {
            builder.addDnsServer(Global.dnsConfig.dns1);
        } else {
            for (String dns : DNSUtil.defaultDNS(this)) {
                builder.addDnsServer(dns);
            }
        }
        fileDescriptor = builder.establish();
        vpnInput = new FileInputStream(fileDescriptor.getFileDescriptor());
        vpnOutput = new FileOutputStream(fileDescriptor.getFileDescriptor());

        Instance = this;
        tcpServer = new TCPServer();
        udpServer = new UDPServer();
//        udpServer2 = new UDPServer2();
        Thread th = new Thread(this);
        th.setName("VPN Service - Thread");
        th.start();

        tcpServer.start();
        udpServer.start();
//        udpServer2.start();
    }

    @Override
    public void run() {
        int size;
        try {
//            System.out.println("读取报文中!!!!!!!!!!!!!!!!!!!!!!!!!");
            while ((size = vpnInput.read(m_Packet)) >= 0) {
                if (isClosed) {
                    vpnInput.close();
                    vpnOutput.close();
                    throw new Exception("LocalServer stopped.");
                }
                if (size == 0) {
                    continue;
                }
                //System.out.println("读取报文中!!!!!!!!!!!!!!!!!!!!!!!!!");
                onIPPacketReceived(m_IPHeader, size);
            }
        } catch (Exception e) {
            //e.printStackTrace();
//            System.out.println("接收报文出现错误!!!!!!!!!!!!!!!!!!!!!!!!!");
        } finally {
            stopVPN();
        }

    }

    void onIPPacketReceived(IPHeader ipHeader, int size) throws IOException {
        switch (ipHeader.getProtocol()) {
            case IPHeader.TCP:
                TCPHeader tcpHeader = m_TCPHeader;
                tcpHeader.m_Offset = ipHeader.getHeaderLength();
                if (ipHeader.getDestinationIP() == intUniqueIp) {
                    //来自TCP服务器
                    //System.out.println("来自TCP服务器: port: " + tcpHeader.getDestinationPort());
                    int portKey = tcpHeader.getDestinationPort();
                    portKey = portKey > 0 ? portKey : portKey + 65536;
                    NATSession session = NATSessionManager.getSession("tcp", portKey);
//                    System.out.println("Request Session: key Port: "+ portKey);
                    if (session != null) {
                        ipHeader.setSourceIP(session.RemoteIP);
                        tcpHeader.setSourcePort(session.RemotePort);
                        ipHeader.setDestinationIP(intLocalIP);
                        CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                        vpnOutput.write(ipHeader.m_Data, ipHeader.m_Offset, size);
                    } else {
                        System.out.printf("NoSession: %s %s\n", ipHeader.toString(), tcpHeader.toString());
                    }
                } else {
                    //来自本地
                    // 添加端口映射
//                    System.out.println("来自本地的TCP报文: port: " + tcpHeader.getDestinationPort());
//                    System.out.println("来自本地的TCP报文: dstIP: " + CommonMethods.ipIntToString(ipHeader.getDestinationIP()));
                    int portKey = tcpHeader.getSourcePort();
                    portKey = portKey > 0 ? portKey : portKey + 65536;
                    int dstIP = ipHeader.getDestinationIP();
                    short dstPort = tcpHeader.getDestinationPort();
                    NATSession session = NATSessionManager.getSession("tcp", portKey);
                    if (session == null || session.RemoteIP != dstIP || session.RemotePort != dstPort) {
                        session = NATSessionManager.createSession("tcp", portKey, dstIP, dstPort);
                    }
                    ipHeader.setSourceIP(intUniqueIp);
                    ipHeader.setDestinationIP(intLocalIP);
                    tcpHeader.setDestinationPort((short) Global.vpnConfig.localPort);
                    CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                    vpnOutput.write(ipHeader.m_Data, ipHeader.m_Offset, size);
                }
                break;
            case IPHeader.UDP:

                UDPHeader udpHeader = m_UDPHeader;
                udpHeader.m_Offset = ipHeader.getHeaderLength();
                if (ipHeader.getDestinationIP() != intUniqueIp) {
//                    System.out.println("LocalVpnService: 本地UDP信息转发给UDP Server");
                    int portKey = udpHeader.getSourcePort();
                    portKey = portKey > 0 ? portKey : portKey + 65536;
                    int dstIP = ipHeader.getDestinationIP();
                    short dstPort = udpHeader.getDestinationPort();
                    NATSession session = NATSessionManager.getSession("udp", portKey);
                    if (session == null || session.RemoteIP != dstIP || session.RemotePort != dstPort) {
                        NATSessionManager.createSession("udp", portKey, dstIP, dstPort);
                    }
                    ipHeader.setSourceIP(CommonMethods.ipStringToInt(uniqueIp));
                    //udpHeader.setSourcePort(originPort);
                    ipHeader.setDestinationIP(intLocalIP);
                    udpHeader.setDestinationPort((short) udpServer.port);

                    ipHeader.setProtocol(IPHeader.UDP);
                    CommonMethods.ComputeUDPChecksum(ipHeader, udpHeader);
                    vpnOutput.write(ipHeader.m_Data, ipHeader.m_Offset, ipHeader.getTotalLength());
                } else {
//                    System.out.println("LocalVpnService: UDP Server 消息, 回复给客户端");
                    int portKey = udpHeader.getDestinationPort();
                    portKey = portKey > 0 ? portKey : portKey + 65536;
                    NATSession session = NATSessionManager.getSession("udp", portKey);
                    if (session != null) {
                        ipHeader.setSourceIP(session.RemoteIP);
                        udpHeader.setSourcePort(session.RemotePort);
                        ipHeader.setDestinationIP(intLocalIP);
                        CommonMethods.ComputeUDPChecksum(ipHeader, udpHeader);
                        vpnOutput.write(ipHeader.m_Data, ipHeader.m_Offset, size);
                    } else {
                        System.out.printf("No UDP Session: %s %s\n", ipHeader.toString(), udpHeader.toString());
                    }
                }
                break;
            default:
                //vpnOutput.write(ipHeader.m_Data, ipHeader.m_Offset, size);
                break;
        }
    }
}