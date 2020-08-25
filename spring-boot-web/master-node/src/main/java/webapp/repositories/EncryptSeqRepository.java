package webapp.repositories;

import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import webapp.documents.EncryptionSequence;
import webapp.service.exceptions.BadRequestException;

@Repository
public interface EncryptSeqRepository extends MongoRepository<EncryptionSequence, String> {

  @NewSpan
  default String saveSequence(List<String> seq) {
    EncryptionSequence newSeq = save(new EncryptionSequence(seq));
    LoggerFactory.getLogger(EncryptSeqRepository.class)
          .debug("Saving new sequence: id : {}, sequence nodes id : {}",
                newSeq.getSeqId(),
                seq.stream().reduce((s1, s2) -> s1 + ", " + s2).get());
    return newSeq.getSeqId();
  }

  @NewSpan
  default List<String> getSequence(String seqId) {
    return findById(seqId)
          .orElseThrow(() -> new BadRequestException("Unknown id:" + seqId))
          .getNodeIdSeq();
  }

}
