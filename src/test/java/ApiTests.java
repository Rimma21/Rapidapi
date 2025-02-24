import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.testng.AssertJUnit.assertNotNull;

public class ApiTests {
    @BeforeClass
    public void setup() {

        RestAssured.baseURI = "https://currency-converter5.p.rapidapi.com";
    }
    @Test
    public void testGetAmountNotNull() {
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        String amount = "100";

        Response response =
        given()
                .queryParam("from", fromCurrency)
                .queryParam("to", toCurrency)
                .queryParam("amount", amount)
                .header("X-RapidAPI-Host", "currency-converter5.p.rapidapi.com")
                .header("X-RapidAPI-Key", "bbe09f947emsh1efbbb2faeed9dep16afb9jsn4807c4fb7f22")
                .when()
                .get("/currency/convert")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response();

        String returnedFromCurrency = response.jsonPath().getString("base_currency_code");
        String returnedToCurrency = response.jsonPath().getString("target_currency_code");
        String conversionRate = response.jsonPath().getString("conversion_rate");
        String convertedAmount = response.jsonPath().getString("converted_amount");

        Assert.assertNotNull(convertedAmount, "Сконвертированная сумма не должна быть null");
    }

    @Test
    public void testGetCurrencyList() {

        Response response = given()
                .queryParam("format", "json")
                .header("X-RapidAPI-Key", "bbe09f947emsh1efbbb2faeed9dep16afb9jsn4807c4fb7f22")
                .header("X-RapidAPI-Host", "currency-converter5.p.rapidapi.com")
                .when()
                .get("/currency/list")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response();
        Map<String, String> currencies = response.jsonPath().getMap("currencies");
        Assert.assertTrue(currencies.containsKey("USD"), "Список должен содержать валюту USD");

    }
    @Test
    public void testConversionOfProhibitedCurrency() {
        String prohibitedCurrency = "RUB"; // код запрещённой валюты для конвертирования

        Response response = RestAssured.given()
                .queryParam("from", prohibitedCurrency)
                .queryParam("to", "USD")
                .queryParam("amount", "100")
                .header("X-RapidAPI-Key", "bbe09f947emsh1efbbb2faeed9dep16afb9jsn4807c4fb7f22") // Замените на ваш действительный API-ключ
                .header("X-RapidAPI-Host", "currency-converter5.p.rapidapi.com")
                .when()
                .get("/currency/convert")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .extract().response();


        String errorMessage = response.jsonPath().getString("error");
        Assert.assertTrue(errorMessage.contains("Currency not allowed for exchange"), "Ожидаемое сообщение об ошибке отсутствует");
    }

    @Test
    public void testConversionWithInvalidCurrencyCode() {
        String invalidCurrency = "XYZ"; // Недопустимый код валюты

        Response response = RestAssured.given()
                .queryParam("from", invalidCurrency)
                .queryParam("to", "USD")
                .queryParam("amount", "100")
                .header("X-RapidAPI-Key", "bbe09f947emsh1efbbb2faeed9dep16afb9jsn4807c4fb7f22") // Замените на ваш действительный API-ключ
                .header("X-RapidAPI-Host", "currency-converter5.p.rapidapi.com")
                .when()
                .get("/currency/convert")
                .then()
                .extract().response();

        int statusCode = response.getStatusCode();
        Assert.assertTrue(statusCode == 400 || statusCode == 422, "Ожидаемый статус-код 400 или 422, но получен: " + statusCode);
    }
}

