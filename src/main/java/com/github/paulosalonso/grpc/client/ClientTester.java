package com.github.paulosalonso.grpc.client;

import com.github.paulosalonso.grpc.server.GrpcServer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class ClientTester {

    private static final Logger LOG = LoggerFactory.getLogger(ClientTester.class);

    private static final int SERVER_PORT = 50051;
    private static final GrpcServer SERVIDOR = new GrpcServer(SERVER_PORT);
    private static final ManagedChannel CHANNEL = ManagedChannelBuilder.forAddress("localhost", SERVER_PORT)
            .usePlaintext()
            .build();

    public static void main(String[] args)
    {
        startServer();

        final var unaryResult = new UnaryClient(CHANNEL).unaryCall();
        final var serverStreamFuture = new ServerStreamingClient(CHANNEL).serverStreamingCall();
        final var clientStreamFuture = new ClientStreamingClient(CHANNEL).clientStreamingCall();
        final var bidirectionalStreamFuture = new BidirectionalStreamingClient(CHANNEL).bidirectionalStreamingCall();

        CompletableFuture.allOf(serverStreamFuture, clientStreamFuture, bidirectionalStreamFuture)
                        .thenAccept(noValue -> {
                            LOG.info("Unary stream result: " + unaryResult);
                            LOG.info("Server stream result: " + serverStreamFuture.join());
                            LOG.info("Client stream result: " + clientStreamFuture.join());
                            LOG.info("Bidirectional stream result: " + bidirectionalStreamFuture.join());
                            System.exit(0);
                        });
    }

    private static void startServer() {
        new Thread(() -> {
            try {
                SERVIDOR.start();
                SERVIDOR.blockUntilShutdown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
