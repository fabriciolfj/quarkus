package com.github.fabriciolfj;

import io.restassured.RestAssured;



class TheBestPlaceToBeResourceTest {

    void verify() {
        RestAssured.get("/")
                .then()
                .statusCode(200);
    }

}