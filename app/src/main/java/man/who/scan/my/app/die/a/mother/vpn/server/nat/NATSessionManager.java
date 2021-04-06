package man.who.scan.my.app.die.a.mother.vpn.server.nat;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import man.who.scan.my.app.die.a.mother.vpn.util.CommonUtil;

public class NATSessionManager {
	static final int MAX_SESSION_COUNT = 60;
	static final long SESSION_TIMEOUT_NS = 60 * 1000000000L;
	static final ConcurrentHashMap<Integer, NATSession> TCPSessions = new ConcurrentHashMap<>();
	static final ConcurrentHashMap<NATSession, Integer> TCPNATSessions = new ConcurrentHashMap<>();
	static final ConcurrentHashMap<Integer, NATSession> UDPSessions = new ConcurrentHashMap<>();
	static final ConcurrentHashMap<NATSession, Integer> UDPNATSessions = new ConcurrentHashMap<>();

	public static Integer getPort(String protocal, NATSession socket) {
		if ("tcp".equalsIgnoreCase(protocal))
			return TCPNATSessions.get(socket);
		else
			return UDPNATSessions.get(socket);
	}

	public static NATSession getSession(String protocal, int portKey) {
		NATSession session = null;
		if ("tcp".equalsIgnoreCase(protocal))
			session = TCPSessions.get(portKey);
		else
			session = UDPSessions.get(portKey);
		if (session != null) {
			session.LastNanoTime = System.nanoTime();
		}
		return session;
	}

	public static int getSessionCount(String protocal) {
		if ("tcp".equalsIgnoreCase(protocal))
			return TCPNATSessions.size();
		else
			return UDPNATSessions.size();
	}

	static void clearExpiredSessions(String protocal) {
		long now = System.nanoTime();
		if ("tcp".equalsIgnoreCase(protocal)) {
			for (Entry<Integer, NATSession> entry : TCPSessions.entrySet()) {
				NATSession session = entry.getValue();
				if (now - session.LastNanoTime > SESSION_TIMEOUT_NS) {
					TCPSessions.remove(entry.getKey());
					TCPNATSessions.remove(entry.getValue());
				}
			}
		} else {
			for (Entry<Integer, NATSession> entry : UDPSessions.entrySet()) {
				NATSession session = entry.getValue();
				if (now - session.LastNanoTime > SESSION_TIMEOUT_NS) {
					UDPSessions.remove(entry.getKey());
					UDPNATSessions.remove(entry.getValue());
				}
			}
		}
	}

	public static NATSession createSession(String protocal, int portKey, int remoteIP, short remotePort) {
		if (getSessionCount(protocal) > MAX_SESSION_COUNT) {
			clearExpiredSessions(protocal);// 清理过期的会话。
		}

		NATSession session = new NATSession();
		session.LastNanoTime = System.nanoTime();
		session.RemoteIP = remoteIP;
		session.RemotePort = remotePort;

		if (session.RemoteHost == null) {
			session.RemoteHost = CommonUtil.ipIntToString(remoteIP);
		}
		if ("tcp".equalsIgnoreCase(protocal)) {
			TCPSessions.put(portKey, session);
			TCPNATSessions.put(session, portKey);
		} else {
			UDPSessions.put(portKey, session);
			UDPNATSessions.put(session, portKey);
		}
		return session;
	}
}
