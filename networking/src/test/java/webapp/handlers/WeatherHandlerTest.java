package webapp.handlers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import webapp.operation.WeatherOp;

@RunWith(MockitoJUnitRunner.class)
public class WeatherHandlerTest {
  private final String city = "kharkiv";
  private final String api = "http://fake-weather.api%s";
  private final String name = "fake.weather";
  private final WeatherHandler.WeatherData kharkivWeather = new WeatherHandler.WeatherData(
    "kharkiv",
    57,
    2,
    345,
    new WeatherHandler.WeatherData.MeasurementCoordinate(
      12.5,
      15.9),
    Collections.singletonList(new WeatherHandler.WeatherData.WeatherCommonInfo(
      "good",
      "very good",
      200,
      "12")),
    "sdsd"
  );

  @Spy
  private final WeatherHandler.WeatherApi weatherApi = new WeatherHandler.WeatherApi(api, name);
  @Mock
  private OkHttpClient fakeClient;
  @Mock
  private Call fakeCall;
  @InjectMocks
  private WeatherHandler handler;

  @Test
  public void shouldPassOnPassingExistCity() throws IOException {
    String resBody = new ObjectMapper().writeValueAsString(kharkivWeather);
    Response response = new Response.Builder()
        .code(200)
        .request(new Request.Builder().url(String.format(api, city)).build())
        .body(ResponseBody.create(MediaType.parse("application/json"), resBody))
        .protocol(Protocol.HTTP_2)
        .build();
    Mockito.when(fakeClient.newCall(Mockito.any())).thenReturn(fakeCall);
    Mockito.when(fakeCall.execute()).thenReturn(response);
    assertEquals(kharkivWeather, handler.handle(new WeatherOp(city)).getResult());
  }

  @Test
  public void shouldFailOnPassingUnknownCity() throws IOException {
    Response response = new Response.Builder()
      .code(404)
      .request(new Request.Builder().url(String.format(api, city)).build())
      .protocol(Protocol.HTTP_2)
      .build();
    Mockito.when(fakeClient.newCall(Mockito.any())).thenReturn(fakeCall);
    Mockito.when(fakeCall.execute()).thenReturn(response);
    assertThatThrownBy(() -> handler.handle(new WeatherOp("kyiv")))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Data for passed city is not found, city : kyiv");
  }

  @Test
  public void shouldFailOnApiError() throws Exception {
    Mockito.when(fakeClient.newCall(Mockito.any())).thenReturn(fakeCall);
    Mockito.when(fakeCall.execute()).thenThrow(IOException.class);
    assertThatThrownBy(() -> handler.handle(new WeatherOp(city)))
      .isInstanceOf(WeatherHandler.WebApiException.class)
      .hasMessage(String.format("API error. api : %1s, city : %2s", name, city));
  }

}
