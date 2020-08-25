package webapp.controllers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static webapp.HelpMethods.getNewEncryptionNode;
import static webapp.HelpMethods.getNodeInfo;
import static webapp.HelpMethods.getNodes;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.SpyBean;
import webapp.HelpMethods;
import webapp.documents.EncryptionNode;
import webapp.repositories.EncryptSeqRepository;
import webapp.repositories.NodesRepository;
import webapp.service.MasterNodeService;
import webapp.service.dto.CanDecryptResponse;
import webapp.service.dto.DecryptionResponse;
import webapp.service.dto.EncryptionNodeInfo;
import webapp.service.dto.EncryptionResponse;
import webapp.service.exceptions.BadRequestException;

@RunWith(MockitoJUnitRunner.class)
public class MasterNodeControllerTest {

  @Mock
  NodesRepository nodesRepo;

  @Mock
  EncryptSeqRepository seqRepo;

  @Mock
  MasterNodeService service;

  @SpyBean
  MeterRegistry registry = new SimpleMeterRegistry();

  @Captor
  ArgumentCaptor<EncryptionNode> saveNodeMethodArgumentCaptor;

  @Test
  public void shouldPassOnNotifyingMasterNodeByNewEncNode() {
    MasterNodeController controller = new MasterNodeController(nodesRepo, seqRepo, service, registry);
    EncryptionNodeInfo info = getNodeInfo(getNewEncryptionNode());
    Mockito.when(nodesRepo.findByDecryptionPath(info.getDecryptionPath()))
          .thenReturn(Optional.empty());
    controller.registerNode(info);
    Mockito.verify(nodesRepo, Mockito.times(1))
          .saveNode(saveNodeMethodArgumentCaptor.capture());
    EncryptionNode node = saveNodeMethodArgumentCaptor.getValue();
    assertEquals(info.getEncryptionPath(), node.getEncryptionPath());
    assertEquals(info.getDecryptionPath(), node.getDecryptionPath());
    assertEquals(info.getMonitorPath(), node.getMonitorPath());
  }

  @Test
  public void shouldFailOnNotifyingMasterNodeByAlreadyAddedEncNode() {
    MasterNodeController controller = new MasterNodeController(nodesRepo, seqRepo, service, registry);
    EncryptionNode node = getNewEncryptionNode();
    node.setNodeId("12e2wedqw");
    EncryptionNodeInfo info = getNodeInfo(node);
    Mockito.when(nodesRepo.findByDecryptionPath(info.getDecryptionPath())).thenReturn(Optional.of(node));
    controller.registerNode(info);
    Mockito.verify(nodesRepo, Mockito.never()).saveNode(Mockito.any());
  }

  @Test
  public void shouldPassOnEncryption() {
    MasterNodeController controller = new MasterNodeController(nodesRepo, seqRepo, service, registry);
    String encStr = "asdsadsnjdf";
    String encRes = "dsdfdf";
    String seqId = "sadd768";
    List<String> nodeIds = Arrays.asList("ads233", "dsad123", "dqad34234");
    Mockito.when(service.getRandomNodesSeq()).thenReturn(nodeIds);
    Mockito.when(service.performEncryption(nodeIds, encStr)).thenReturn(encRes);
    Mockito.when(seqRepo.saveSequence(nodeIds)).thenReturn(seqId);
    EncryptionResponse response = controller.encrypt(encStr);
    assertEquals(encRes, response.getResult());
    assertEquals(seqId, response.getId());
  }

  @Test
  public void shouldPassOnDecryption() {
    MasterNodeController controller = new MasterNodeController(nodesRepo, seqRepo, service, registry);
    String decStr = "asdsadsnjdf";
    String decId = "sadd768";
    String decRes = "dsdfdf";
    List<String> nodeIds = Arrays.asList("ads233", "dsad123", "dqad34234");
    Mockito.when(seqRepo.getSequence(decId)).thenReturn(nodeIds);
    Mockito.when(service.checkSequence(nodeIds)).thenReturn(true).thenReturn(false);
    Mockito.when(service.performDecryption(nodeIds, decStr)).thenReturn(decRes);
    DecryptionResponse response = controller.decrypt(decStr, decId);
    assertEquals(decRes, response.getResult());
    assertThatThrownBy(() -> controller.decrypt(decStr, decId))
          .isInstanceOf(BadRequestException.class)
          .hasMessage("Some of decryption nodes is not accessible already");
  }

  @Test
  public void shouldPassOnGettingAllNodes() {
    MasterNodeController controller = new MasterNodeController(nodesRepo, seqRepo, service, registry);
    List<EncryptionNode> allNodes = getNodes(5);
    List<EncryptionNodeInfo> allNodesInfo = allNodes.stream()
          .map(HelpMethods::getNodeInfo).collect(Collectors.toList());
    Mockito.when(nodesRepo.findAll()).thenReturn(allNodes);
    Mockito.when(service.getOperationList(allNodes)).thenReturn(allNodesInfo);
    List<EncryptionNodeInfo> list = controller.getAllNodes();
    assertEquals(allNodesInfo, list);
  }

  @Test
  public void shouldPassOnCheckingSequence() {
    MasterNodeController controller = new MasterNodeController(nodesRepo, seqRepo, service, registry);
    String seqId = "dsadasd";
    List<String> nodeIds = Arrays.asList("ads233", "dsad123", "dqad34234");
    Mockito.when(seqRepo.getSequence(seqId)).thenReturn(nodeIds);
    Mockito.when(service.checkSequence(nodeIds)).thenReturn(true).thenReturn(false);
    CanDecryptResponse response = controller.canDecrypt(seqId);
    CanDecryptResponse response1 = controller.canDecrypt(seqId);
    assertEquals("yes", response.getAnswer());
    assertEquals("no", response1.getAnswer());
  }

}
