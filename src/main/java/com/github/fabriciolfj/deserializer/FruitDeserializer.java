package com.github.fabriciolfj.deserializer;

import com.github.fabriciolfj.model.Fruit;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class FruitDeserializer extends ObjectMapperDeserializer<Fruit> {
    public FruitDeserializer(Class<Fruit> type) {
        super(type);
    }
}
