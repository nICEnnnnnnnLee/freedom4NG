package man.who.scan.my.app.die.a.mother.vpn.server.handler;

import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.vpn.grpc.FreedomGrpc;
import man.who.scan.my.app.die.a.mother.vpn.grpc.HeaderClientInterceptor;
import man.who.scan.my.app.die.a.mother.vpn.grpc.Stream;
import man.who.scan.my.app.die.a.mother.vpn.util.TrustAllSSLUtil;
import man.who.scan.my.app.die.a.mother.vpn.util.socket.MSSLSocketFactory;
import man.who.scan.my.app.die.a.mother.vpn.util.socket.MSocketFactory;

public class GrpcBackendClient {

    private final ManagedChannel channel;
    private final FreedomGrpc.FreedomStub stub;
    private final Channel inboundChannel;

    public GrpcBackendClient(Channel inboundChannel, String host, String port) {
//        ChannelCredentials cred;
//        if (!Global.vpnConfig.useSSL) {
//            cred = InsecureChannelCredentials.create();
//        } else if (Global.vpnConfig.verifySSL) {
//            cred = TlsChannelCredentials.create();
//        } else {
//            cred = TlsChannelCredentials.newBuilder().trustManager(TrustAllSSLUtil.getTrustManager()).build();
//        }
//        channel = Grpc.newChannelBuilderForAddress(Global.vpnConfig.remoteHost, Global.vpnConfig.remotePort, cred).builder();
        ClientInterceptor interceptor = new HeaderClientInterceptor(host, port, Global.vpnConfig);
        OkHttpChannelBuilder builder = OkHttpChannelBuilder.forAddress(
                Global.vpnConfig.remoteHost, Global.vpnConfig.remotePort
        ).socketFactory(MSocketFactory.getDefault()).userAgent(Global.vpnConfig.userAgent)
                .overrideAuthority(Global.vpnConfig.domain).intercept(interceptor);
        if (Global.vpnConfig.useSSL) {
            builder.sslSocketFactory(
                    new MSSLSocketFactory(TrustAllSSLUtil.getSSLContext(Global.vpnConfig.verifySSL).getSocketFactory())
            );
        }
        channel = builder.build();
        stub = FreedomGrpc.newStub(channel);
        this.inboundChannel = inboundChannel;
    }

    public void shutdown() {
        try {
            inboundChannel.close();
            channel.shutdownNow();
        } catch (Exception e) {
        }
    }

    public StreamObserver<Stream.FreedomRequest> connect() {
        StreamObserver<Stream.FreedomResponse> observer = new StreamObserver<Stream.FreedomResponse>() {
            @Override
            public void onNext(Stream.FreedomResponse value) {
//				System.out.println("FreedomResponse from remote:" + value.getData().toStringUtf8());
                ByteBuf newMsg = Unpooled.wrappedBuffer(value.getData().toByteArray());
                inboundChannel.writeAndFlush(newMsg).addListener(future -> {
                    if (!future.isSuccess()) {
                        shutdown();
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
//                System.out.println(t.getMessage());
                shutdown();
            }

            @Override
            public void onCompleted() {
                shutdown();
            }
        };
        return stub.pipe(observer);
    }

//        try {
//            for (InternalInstrumented<InternalChannelz.SocketStats> otherSocket :
//                    getSocketMap().values()) {
//                Socket socket = getSocketFrom(otherSocket);
//                if (socket == null) {
//                    System.err.println("socket is null");
//                } else {
//                    LocalVpnService.Instance.protect(socket);
//                    System.err.println("socket protected");
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//    static Field field1, field2, field3, field4;
//    static ConcurrentMap<Long, InternalInstrumented<InternalChannelz.SocketStats>> otherSockets;
//
//    public static Socket getSocketFrom(InternalInstrumented<InternalChannelz.SocketStats> otherSocket) throws NoSuchFieldException, IllegalAccessException {
//        Object obj = getField1(otherSocket).get(otherSocket);
////        System.out.println("obj1: " + obj);
//        obj = getField2(obj).get(obj);
////        System.out.println("obj2: " + obj);
//        obj = getField3(obj).get(obj);
////        System.out.println("obj3: " + obj);
//        if (obj == null) {
//            return null;
//        } else if (!(obj instanceof Socket)) {
//            obj = getField4(obj).get(obj);
//            System.out.println("obj4: " + obj);
//        }
//        return ((Socket) obj);
//    }
//
//    static Field getField1(Object obj) throws NoSuchFieldException {
//        if (field1 == null) {
//            field1 = obj.getClass().getDeclaredField("delegate");
//            field1.setAccessible(true);
//        }
//        return field1;
//    }
//
//    static Field getField2(Object obj) throws NoSuchFieldException {
//        if (field2 == null) {
//            field2 = obj.getClass().getDeclaredField("delegate");
//            field2.setAccessible(true);
//        }
//        return field2;
//    }
//
//    static Field getField3(Object obj) throws NoSuchFieldException {
//        if (field3 == null) {
//            field3 = obj.getClass().getDeclaredField("socket");
//            field3.setAccessible(true);
//        }
//        return field3;
//    }
//
//    static Field getField4(Object obj) throws NoSuchFieldException {
//        if (field4 == null) {
//            field4 = obj.getClass().getDeclaredField("socket");
//            field4.setAccessible(true);
//        }
//        return field4;
//    }
//
//    public static ConcurrentMap<Long, InternalInstrumented<InternalChannelz.SocketStats>> getSocketMap() {
//        if (otherSockets == null) {
//            try {
//                InternalChannelz channels = InternalChannelz.instance();
//                Field otherSocketsField = InternalChannelz.class.getDeclaredField("otherSockets");
//                otherSocketsField.setAccessible(true);
//                otherSockets = (ConcurrentMap<Long, InternalInstrumented<InternalChannelz.SocketStats>>)
//                        otherSocketsField.get(channels);
//            } catch (IllegalAccessException | NoSuchFieldException e) {
//            }
//        }
//        return otherSockets;
//    }
//
//    private Object getFiledOfObject(String filed, Object object) throws NoSuchFieldException, IllegalAccessException {
//        Field f = object.getClass().getDeclaredField(filed);
//        f.setAccessible(true);
//        return f.get(object);
//    }
}
