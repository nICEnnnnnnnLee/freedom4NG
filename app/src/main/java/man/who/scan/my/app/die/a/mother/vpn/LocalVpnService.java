package man.who.scan.my.app.die.a.mother.vpn;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import androidx.core.app.NotificationCompat;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.R;
import man.who.scan.my.app.die.a.mother.model.BaseConfig;
import man.who.scan.my.app.die.a.mother.ui.base.ToastHandler;
import man.who.scan.my.app.die.a.mother.vpn.ip.CommonMethods;
import man.who.scan.my.app.die.a.mother.vpn.ip.IPHeader;
import man.who.scan.my.app.die.a.mother.vpn.ip.TCPHeader;
import man.who.scan.my.app.die.a.mother.vpn.ip.UDPHeader;
import man.who.scan.my.app.die.a.mother.vpn.server.TCPServer;
import man.who.scan.my.app.die.a.mother.vpn.server.UDPServer;
import man.who.scan.my.app.die.a.mother.vpn.server.nat.NATSession;
import man.who.scan.my.app.die.a.mother.vpn.server.nat.NATSessionManager;
import man.who.scan.my.app.die.a.mother.vpn.util.AppManagerUtil;
import man.who.scan.my.app.die.a.mother.vpn.util.DNSUtil;
import man.who.scan.my.app.die.a.mother.vpn.util.EngineRhino;

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
    public EngineRhino pac;
    TCPServer tcpServer;
    UDPServer udpServer;
//    UDPServer2 udpServer2;

    NotificationCompat.Builder notificationBuilder;
    NotificationManager notificationManager;
    private static final int NotificationID = 0x1314;
    private static final String NotificationTAG = "freedom";
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
        pac = null;
        Global.hostTableRuntime = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        notificationBuilder.setOngoing(false);
//        notificationManager.notify(NotificationTAG, NotificationID, notificationBuilder.build());
        notificationManager.cancel(NotificationTAG, NotificationID);
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

        try {
            switch (Global.vpnGlobalConfig.mode){
                case BaseConfig.MODE_WHITE_LIST:
                    builder.addAllowedApplication(this.getPackageName());
                    for(String pkg: Global.vpnGlobalConfig.whitelist){
                        builder.addAllowedApplication(pkg);
                    }
                    break;
                case BaseConfig.MODE_BLACK_LIST:
                    for(String pkg: Global.vpnGlobalConfig.blacklist){
                        builder.addDisallowedApplication(pkg);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\n\n\n\n\n\n\n创建黑白名单时出现错误!!!!!!!!!!!!!!!!!!!!!!!!!\n\n\n\n\n\n\n\n\n\n");
        }
        if (Global.dnsConfig.useCunstomDNS && !Global.dnsConfig.dns1.isEmpty()) {
            builder.addDnsServer(Global.dnsConfig.dns1);
            // 在builder.addRoute("0.0.0.0", 0);的情况下没必要再加重复路由
            //builder.addRoute(Global.dnsConfig.dns1, 32);
        } else {
            for (String dns : DNSUtil.defaultDNS(this)) {
                builder.addDnsServer(dns);
                // 在builder.addRoute("0.0.0.0", 0);的情况下没必要再加重复路由
                //builder.addRoute(dns, 32);
            }
        }
        try{
            if(Global.vpnConfig.usePAC){
                File pacFile = new File(Global.vpnConfig.pacPath);
                InputStreamReader isr = null;
                if(pacFile.exists())
                    isr = new InputStreamReader(new FileInputStream(pacFile));
                else{
                    if(Global.vpnConfig.pacPath.length()>1)// * or empty is designed for default
                        ToastHandler.show(this, "PAC path is not right.");
                    isr = new InputStreamReader(this.getResources().openRawResource(R.raw.gfw_pac));
                }

                pac = new EngineRhino(isr);
                Global.hostTableRuntime = new ConcurrentHashMap<>();
            }
        }catch (Exception e){
            ToastHandler.show(this, "PAC file parse error.");
            pac = null;
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
        notificationBuilder = new NotificationCompat.Builder(this, this.getPackageName());
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
        notificationBuilder.setContentTitle("Freedom");
        notificationBuilder.setContentText("运行中");
        notificationBuilder.setOngoing(true);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(this.getPackageName(), NotificationTAG, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(NotificationTAG, NotificationID, notificationBuilder.build());
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