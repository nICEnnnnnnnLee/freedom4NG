package man.who.scan.my.app.die.a.mother.vpn.grpc;

import java.util.HashMap;
import java.util.StringJoiner;
import java.util.Map.Entry;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.model.VPNConfig;
import man.who.scan.my.app.die.a.mother.vpn.util.CommonUtil;

public class HeaderClientInterceptor implements ClientInterceptor {

	static final Metadata.Key<String> CUSTOM_HEADER_KEY = Metadata.Key.of("cookie", Metadata.ASCII_STRING_MARSHALLER);

	final String host;
	final String port;
	final VPNConfig vpnConfig;
	public HeaderClientInterceptor(String host, String port, VPNConfig vpnConfig) {
		this.host = host;
		this.port = port;
		this.vpnConfig = vpnConfig;
	}

	public String genCookie() {
		HashMap<String, String> newCookie = new HashMap<>(Global.cookies);
		String currentTime = String.valueOf(System.currentTimeMillis());
		String token = new StringBuilder(vpnConfig.password).append(vpnConfig.salt).append(currentTime).toString();
		newCookie.put("my_domain", host);
		newCookie.put("my_port", port);
		newCookie.put("my_time", currentTime);
		newCookie.put("my_token", CommonUtil.MD5(token));
		StringJoiner sj = new StringJoiner("; ");
		for (Entry<String, String> entry : newCookie.entrySet()) {
			sj.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
		}
		return sj.toString();
	}

	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
			CallOptions callOptions, Channel next) {
		return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

			@Override
			public void start(Listener<RespT> responseListener, Metadata headers) {
				headers.put(CUSTOM_HEADER_KEY, genCookie());
				super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {
					@Override
					public void onHeaders(Metadata headers) {
						super.onHeaders(headers);
					}
				}, headers);
			}
		};
	}
}