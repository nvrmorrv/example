package webapp.repositories;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static webapp.HelpMethods.getNewEncryptionNode;

import java.io.IOException;
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
import webapp.documents.EncryptionNode;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
      ReposConfig.class,
      TransactionalManagerConfig.class
})
@DataMongoTest
@Transactional
public class NodesRepositoryTest {
  @ClassRule
  public static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.2.0");

  @Autowired
  private NodesRepository repository;

  @DynamicPropertySource
  public static void dynamicPropertySource(DynamicPropertyRegistry registry) {
    System.out.println("jqsadhxksajdjkas");
    registry.add("spring.data.mongodb.port", mongoDBContainer::getFirstMappedPort);
    registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
  }

  @BeforeClass
  public static void setupMongoContainer() throws IOException, InterruptedException {
    mongoDBContainer.execInContainer("mongo", "--eval", "db.createCollection('encryptionNode')");
  }

  @Test
  public void shouldPassOnSavingNode() {
    EncryptionNode node = getNewEncryptionNode();
    String nodeId = repository.saveNode(node).getNodeId();
    assertTrue(repository.findById(nodeId).isPresent());
    EncryptionNode retrievedNode = repository.findById(nodeId).get();
    assertEquals(node.getEncryptionPath(), retrievedNode.getEncryptionPath());
    assertEquals(node.getDecryptionPath(), retrievedNode.getDecryptionPath());
    assertEquals(node.getMonitorPath(), retrievedNode.getMonitorPath());
  }

  @Test
  public void shouldPassOnDeletingNode() {
    EncryptionNode node = repository.saveNode(getNewEncryptionNode());
    assertTrue(repository.findById(node.getNodeId()).isPresent());
    repository.deleteNode(node);
    assertFalse(repository.findById(node.getNodeId()).isPresent());
  }

  @Test
  public void shouldPassOnGettingNode() {
    EncryptionNode node = repository.saveNode(getNewEncryptionNode());
    EncryptionNode resNode = repository.getNode(node.getNodeId());
    assertEquals(node.getEncryptionPath(), resNode.getEncryptionPath());
    assertEquals(node.getDecryptionPath(), resNode.getDecryptionPath());
    assertEquals(node.getMonitorPath(), resNode.getMonitorPath());
  }

  @Test
  public void shouldFailOnGettingNodeWhichWasNotAdded() {
    String nodeId = "234232rjk3rbd44343453kjh5657kh9080kj45";
    assertThatThrownBy(() -> repository.getNode(nodeId))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Nodes repo doesnt contain such id:" + nodeId);
  }

}
