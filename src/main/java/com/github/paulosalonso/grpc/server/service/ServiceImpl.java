package com.github.paulosalonso.grpc.server.service;

import grpc.test.Request;
import grpc.test.Response;
import grpc.test.ServiceGrpc.ServiceImplBase;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

public class ServiceImpl extends ServiceImplBase {

    @Override
    public void unaryCall(Request request, StreamObserver<Response> responseObserver) {
        final var response = Response.newBuilder()
                .setMsg("Requisição processada")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void serverStreamingCall(Request request, StreamObserver<Response> responseObserver) {
        for (var i = 0; i < 5; i++) {
            responseObserver.onNext(Response.newBuilder()
                    .setMsg("Resposta " + i)
                    .build());

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Request> clientStreamingCall(StreamObserver<Response> responseObserver) {
        return new StreamObserver<>() {

            final List<String> result = new ArrayList<>();

            @Override
            public void onNext(Request request) {
                result.add(request.getMsg());
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                final var response = Response.newBuilder()
                        .setMsg(result.toString())
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Request> bidirectionalStreamingCall(StreamObserver<Response> responseObserver) {
        return new StreamObserver<>() {

            private int count;

            @Override
            public void onNext(Request request) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                final var response = Response.newBuilder()
                        .setMsg(String.format("Mensagem %d recebida: %s", count++, request.getMsg()))
                        .build();

                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
