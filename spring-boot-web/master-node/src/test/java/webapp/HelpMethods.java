package webapp;

import static okhttp3.MediaType.parse;

import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import webapp.documents.EncryptionNode;
import webapp.service.dto.EncryptionNodeInfo;

public class HelpMethods {
  private static final Random random = new Random(System.currentTimeMillis());

  public static EncryptionNode getNewEncryptionNode() {
    String enPAth = "encPath" + Math.abs(random.nextLong());
    String decPAth = "decPath" + Math.abs(random.nextLong());
    String mPAth = "mPath" + Math.abs(random.nextLong());
    return new EncryptionNode(enPAth, decPAth, mPAth);
  }

  public static List<EncryptionNode> getNodes(int count) {
    return IntStream.range(0, count)
          .mapToObj(i -> getNewEncryptionNode())
          .collect(Collectors.toList());
  }

  public static EncryptionNodeInfo getNodeInfo(EncryptionNode node) {
    return new EncryptionNodeInfo(
          node.getEncryptionPath(),
          node.getDecryptionPath(),
          node.getMonitorPath());
  }


  public static void setNodesId(List<EncryptionNode> nodes) {
    nodes.forEach(n -> n.setNodeId(String.valueOf(random.nextLong())));
  }

  public static Response getNewResponse(int code, String url, Optional<String> content) {
    return (content.isPresent()) ?
          new Response.Builder()
                .code(code)
                .request(new Request.Builder().url(url).build())
                .body(ResponseBody.create(parse("application/json"), content.get()))
                .message("")
                .protocol(Protocol.HTTP_2)
                .build() :
          new Response.Builder()
                .code(code)
                .request(new Request.Builder().url(url).build())
                .message("")
                .protocol(Protocol.HTTP_2)
                .build();
  }

}
