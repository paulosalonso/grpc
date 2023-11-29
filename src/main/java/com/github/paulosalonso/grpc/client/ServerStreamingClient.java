package com.github.paulosalonso.grpc.client;

import grpc.test.Request;
import grpc.test.Response;
import grpc.test.ServiceGrpc;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Collections.unmodifiableList;

public class ServerStreamingClient {

    private static final Logger LOG = LoggerFactory.getLogger(ServerStreamingClient.class);

    public ServerStreamingClient(ManagedChannel channel) {
        this.channel = channel;
    }

    private final ManagedChannel channel;

    public CompletableFuture<List<String>> serverStreamingCall() {
        LOG.info("Realizando chamada ao método \"server streaming\" do servidor gRPC");

        final var stub = ServiceGrpc.newBlockingStub(channel);

        final var request = Request.newBuilder()
                .setMsg("Hello World GRPC!")
                .build();

        final var responseIterator = stub.serverStreamingCall(request);

        return Streamer.stream(responseIterator);
    }

    private static class Streamer {

        private final CompletableFuture<List<String>> completableFuture;
        private final Iterator<Response> responseIterator;

        private Streamer(CompletableFuture<List<String>> completableFuture, Iterator<Response> responseIterator) {
            this.completableFuture = completableFuture;
            this.responseIterator = responseIterator;
        }

        public static CompletableFuture<List<String>> stream(Iterator<Response> response) {
            final var completableFuture = new CompletableFuture<List<String>>();
            final var streamer = new Streamer(completableFuture, response);
            new Thread(streamer::run).start();
            return completableFuture;
        }

        private void run() {
            final var result = new ArrayList<String>();

            while (responseIterator.hasNext()) {
                final var response = responseIterator.next();
                result.add(response.getMsg());
            }

            completableFuture.complete(unmodifiableList(result));
        }
    }
}
