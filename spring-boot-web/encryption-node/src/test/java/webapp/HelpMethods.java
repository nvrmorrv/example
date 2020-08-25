package webapp;

import static okhttp3.MediaType.parse;

import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import java.util.Optional;

public class HelpMethods {

  public static Response getNewResponse(int code, String url, Optional<String> content) {
    return (content.isPresent()) ?
          new Response.Builder()
                .code(code)
                .request(new Request.Builder().url(url).build())
                .body(ResponseBody.create(parse("application/json"), content.get()))
                .protocol(Protocol.HTTP_2)
                .message("")
                .build() :
          new Response.Builder()
                .code(code)
                .request(new Request.Builder().url(url).build())
                .protocol(Protocol.HTTP_2)
                .message("")
                .build();
  }

}
