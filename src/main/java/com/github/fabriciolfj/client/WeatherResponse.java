package com.github.fabriciolfj.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherResponse(@JsonProperty("current_weather") Weather weather) {
    // represents the response
}

