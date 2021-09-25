package man.who.scan.my.app.die.a.mother.vpn.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DNSUtil {

	public static List<String> defaultDNS(Context context){
		ConnectivityManager mConnectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = mConnectivity.getActiveNetworkInfo();

		int netType = info.getType();
		int netSubtype = info.getSubtype();
		List<String> dnsList = null;
		if (netType == ConnectivityManager.TYPE_WIFI) {  //WIFI
//			System.out.println("当前通过Wifi 上网");
			dnsList = getWifiNetInfo(context);
		} else if (netType == ConnectivityManager.TYPE_MOBILE) {   //MOBILE
//			System.out.println("当前通过流量 上网");
			dnsList = getLocalDNS();
		}
		if(dnsList.isEmpty()){
			dnsList.add("114.114.114.114");
		}
//		for(String dns : dnsList){
//			System.out.println("DNS 服务器: " + dns);
//		}
		return dnsList;

	}
	final static Pattern patternDNS = Pattern.compile(":[ ]*\\[([0-9]+.[0-9]+.[0-9]+.[0-9]+)\\]");
	public static List<String> getLocalDNS(){
		Process cmdProcess = null;
		BufferedReader reader = null;
		List<String> dnsList = new ArrayList<>();
		try {
			String[] cmd = new String[]{"sh","-c","getprop | grep dns"};
			cmdProcess = Runtime.getRuntime().exec(cmd);
			reader = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));
			String dnsIP = reader.readLine();
			while(dnsIP != null){
				Matcher matcher = patternDNS.matcher(dnsIP);
				if(matcher.find()){
					dnsList.add(matcher.group(1));
				}
				dnsIP = reader.readLine();
			}
			return dnsList;
		} catch (IOException e) {
			e.printStackTrace();
			return dnsList;
		} finally{
			try {
				reader.close();
			} catch (IOException e) {
			}
			cmdProcess.destroy();
		}
	}
	public static List<String> getWifiNetInfo(Context context){
		List<String> dnsList = new ArrayList<>();
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if(wifi  != null){
			DhcpInfo info = wifi.getDhcpInfo();
			String dns = intToIp(info.dns1);
			if(!"0.0.0.0".equals(dns)){
				dnsList.add(dns);
			}
		}
		return dnsList;
	}

	public static String intToIp(int addr) {
		return  ((addr & 0xFF) + "." +
				((addr >>>= 8) & 0xFF) + "." +
				((addr >>>= 8) & 0xFF) + "." +
				((addr >>>= 8) & 0xFF));
	}
}
