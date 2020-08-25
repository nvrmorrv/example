package webapp.repositories;

import java.util.Optional;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import webapp.documents.EncryptionNode;

@Repository
public interface NodesRepository extends MongoRepository<EncryptionNode, String> {
  @NewSpan
  Optional<EncryptionNode> findByDecryptionPath(String path);

  @NewSpan
  default EncryptionNode getNode(String nodeId) {
    EncryptionNode node = findById(nodeId)
          .orElseThrow(() -> new IllegalArgumentException("Nodes repo doesnt contain such id:" + nodeId));
    return node;
  }

  @NewSpan
  default void deleteNode(EncryptionNode node) {
    LoggerFactory.getLogger(NodesRepository.class)
          .info("Deleting node: id : {}, enPath : {}, decPath : {}, checkPath : {}",
                node.getNodeId(),
                node.getEncryptionPath(),
                node.getDecryptionPath(),
                node.getMonitorPath());
    delete(node);
  }

  @NewSpan
  default EncryptionNode saveNode(EncryptionNode node) {
    EncryptionNode newNode = save(node);
    LoggerFactory.getLogger(NodesRepository.class)
          .info("Saving new node: id : {}, enPath : {}, decPath : {}, checkPath : {}",
                newNode.getNodeId(),
                newNode.getEncryptionPath(),
                newNode.getDecryptionPath(),
                newNode.getMonitorPath());
    return newNode;
  }

}

