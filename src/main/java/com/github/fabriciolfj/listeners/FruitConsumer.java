package com.github.fabriciolfj.listeners;

import com.github.fabriciolfj.model.Fruit;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public class FruitConsumer {

    private static final Logger log = LoggerFactory.getLogger(FruitConsumer.class);

    @Incoming("fruit-in")
    public void receive(final Fruit fruit) {
        log.info("receive fruit {}", fruit);
    }
}
