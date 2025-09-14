package com.github.fabriciolfj;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TheBestPlaceToBeResourceTest {

    @Test
    void verify() {
        RestAssured.get("/")
                .then()
                .statusCode(200);
    }

}