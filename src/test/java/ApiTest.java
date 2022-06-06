import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


public class ApiTest {
    //    fixtures
    String baseUrl = "https://api.magicthegathering.io/v1/";
    String expectedContentType = "application/json; charset=utf-8";
    ArrayList codeList = new ArrayList();


    public void responseBasicValidation(Response response) {
        response.then().statusCode(200);
        Headers allHeaders = response.getHeaders();
        String contentType = allHeaders.getValue("Content-Type");
        String transferEncoding = allHeaders.getValue("Transfer-Encoding");
        assertEquals(contentType, expectedContentType);
        assertEquals(transferEncoding, "chunked");
    }

    public void testCardId(String id) {
//        method to get card by id, not mocked, require id , request looks like /card/1123-czxjnc-h213
        Response response = get(baseUrl + "cards/" + id);
        responseBasicValidation(response);
        response.then().body("card.id", equalTo(id));
    }

    public void testSetId(String id) {
//        pass if mocked set-code exist and data  on set booster is correct
        Response response = get(baseUrl + "sets/" + id);
        responseBasicValidation(response);

        JsonPath jsonPathEvaluator = response.jsonPath();
        String a = jsonPathEvaluator.get("set.code");
        assertNotEquals(codeList.indexOf(a), -1);
    }


    @Test
    public void getCards() {
//        pass if get request on /cards works correctly
        Response response = get(baseUrl + "cards");
        responseBasicValidation(response);
    }

    @Test
    public void getCardsMockId() {
//        pass if first card exist in cards
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
//        pass if sets return all sets(actual 500 codes of sets)
        Response response = get(baseUrl + "sets/");
        responseBasicValidation(response);

        JsonPath jsonPathEvaluator = response.jsonPath();
        ArrayList list = jsonPathEvaluator.get("sets.code");
        codeList.addAll(list);
        assertEquals(list.size(), 500);

        String tmpId = (String) list.get(123);
        testSetId(tmpId);
    }

    @Test
    public void getSetMockId() {
//        pass if mocked set-code exist and data  on set booster is correct
        Response response = get(baseUrl + "sets/" + "10E");
        responseBasicValidation(response);

        JsonPath jsonPathEvaluator = response.jsonPath();
        assertEquals(jsonPathEvaluator.get("set.name"), "Tenth Edition");

        ArrayList list = jsonPathEvaluator.get("set.booster");

        assertEquals(list.size(), 14);
    }
    @Test
    public void getTypes() {
//        pass if /types returns data
        Response response = get(baseUrl + "types");
        responseBasicValidation(response);

        JsonPath jsonPathEvaluator = response.jsonPath();
        assertEquals(jsonPathEvaluator.get("types.size()"), new Integer("23"));
    }

    @Test
    public void getSubTypes() {
        Response response = get(baseUrl + "subtypes");
        responseBasicValidation(response);
        JsonPath jsonPathEvaluator = response.jsonPath();
        assertEquals(jsonPathEvaluator.get("subtypes.size()"), new Integer("445"));
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
        RestAssured.baseURI = baseUrl;
        RequestSpecification httpRequest = RestAssured.given();

        Response res = httpRequest.queryParam("name", "avacyn")
                .queryParam("language", "spanish")
                .get("/cards");
        JsonPath jsonPathEvaluator = res.jsonPath();
        assertEquals(jsonPathEvaluator.get("cards[0].name"), "Avacyn, Angel of Hope");
        assertEquals(jsonPathEvaluator.get("cards[0].id"), "2ed7c882-83c3-5867-87bc-5a6c8e69db56");
        testCardId(jsonPathEvaluator.get("cards[0].id"));
    }

    @Test
    public void searchByInvalidForeignName() {
        RestAssured.baseURI = baseUrl;
        RequestSpecification httpRequest = RestAssured.given();

        Response res = httpRequest.queryParam("name", "qwertyu")
//                .queryParam("language", "spanish")
                .get("/cards");
        JsonPath jsonPathEvaluator = res.jsonPath();
        assertEquals(jsonPathEvaluator.get("cards"), new ArrayList<>());
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
