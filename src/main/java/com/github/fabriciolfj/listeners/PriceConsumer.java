package com.github.fabriciolfj.listeners;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PriceConsumer {

    private final Logger log = LoggerFactory.getLogger(PriceConsumer.class);

    /*@Incoming("prices")
    @Retry(maxRetries = 3, delay = 10)
    public Uni<Void> consome(final Message<Double> msg) {
        log.info("price receive {}", msg.getPayload());
        msg.ack();

        return Uni.createFrom().voidItem();
    }*/

    @Incoming("prices")
    @Retry(maxRetries = 3, delay = 10)
    public void consome(final double value) {
        log.info("price receive {}", value);
    }
}
