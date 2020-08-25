package webapp.repositories;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MongoDBContainer;
import webapp.configs.ReposConfig;
import webapp.documents.EncryptionSequence;
import webapp.service.exceptions.BadRequestException;

;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
      ReposConfig.class,
      TransactionalManagerConfig.class
})
@DataMongoTest
@Transactional
public class EncryptSeqRepositoryTest {
  @ClassRule
  public static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.2.0");

  @Autowired
  EncryptSeqRepository repository;

  @DynamicPropertySource
  public static void dynamicPropertySource(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.port", mongoDBContainer::getFirstMappedPort);
    registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
  }

  @BeforeClass
  public static void setupMongoContainer() throws IOException, InterruptedException {
    mongoDBContainer.execInContainer("mongo", "--eval", "db.createCollection('encryptionSequence')");
  }

  @Test
  public void shouldPassOnSavingSequence() {
    List<String> seq = Arrays.asList("2132131", "123213", "1123213");
    String seqId = repository.saveSequence(seq);
    assertTrue(repository.findById(seqId).isPresent());
    EncryptionSequence savedSeq = repository.findById(seqId).get();
    assertEquals(seq, savedSeq.getNodeIdSeq());
  }

  @Test
  public void shouldPassOnGettingStoredSequence() {
    List<String> seq = Arrays.asList("2132131", "123213", "1123213");
    String seqId = repository.saveSequence(seq);
    List<String> savedSeq = repository.getSequence(seqId);
    assertEquals(seq, savedSeq);
  }

  @Test
  public void shouldFailOnGettingSequenceWhichWasNotStored() {
    String seqId = "324h5345543dsf543543jbj42j34324";
    assertThatThrownBy(() -> repository.getSequence(seqId))
          .isInstanceOf(BadRequestException.class)
          .hasMessage("Unknown id:" + seqId);

  }
}

