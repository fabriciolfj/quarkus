package com.github.fabriciolfj.controller;

import com.github.fabriciolfj.client.WeatherClient;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/")
public class TheBestPlaceToBeResource {

    static final double VALENCE_LATITUDE = 44.9;
    static final double VALENCE_LONGITUDE = 4.9;

    static final double ATHENS_LATITUDE = 37.9;
    static final double ATHENS_LONGITUDE = 23.7;

    @RestClient
    WeatherClient client;

    @GET
    @RunOnVirtualThread
    public String getTheVestPlaceToBe() {
        var valence = client.getWeather(VALENCE_LATITUDE, VALENCE_LONGITUDE).weather().temperature();
        var athens = client.getWeather(ATHENS_LATITUDE, ATHENS_LONGITUDE).weather().temperature();

        if (valence > athens && valence <= 35) {
            return "Valence " + Thread.currentThread();
        } else if (athens > 35) {
            return "Valence " + Thread.currentThread();
        } else {
            return "Athens " + Thread.currentThread();
        }
    }

}
