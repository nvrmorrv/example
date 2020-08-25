package webapp.handlers;

import static webapp.JsonMapper.getValueFromJson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import webapp.operation.OperationHandler;
import webapp.operation.WeatherOp;

@AllArgsConstructor
@Slf4j
public class WeatherHandler implements OperationHandler<WeatherOp, WeatherHandler.WeatherResult> {
  private final WeatherApi weatherApi;
  private final OkHttpClient client;

  public WeatherHandler(WeatherApi weatherApi) {
    this.weatherApi = weatherApi;
    this.client = new OkHttpClient();
  }


  @Override
  public WeatherResult handle(WeatherOp operation) {
    try {
      log.debug("city : {}", operation.getCity());
      log.debug("api : {}", weatherApi.getName());
      InputStream json = getWeatherJson(operation.getCity());
      return new WeatherResult(getValueFromJson(json, WeatherData.class));
    } catch (IOException ex) {
      throw new WebApiException(
        String.format("API error. api : %1s, city : %2s", weatherApi.getName(), operation.getCity()), ex);
    }
  }


  private InputStream getWeatherJson(String city) throws IOException {
    Request request = new Request.Builder()
        .url(String.format(weatherApi.getApi(), city))
        .build();
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      throw new IllegalArgumentException(
        String.format("Data for passed city is not found, city : %s", city));
    }
    return response.body().byteStream();
  }

  public static class WebApiException extends RuntimeException {
    public WebApiException(String mess, Throwable cause) {
      super(mess, cause);
    }
  }

  @AllArgsConstructor
  @Getter
  public static class WeatherApi {
    private final String api;
    private final String name;
  }

  @AllArgsConstructor
  @Getter
  public static class WeatherResult {
    private final WeatherData result;
  }

  @AllArgsConstructor
  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WeatherData {
    private final String name;
    private final Integer cod;
    private final Integer timezone;
    private final Integer id;
    private final MeasurementCoordinate coord;
    private final List<WeatherCommonInfo> weather;
    private final String base;


    @AllArgsConstructor
    @Data
    public static class WeatherCommonInfo {
      private final String main;
      private final String description;
      private final Integer id;
      private final String icon;
    }

    @AllArgsConstructor
    @Data
    public static class MeasurementCoordinate {
      private final Double lon;
      private final Double lat;
    }
  }
}
