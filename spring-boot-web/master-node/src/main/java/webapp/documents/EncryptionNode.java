package webapp.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
public class EncryptionNode {
  private final String encryptionPath;
  private final String decryptionPath;
  private final String monitorPath;
  @Id
  private String nodeId;

  public EncryptionNode(String encryptionPath, String decryptionPath, String monitorPath) {
    this.encryptionPath = encryptionPath;
    this.decryptionPath = decryptionPath;
    this.monitorPath = monitorPath;
  }
}
