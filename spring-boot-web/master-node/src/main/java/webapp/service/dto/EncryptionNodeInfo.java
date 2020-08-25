package webapp.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EncryptionNodeInfo {
  private final String encryptionPath;
  private final String decryptionPath;
  private final String monitorPath;
}