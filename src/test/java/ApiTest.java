import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiTest {
    //    fixtures
    String baseUrl = "https://api.magicthegathering.io/v1/";
    String expectedContentType = "application/json; charset=utf-8";


    public void responseBasicValidation(Response response) {
        response.then().statusCode(200);
        Headers allHeaders = response.getHeaders();
        String contentType = allHeaders.getValue("Content-Type");
        String transferEncoding = allHeaders.getValue("Transfer-Encoding");
        assertEquals(contentType, expectedContentType);
        assertEquals(transferEncoding, "chunked");
    }


    @Test
    public void getCards() {
//        pass all tests if get request on /cards works correctly
        Response response = get(baseUrl + "cards");
        responseBasicValidation(response);
    }


    @Test
    public void getCardsId() {
        Response response = get(baseUrl + "cards/1");
        responseBasicValidation(response);
        response.then().body("card.cmc",
                equalTo(2.0F));
        response.then().body("card.name", equalTo("Ankh of Mishra"));

        JsonPath jsonPathEvaluator = response.jsonPath();
        String id = jsonPathEvaluator.get("card.id");

        response.then().body("card.id", equalTo(id));

        Response responseById = get(baseUrl + "cards/" + id);
        responseBasicValidation(responseById);
    }

    @Test
    public void getSets() {
        Response response = get(baseUrl + "sets/");
        responseBasicValidation(response);

        JsonPath jsonPathEvaluator = response.jsonPath();
        ArrayList list = jsonPathEvaluator.get("sets.code");
//        list.forEach((n) -> {
//            System.out.println(n);
//        });

        assertEquals(list.size(), 500);
//        System.out.println(list.size());
    }

    @Test
    public void getSetId() {

        Response response = get(baseUrl + "sets/" + "10E");
        responseBasicValidation(response);

        JsonPath jsonPathEvaluator = response.jsonPath();
        assertEquals(jsonPathEvaluator.get("set.name"), "Tenth Edition");

        ArrayList list = jsonPathEvaluator.get("set.booster");
        list.forEach((n) -> {
            System.out.println(n);
        });
    }

    @Test
    public void getTypes() {
        Response response = get(baseUrl + "types");
        responseBasicValidation(response);
    }

    @Test
    public void getSubTypes() {
        Response response = get(baseUrl + "subtypes");
        responseBasicValidation(response);
    }

    @Test
    public void getSupertypesTypes() {
        Response response = get(baseUrl + "supertypes");
        responseBasicValidation(response);

        ArrayList<String> mockSuperTypes = new ArrayList<String>() {
            {
                add("Basic");
                add("Host");
                add("Legendary");
                add("Ongoing");
                add("Snow");
                add("World");
            }
        };

        JsonPath jsonPathEvaluator = response.jsonPath();
        assertEquals(jsonPathEvaluator.get("supertypes"), mockSuperTypes);
    }

    @Test
    public void getFormats() {
        Response response = get(baseUrl + "formats");

        responseBasicValidation(response);
        response.then().assertThat()
                .body("formats.size()", is(19));
    }


    @Test
    public void findByForeignName() {
        String name = "avacyn";
        //Defining the base URI
        RestAssured.baseURI = baseUrl;
        RequestSpecification httpRequest = RestAssured.given();
        //Passing the resource details
        Response res = httpRequest.queryParam("name", "avacyn")
                .queryParam("language", "spanish")
                .get("/cards");
        //Retrieving the response body using getBody() method
        ResponseBody body = res.body();
        //Converting the response body to string object
        String rbdy = body.asString();
        //Creating object of JsonPath and passing the string response body as parameter
        body.prettyPrint();
    }

    @Test
    public void tmp(){
        RestAssured.baseURI = baseUrl;
        RestAssured
                .given()


                .pathParam("resourcePath", "types")
                .when()
                .get("{resourcePath}")
                .then()
                .log()
                .all();

    }

}
