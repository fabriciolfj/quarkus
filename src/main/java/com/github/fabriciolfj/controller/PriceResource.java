package com.github.fabriciolfj.controller;

import com.github.fabriciolfj.listeners.PriceConsumer;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/prices")
public class PriceResource {

    private final Logger log = LoggerFactory.getLogger(PriceConsumer.class);

    @Inject
    @Channel("prices-out")
    private Emitter<Double> priceProducer;

    @POST
    public Response createPrice(final double value) {
        log.info("request {}", value);

        priceProducer.send(value);
        return Response.ok().build();
    }
}
