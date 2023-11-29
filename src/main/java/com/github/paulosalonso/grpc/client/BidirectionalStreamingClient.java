package com.github.paulosalonso.grpc.client;

import grpc.test.Request;
import grpc.test.Response;
import grpc.test.ServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Collections.unmodifiableList;

public class BidirectionalStreamingClient {

    public BidirectionalStreamingClient(ManagedChannel channel) {
        this.channel = channel;
    }

    private final ManagedChannel channel;

    public CompletableFuture<List<String>> bidirectionalStreamingCall() {
        System.out.println("Realizando chamada ao método \"bidirectional streaming\" do servidor gRPC");

        final var completableFuture = new CompletableFuture<List<String>>();
        final var responseObserver = new ResponseObserver(completableFuture);

        final var stub = ServiceGrpc.newStub(channel);
        final var requestObserver = stub.bidirectionalStreamingCall(responseObserver);

        Streamer.stream(requestObserver);

        return completableFuture;
    }

    private static class ResponseObserver implements StreamObserver<Response> {

        private final List<String> result;
        private final CompletableFuture<List<String>> completableFuture;

        public ResponseObserver(CompletableFuture<List<String>> completableFuture) {
            this.completableFuture = completableFuture;
            result = new ArrayList<>();
        }

        @Override
        public void onNext(Response response) {
            result.add(response.getMsg());
        }

        @Override
        public void onError(Throwable throwable) {
            System.err.println(Arrays.toString(throwable.getStackTrace()));
        }

        @Override
        public void onCompleted() {
            completableFuture.complete(unmodifiableList(result));
        }
    }

    private static class Streamer {

        private final StreamObserver<Request> requestObserver;

        private Streamer(StreamObserver<Request> requestObserver) {
            this.requestObserver = requestObserver;
        }

        public static void stream(StreamObserver<Request> requestObserver) {
            final var streamer = new Streamer(requestObserver);
            new Thread(streamer::run).start();
        }

        private void run() {
            for (var i = 0; i < 5; i++) {
                final var request = Request.newBuilder()
                        .setMsg(String.format("Esta é a mensagem %d", i))
                        .build();

                requestObserver.onNext(request);
            }

            requestObserver.onCompleted();
        }
    }
}
