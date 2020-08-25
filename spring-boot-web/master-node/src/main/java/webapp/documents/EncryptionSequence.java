package webapp.documents;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
public class EncryptionSequence {
  private final List<String> nodeIdSeq;
  @Id
  private String seqId;

  public EncryptionSequence(List<String> nodeIdSeq) {
    this.nodeIdSeq = nodeIdSeq;
  }

}
