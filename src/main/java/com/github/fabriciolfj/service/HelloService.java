package com.github.fabriciolfj.service;

import io.quarkus.example.HelloReply;
import io.quarkus.example.HelloRequest;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@GrpcService
public class HelloService implements io.quarkus.example.Greeter {

    @Override
    public Uni<HelloReply> sayHello(HelloRequest request) {
        return Uni.createFrom()

                .item(() -> io.quarkus.example.HelloReply.newBuilder().setMessage("hello " + request.getName())
                        .build());
    }

    @Override
    public Multi<HelloReply> sayHelloStreaming(HelloRequest request) {
        return Multi.createFrom().empty();
    }
}
