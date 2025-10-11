package com.github.fabriciolfj;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;


class GreetingResourceTest {

    void testHelloEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .body(is("Hello from Quarkus REST"));
    }

}