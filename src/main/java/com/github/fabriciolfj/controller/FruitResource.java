package com.github.fabriciolfj.controller;

import com.github.fabriciolfj.model.Fruit;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Path("/fruit")
public class FruitResource {

    @Channel("fruit")
    private Emitter<Fruit> emitter;

    @POST
    public Response create(final Fruit fruit) {
        emitter.send(fruit);
        return Response.ok().build();
    }
}
