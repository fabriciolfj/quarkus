package com.github.fabriciolfj.deserializer;

import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.fabriciolfj.model.Fruit;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class ListOfFruitDeserializer extends ObjectMapperDeserializer<List<Fruit>> {
    public ListOfFruitDeserializer() {
        super(new TypeReference<List<Fruit>>() {});
    }
}