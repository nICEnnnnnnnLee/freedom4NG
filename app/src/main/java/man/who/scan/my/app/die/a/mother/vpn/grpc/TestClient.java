package man.who.scan.my.app.die.a.mother.vpn.grpc;

import io.grpc.ChannelCredentials;
import io.grpc.ClientInterceptor;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import io.grpc.stub.StreamObserver;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.model.VPNConfig;
import man.who.scan.my.app.die.a.mother.vpn.util.TrustAllSSLUtil;

import com.google.protobuf.ByteString;

import java.util.concurrent.CountDownLatch;

public class TestClient {

	private final ManagedChannel channel;
	private final FreedomGrpc.FreedomStub stub;
	private final VPNConfig vpnConfig;
	public final CountDownLatch finishLatch;
	public volatile Throwable failed;
	public volatile Boolean valid;

	public TestClient(String host, String port,VPNConfig vpnConfig) {
		this.vpnConfig = vpnConfig;
		ChannelCredentials cred;
		if (!vpnConfig.useSSL) {
			cred = InsecureChannelCredentials.create();
		} else if (vpnConfig.verifySSL) {
			cred = TlsChannelCredentials.create();
		} else {
			cred = TlsChannelCredentials.newBuilder().trustManager(TrustAllSSLUtil.getTrustManager()).build();
		}
		ClientInterceptor interceptor = new HeaderClientInterceptor(host, port, vpnConfig);
		channel = Grpc.newChannelBuilderForAddress(vpnConfig.remoteHost, vpnConfig.remotePort, cred)
				.userAgent(vpnConfig.userAgent).overrideAuthority(vpnConfig.domain).intercept(interceptor).build();
		stub = FreedomGrpc.newStub(channel);
		finishLatch = new CountDownLatch(1);
	}

	public void shutdown() {
		channel.shutdownNow();
	}

	public StreamObserver<Stream.FreedomRequest> connect() {
		StreamObserver<Stream.FreedomResponse> observer = new StreamObserver<Stream.FreedomResponse>() {
			@Override
			public void onNext(Stream.FreedomResponse value) {
//				System.out.println(value.getData().toStringUtf8());
				if(valid == null){
					if(value.getData().toStringUtf8().startsWith("HTTP/1.1 302 Found")){
						valid = true;
					}else {
						failed = new Throwable("收到的消息与预期不符: " + value.getData().toStringUtf8());
						valid = false;
					}
				}
				shutdown();
			}

			@Override
			public void onError(Throwable t) {
//				t.printStackTrace();
				System.out.println(t.getMessage());
				shutdown();
				failed = t;
				finishLatch.countDown();
			}

			@Override
			public void onCompleted() {
				System.out.println("ok");
				shutdown();
				finishLatch.countDown();
			}
		};
		return stub.pipe(observer);
	}

	public static void main(String[] args) throws InterruptedException {
		TestClient client = new TestClient("www.baidu.com", "80", Global.vpnConfig);
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
			Thread.sleep(100000);
			requestStreamObserver.onCompleted();
		} finally {
			client.shutdown();
		}

	}

}
