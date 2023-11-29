package com.github.paulosalonso.grpc.client;

import grpc.test.Request;
import grpc.test.ServiceGrpc;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnaryClient {

    private static final Logger LOG = LoggerFactory.getLogger(UnaryClient.class);

    public UnaryClient(ManagedChannel channel) {
        this.channel = channel;
    }

    private final ManagedChannel channel;

    public String unaryCall() {
        LOG.info("Realizando chamada ao método \"unário\" do servidor gRPC");

        final var stub = ServiceGrpc.newBlockingStub(channel);

        final var request = Request.newBuilder()
                .setMsg("Hello World GRPC!")
                .build();

        final var response = stub.unaryCall(request);

        return response.getMsg();
    }
}
