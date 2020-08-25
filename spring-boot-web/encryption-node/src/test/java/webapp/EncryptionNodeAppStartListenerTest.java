package webapp;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static webapp.HelpMethods.getNewResponse;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import java.io.IOException;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.event.ContextRefreshedEvent;
import webapp.service.EncryptionNodeInfo;
import webapp.service.MasterNodeUri;

@RunWith(MockitoJUnitRunner.class)
public class EncryptionNodeAppStartListenerTest {
  private final MasterNodeUri masterNodeUri = new MasterNodeUri("http://master-node");
  private final EncryptionNodeInfo info = new EncryptionNodeInfo(
        8090,
        "some-address",
        "/encryption-node",
        "/monitor"
  );

  @Mock
  private ContextRefreshedEvent event;

  @Mock
  private OkHttpClient client;

  @Mock
  private Call call;

  @Test
  public void shouldPassWhenMasterNodeIsPresent() throws IOException {
    EncryptionNodeAppStartListener listener =
          new EncryptionNodeAppStartListener(masterNodeUri, info, client);
    Response response = getNewResponse(
          200, masterNodeUri.getNotifyUri(), Optional.empty());
    Mockito.when(client.newCall(Mockito.any())).thenReturn(call);
    Mockito.when(call.execute()).thenReturn(response);
    assertThatCode(() -> listener.onApplicationEvent(event))
          .doesNotThrowAnyException();
  }

  @Test
  public void shouldFailWhenMasterNodeIsNotPresent() throws IOException {
    EncryptionNodeAppStartListener listener =
          new EncryptionNodeAppStartListener(masterNodeUri, info, client);
    Response response = getNewResponse(
          400, masterNodeUri.getNotifyUri(), Optional.empty());
    Mockito.when(client.newCall(Mockito.any())).thenReturn(call);
    Mockito.when(call.execute()).thenReturn(response);
    assertThatThrownBy(() -> listener.onApplicationEvent(event))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage(String.format("Notifying master node error, uri: %s ",
                masterNodeUri.getNotifyUri()));
  }


}
