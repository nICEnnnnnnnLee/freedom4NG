package man.who.scan.my.app.die.a.mother.vpn.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * 定义服务
 * </pre>
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 1.51.0)",
        comments = "Source: stream.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class FreedomGrpc {

    private FreedomGrpc() {
    }

    public static String SERVICE_NAME = "freedomGo.grpc.Freedom";

    // Static method descriptors that strictly reflect the proto.
    private static volatile io.grpc.MethodDescriptor<Stream.FreedomRequest,
            Stream.FreedomResponse> getPipeMethod;

    public static void resetPipeMethod() {
        getPipeMethod = null;
    }

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = "freedomGo.grpc.Freedom/Pipe",
            requestType = Stream.FreedomRequest.class,
            responseType = Stream.FreedomResponse.class,
            methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
    public static io.grpc.MethodDescriptor<Stream.FreedomRequest,
            Stream.FreedomResponse> getPipeMethod() {
        io.grpc.MethodDescriptor<Stream.FreedomRequest, Stream.FreedomResponse> getPipeMethod;
        if ((getPipeMethod = FreedomGrpc.getPipeMethod) == null) {
            synchronized (FreedomGrpc.class) {
                if ((getPipeMethod = FreedomGrpc.getPipeMethod) == null) {
                    FreedomGrpc.getPipeMethod = getPipeMethod =
                            io.grpc.MethodDescriptor.<Stream.FreedomRequest, Stream.FreedomResponse>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Pipe"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                                            Stream.FreedomRequest.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                                            Stream.FreedomResponse.getDefaultInstance()))
                                    .build();
                }
            }
        }
        return getPipeMethod;
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static FreedomStub newStub(io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<FreedomStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<FreedomStub>() {
                    @Override
                    public FreedomStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new FreedomStub(channel, callOptions);
                    }
                };
        return FreedomStub.newStub(factory, channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static FreedomBlockingStub newBlockingStub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<FreedomBlockingStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<FreedomBlockingStub>() {
                    @Override
                    public FreedomBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new FreedomBlockingStub(channel, callOptions);
                    }
                };
        return FreedomBlockingStub.newStub(factory, channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static FreedomFutureStub newFutureStub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<FreedomFutureStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<FreedomFutureStub>() {
                    @Override
                    public FreedomFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new FreedomFutureStub(channel, callOptions);
                    }
                };
        return FreedomFutureStub.newStub(factory, channel);
    }

    /**
     * <pre>
     * 定义服务
     * </pre>
     */
    public static abstract class FreedomImplBase implements io.grpc.BindableService {

        /**
         *
         */
        public io.grpc.stub.StreamObserver<Stream.FreedomRequest> pipe(
                io.grpc.stub.StreamObserver<Stream.FreedomResponse> responseObserver) {
            return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getPipeMethod(), responseObserver);
        }

        @Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            getPipeMethod(),
                            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
                                    new MethodHandlers<
                                            Stream.FreedomRequest,
                                            Stream.FreedomResponse>(
                                            this, METHODID_PIPE)))
                    .build();
        }
    }

    /**
     * <pre>
     * 定义服务
     * </pre>
     */
    public static final class FreedomStub extends io.grpc.stub.AbstractAsyncStub<FreedomStub> {
        private FreedomStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected FreedomStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new FreedomStub(channel, callOptions);
        }

        /**
         *
         */
        public io.grpc.stub.StreamObserver<Stream.FreedomRequest> pipe(
                io.grpc.stub.StreamObserver<Stream.FreedomResponse> responseObserver) {
            return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
                    getChannel().newCall(getPipeMethod(), getCallOptions()), responseObserver);
        }
    }

    /**
     * <pre>
     * 定义服务
     * </pre>
     */
    public static final class FreedomBlockingStub extends io.grpc.stub.AbstractBlockingStub<FreedomBlockingStub> {
        private FreedomBlockingStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected FreedomBlockingStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new FreedomBlockingStub(channel, callOptions);
        }
    }

    /**
     * <pre>
     * 定义服务
     * </pre>
     */
    public static final class FreedomFutureStub extends io.grpc.stub.AbstractFutureStub<FreedomFutureStub> {
        private FreedomFutureStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected FreedomFutureStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new FreedomFutureStub(channel, callOptions);
        }
    }

    private static final int METHODID_PIPE = 0;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final FreedomImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(FreedomImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                default:
                    throw new AssertionError();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(
                io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_PIPE:
                    return (io.grpc.stub.StreamObserver<Req>) serviceImpl.pipe(
                            (io.grpc.stub.StreamObserver<Stream.FreedomResponse>) responseObserver);
                default:
                    throw new AssertionError();
            }
        }
    }

    private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

    public static io.grpc.ServiceDescriptor getServiceDescriptor() {
        io.grpc.ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (FreedomGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .addMethod(getPipeMethod())
                            .build();
                }
            }
        }
        return result;
    }
}
