package webapp.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EncryptionNodeInfo {
  private final int port;
  private final String address;
  private final String contextPath;
  private final String monitorPath;
}
