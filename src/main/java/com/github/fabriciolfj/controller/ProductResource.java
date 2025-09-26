package com.github.fabriciolfj.controller;

import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.resteasy.reactive.RestStreamElementType;

@Path("/products")
public class ProductResource {

    @Inject
    @Channel("products-in")
    Multi<String> products;

    @Inject
    @Channel("products-out")
    Emitter<String> productsSend;

    @POST
    public void createProduct(final String product) {
        productsSend.send(product);
    }

    @GET
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<String> stream() {
        return products;
    }
}
