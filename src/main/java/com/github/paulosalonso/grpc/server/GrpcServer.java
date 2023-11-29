package com.github.paulosalonso.grpc.server;

import com.github.paulosalonso.grpc.server.service.ServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GrpcServer {

    private final int port;
    private final Server server;

    public GrpcServer(int port) {
        Logger.getLogger("io.grpc").setLevel(Level.ALL);
        Logger.getLogger("io.grpc").addHandler(new ConsoleHandler());

        this.port = port;
        this.server = ServerBuilder.forPort(port)
                .addService(new ServiceImpl())
                .build();
    }

    public void start() throws IOException {
        server.start();
        System.out.println("Servidor gRPC iniciado, ouvindo a porta " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Desligando o servidor gRPC");
            GrpcServer.this.stop();
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final var servidor = new GrpcServer(50051);
        servidor.start();
        servidor.blockUntilShutdown();
    }
}
