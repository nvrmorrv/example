package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.restassured.RestAssured;
import java.io.File;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class WebAppTest {
  private ServerConfig config;

  @Before
  public void takeServerConfig() throws IOException {
    String configPath = WebAppTest.class.getClassLoader()
      .getResource("serverConfig.yaml").getPath();
    config = new ObjectMapper(new YAMLFactory())
      .readValue(new File(configPath), ServerConfig.class);
  }

  @Before
  public void clientSetup() {
    RestAssured.reset();
    RestAssured.baseURI = config.getUri();
    RestAssured.basePath = config.getPath();
    RestAssured.port = config.getPort();
  }

  @Test
  public void shouldPassOnAddition() {
    String reqBody = "{\"operation\":\"addition\",\"payload\":{\"argA\": 20,\"argB\": 22}}";
    RestAssured.with().body(reqBody).header("Request-ID", 1232)
      .when().post()
      .then().assertThat().statusCode(200).body("result", Matchers.equalTo(42));
  }

  @Test
  public void shouldPassOnConcat() {
    String reqBody = "{\"operation\":\"concat\",\"payload\":{\"strings\":[\"a\",\"b\", \"c\"]}}";
    RestAssured.with().body(reqBody).header("Request-ID", 1232)
      .when().post()
      .then().assertThat().statusCode(200).body("result", Matchers.equalTo("abc"));
  }

  @Test
  public void shouldPassOnWeatherOp() {
    String reqBody = "{\"operation\":\"weather\",\"payload\":{\"city\": \"kharkiv\" }}";
    RestAssured.with().body(reqBody).header("Request-ID", 1232)
      .when().post()
      .then().assertThat().statusCode(200).body("result.name", Matchers.equalTo("Kharkiv"));
  }

  @Test
  public void shouldFailOnPassingUnknownCityToWeatherOp() {
    String reqBody = "{\"operation\":\"weather\",\"payload\":{\"city\": \"sdfdsf\" }}";
    RestAssured.with().body(reqBody).header("Request-ID", 1232)
      .when().post()
      .then().assertThat().statusCode(404)
      .body("error", Matchers.equalTo("Data for passed city is not found, city : sdfdsf"));
  }

  @Test
  public void shouldFailOnPassingUnknownIdToExtractOp() {
    String reqBody = "{\"operation\":\"extract\",\"payload\":{\"id\":105}}";
    RestAssured.with().body(reqBody).header("Request-ID", 1232)
      .when().post()
      .then().assertThat().statusCode(404)
      .body("error", Matchers.equalTo("passed id doesnt exist, id : 105"));
  }

  @Test
  public void shouldPassOnStoreOp() {
    String reqBody = "{\"operation\":\"store\",\"payload\":{\"id\":31,\"content\":{\"a\":23,\"b\":37}}}";
    RestAssured.with().body(reqBody).header("Request-ID", 1232)
      .when().put()
      .then().assertThat().statusCode(200)
      .body("result", Matchers.equalTo("data stored under id : 31"));
    String reqBody1 = "{\"operation\":\"store\",\"payload\":{\"id\":31,\"content\":56}}";
    RestAssured.with().body(reqBody1).header("Request-ID", 1232)
      .when().put()
      .then().assertThat().statusCode(200)
      .body("result", Matchers.equalTo("data stored under id : 31"));
  }

  @Test
  public void shouldPassOnExtractOp() {
    String reqBodyToStore = "{\"operation\":\"store\",\"payload\":{\"id\":31,\"content\":{\"a\":23,\"b\":37}}}";
    String reqBodyToExtract = "{\"operation\":\"extract\",\"payload\":{\"id\":31}}";
    RestAssured.with().body(reqBodyToStore).header("Request-ID", 1232).when().put();
    RestAssured.with().body(reqBodyToExtract).header("Request-ID", 1232)
      .when().post()
      .then().assertThat().statusCode(200)
      .body("content.a", Matchers.equalTo(23))
      .body("content.b", Matchers.equalTo(37));
  }

  @Test
  public void shouldFailOnPassingWrongRequestBody() {
    String reqBody = "{\"payload\":{\"id\":31}}";
    RestAssured.with().body(reqBody).header("Request-ID", 1232)
      .when().post()
      .then().assertThat().statusCode(400)
      .body("error", Matchers.equalTo("wrong request format"));
  }

  @Test
  public void shouldFailOnPassingWrongPayload() {
    String reqBody = "{\"operation\":\"addition\",\"payload\":{\"argA\": 20,\"argB\": 22, \"argC\": 30}}";
    String resBody = "{\"error\":\"wrong payload format\"}";
    RestAssured.with().body(reqBody).header("Request-ID", 1232)
      .when().post()
      .then().assertThat().statusCode(400).body(Matchers.equalTo(resBody));
  }

  @Test
  public void shouldFailOnCallingUnknownOperation() {
    String reqBody = "{\"operation\":\"unknown\",\"payload\":{\"argA\": 20,\"argB\": 22}}";
    RestAssured.with().body(reqBody).header("Request-ID", 1232)
      .when().post()
      .then().assertThat().statusCode(404)
      .body("error", Matchers.equalTo("operation unknown is not found"));
  }

  @Test
  public void shouldFailOnRequestWithoutRequestIdHeader() {
    String reqBody = "{\"operation\":\"addition\",\"payload\":{\"argA\": 20,\"argB\": 22}}";
    RestAssured.with().body(reqBody)
      .when().post()
      .then().assertThat().statusCode(400)
      .body("error", Matchers.equalTo("request doesnt have request-id header"));
  }


  @AllArgsConstructor
  @Getter
  public static class ServerConfig {
    private final String uri;
    private final String path;
    private final int port;
  }
}
