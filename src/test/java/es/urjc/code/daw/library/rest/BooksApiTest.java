package es.urjc.code.daw.library.rest;

import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class BooksApiTest {

    @BeforeEach
    public void setUp() {
        port = 8443;
        useRelaxedHTTPSValidation();
        baseURI = "https://localhost:" + port;
    }

    @Test
    public void whenRequestAllBookAsAnonymousUserThenReturnAllBooks() {
        when().get("/api/books/")
                .then().statusCode(200)
                .body("find { book -> book.id == 1 }.title", equalTo("SUEÑOS DE ACERO Y NEON"))
                .body("find { book -> book.id == 2 }.title", equalTo("LA VIDA SECRETA DE LA MENTE"))
                .body("find { book -> book.id == 3 }.title", equalTo("CASI SIN QUERER"))
                .body("find { book -> book.id == 4 }.title", equalTo("TERMINAMOS Y OTROS POEMAS SIN TERMINAR"))
                .body("find { book -> book.id == 5 }.title", equalTo("LA LEGIÓN PERDIDA"));
    }

    @Test
    public void whenRequestAllBookAsLoggedUserThenReturnAllBooks() {
        given().auth().basic("user", "pass")
                .when().get("/api/books/")
                .then().statusCode(200)
                .body("find { book -> book.id == 1 }.title", equalTo("SUEÑOS DE ACERO Y NEON"))
                .body("find { book -> book.id == 2 }.title", equalTo("LA VIDA SECRETA DE LA MENTE"))
                .body("find { book -> book.id == 3 }.title", equalTo("CASI SIN QUERER"))
                .body("find { book -> book.id == 4 }.title", equalTo("TERMINAMOS Y OTROS POEMAS SIN TERMINAR"))
                .body("find { book -> book.id == 5 }.title", equalTo("LA LEGIÓN PERDIDA"));
    }

    @Test
    public void whenRequestAddBookAsAnonymousUserThenReturnError() {
        given().contentType("application/json")
                .request().body("{ \"title\": \"Test title\", \"description\":\"test description\"}")
                .when().post("/api/books/")
                .then().statusCode(401);

    }

    @Test
    public void whenRequestAddBookAsLoggedUserThenReturnSuccess() {
        given().auth().basic("user", "pass")
                .contentType("application/json")
                .request().body("{ \"title\": \"Test title\", \"description\":\"test description\"}")
                .when().post("/api/books/")
                .then().statusCode(201)
                .body("title", equalTo("Test title"))
                .body("description", equalTo("test description"));
    }

    @Test
    public void whenRequestDeleteBookAsAnonymousUserTheReturnError() {
        when().delete("/api/books/8271").then().statusCode(401);
    }

    @Test
    public void whenRequestDeleteBookAsLoggedUserThenReturnError() {
        given().auth().basic("user", "pass")
                .when().delete("/api/books/8373")
                .then().statusCode(403);
    }

    @Test
    public void whenRequestDeleteBookAsAdminThenReturnSuccess() {
        Long id = createBook();
        given().auth().basic("admin", "pass")
                .when().delete("/api/books/" + id)
                .then().statusCode(200);
    }


    private Long createBook() {
        RequestSpecification httpRequest = given().contentType("application/json");
        httpRequest.auth().basic("admin", "pass");
        httpRequest.request().body("{ \"title\": \"Test title\", \"description\":\"test description\"}");

        JsonPath jsonPath = httpRequest.post("/api/books/").jsonPath();
        return jsonPath.getLong("id");
    }
}
