package webapp.service;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import webapp.documents.EncryptionNode;
import webapp.repositories.NodesRepository;
import webapp.service.dto.EncryptionNodeInfo;
import webapp.service.dto.MonitorResult;
import webapp.service.exceptions.ServerConnectionException;

@AllArgsConstructor
@Slf4j
public class MasterNodeService {
  private final NodesRepository nodesRepo;
  private final NodeResponseHandler handler;
  private final NodeClient client;
  private final Random random = new Random(System.currentTimeMillis());

  public List<EncryptionNodeInfo> getOperationList(List<EncryptionNode> enList) {
    return enList.stream()
          .map(en -> new EncryptionNodeInfo(
                en.getEncryptionPath(),
                en.getDecryptionPath(),
                en.getMonitorPath()))
          .collect(Collectors.toList());
  }

  public boolean checkSequence(List<String> seq) {
    try {
      log.debug("Checking sequence");
      return seq.stream().map(nodesRepo::getNode).allMatch(this::checkNode);
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  public List<String> getRandomNodesSeq() {
    log.debug("Generating new encryption sequence");
    List<String> opList = nodesRepo.findAll().stream()
          .filter(this::checkNode)
          .limit(5)
          .map(EncryptionNode::getNodeId)
          .collect(Collectors.toList());
    Collections.shuffle(opList, random);
    return opList;
  }

  public boolean checkNode(EncryptionNode node) {
    try {
      MonitorResult result = handler.handleMonitorResponse(
            client.makeRequest(node.getMonitorPath()),
            node.getMonitorPath());
      boolean status = result.getStatus().equals("UP");
      log.debug("Checking node: id : {}, uri : {}, status : {}",
            node.getNodeId(), node.getMonitorPath(), status);
      return status;
    } catch (ServerConnectionException exception) {
      log.debug("Checking node: id : {}, uri : {}, status : false, exception : {}",
            node.getNodeId(), node.getMonitorPath(), exception.getMessage());
      return false;
    }
  }

  public String performEncryption(List<String> seq, String string) {
    for (String nodeId : seq) {
      EncryptionNode node = nodesRepo.getNode(nodeId);
      String uri = String.format("%1s/%2s", node.getEncryptionPath(), string);
      string = handler.handleEncResponse(client.makeRequest(uri), uri).getResult();
      log.debug("Node encryption is done: node id : {}, uri : {}",
            node.getNodeId(), node.getEncryptionPath());
    }
    return string;
  }

  public String performDecryption(List<String> seq, String string) {
    Collections.reverse(seq);
    for (String nodeId : seq) {
      EncryptionNode node = nodesRepo.getNode(nodeId);
      String uri = String.format("%1s/%2s", node.getDecryptionPath(), string);
      string = handler.handleDecResponse(client.makeRequest(uri), uri).getResult();
      log.debug("Node decryption is done: node id : {}, uri: {}",
            node.getNodeId(), node.getDecryptionPath());
    }
    return string;
  }

}
