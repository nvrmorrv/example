package webapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static webapp.HelpMethods.getNewEncryptionNode;
import static webapp.HelpMethods.getNewResponse;
import static webapp.HelpMethods.getNodes;
import static webapp.HelpMethods.setNodesId;

import okhttp3.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import webapp.documents.EncryptionNode;
import webapp.repositories.NodesRepository;
import webapp.service.dto.DecryptionResult;
import webapp.service.dto.EncryptionNodeInfo;
import webapp.service.dto.EncryptionResult;
import webapp.service.dto.MonitorResult;
import webapp.service.exceptions.ServerConnectionException;

@RunWith(MockitoJUnitRunner.class)
public class MasterNodeServiceTest {
  private final String URL = "http://fakeurl";

  @Mock
  NodesRepository nodesRepo;

  @Mock
  NodeResponseHandler handler;

  @Mock
  NodeClient client;

  @Test
  public void shouldPassOnGettingEncryptionNodeInfoList() {
    MasterNodeService service = new MasterNodeService(nodesRepo, handler, client);
    EncryptionNode node = getNewEncryptionNode();
    List<EncryptionNodeInfo> list = service.getOperationList(Collections.singletonList(node));
    assertEquals(1, list.size());
    assertEquals(node.getEncryptionPath(), list.get(0).getEncryptionPath());
    assertEquals(node.getDecryptionPath(), list.get(0).getDecryptionPath());
    assertEquals(node.getMonitorPath(), list.get(0).getMonitorPath());
  }

  @Test
  public void shouldPassOnEncryption() {
    MasterNodeService service = new MasterNodeService(nodesRepo, handler, client);
    List<EncryptionNode> nodes = getNodes(3);
    setNodesId(nodes);
    List<String> seq =
          nodes.stream().map(EncryptionNode::getNodeId).collect(Collectors.toList());
    Response response = getNewResponse(200, URL, Optional.empty());
    EncryptionResult result = new EncryptionResult("string");
    Mockito.when(nodesRepo.getNode(any()))
          .thenReturn(nodes.get(0)).thenReturn(nodes.get(1)).thenReturn(nodes.get(2));
    Mockito.when(client.makeRequest(Mockito.anyString())).thenReturn(response);
    Mockito.when(handler.handleEncResponse(any(), Mockito.anyString())).thenReturn(result);
    assertEquals(result.getResult(), service.performEncryption(seq, "some string"));
  }

  @Test
  public void shouldPassOnDecryption() {
    MasterNodeService service = new MasterNodeService(nodesRepo, handler, client);
    List<EncryptionNode> nodes = getNodes(3);
    setNodesId(nodes);
    List<String> seq =
          nodes.stream().map(EncryptionNode::getNodeId).collect(Collectors.toList());
    Response response = getNewResponse(200, URL, Optional.empty());
    DecryptionResult result = new DecryptionResult("string");
    Mockito.when(nodesRepo.getNode(any()))
          .thenReturn(nodes.get(0)).thenReturn(nodes.get(1)).thenReturn(nodes.get(2));
    Mockito.when(client.makeRequest(Mockito.anyString())).thenReturn(response);
    Mockito.when(handler.handleDecResponse(any(), Mockito.anyString())).thenReturn(result);
    assertEquals(result.getResult(), service.performDecryption(seq, "some string"));
  }

  @Test
  public void shouldPassOnCheckingPresentNode() {
    MasterNodeService service = new MasterNodeService(nodesRepo, handler, client);
    EncryptionNode node = getNewEncryptionNode();
    node.setNodeId("123213");
    Response response = getNewResponse(200, URL, Optional.empty());
    MonitorResult result1 = new MonitorResult("UP");
    MonitorResult result2 = new MonitorResult("DOWN");
    Mockito.when(client.makeRequest(Mockito.anyString())).thenReturn(response);
    Mockito.when(handler.handleMonitorResponse(any(), Mockito.anyString()))
          .thenReturn(result1).thenReturn(result2);
    assertTrue(service.checkNode(node));
    assertFalse(service.checkNode(node));
  }

  @Test
  public void shouldFailOnCheckingClosedNode() {
    MasterNodeService service = new MasterNodeService(nodesRepo, handler, client);
    EncryptionNode node = getNewEncryptionNode();
    node.setNodeId("123213");
    Mockito.when(client.makeRequest(Mockito.anyString()))
          .thenThrow(new ServerConnectionException("node is not respond"));
    assertFalse(service.checkNode(node));
  }

  @Test
  public void shouldPassOnCheckingSequenceOfPresentNodes() {
    MasterNodeService service = Mockito.spy(new MasterNodeService(nodesRepo, handler, client));
    List<EncryptionNode> nodes = getNodes(3);
    setNodesId(nodes);
    List<String> seq =
          nodes.stream().map(EncryptionNode::getNodeId).collect(Collectors.toList());
    Mockito.when(nodesRepo.getNode(any()))
          .thenReturn(nodes.get(0)).thenReturn(nodes.get(1)).thenReturn(nodes.get(2));
    Mockito.doReturn(true).when(service).checkNode(any());
    assertTrue(service.checkSequence(seq));
    Mockito.doReturn(false).when(service).checkNode(any());
    assertFalse(service.checkSequence(seq));
  }

  @Test
  public void shouldPassOnCheckingSequenceWithDeletedNodes() {
    MasterNodeService service = new MasterNodeService(nodesRepo, handler, client);
    Mockito.when(nodesRepo.getNode(any()))
          .thenThrow(new IllegalArgumentException("there is no such in the repository"));
    assertFalse(service.checkSequence(Collections.singletonList("12313")));
  }

  @Test
  public void shouldPassOnGettingRandomNodesSequence() {
    MasterNodeService service = Mockito.spy(new MasterNodeService(nodesRepo, handler, client));
    List<EncryptionNode> nodes = getNodes(7);
    setNodesId(nodes);
    Mockito.when(nodesRepo.findAll()).thenReturn(nodes);
    Mockito.doReturn(false).when(service).checkNode(nodes.get(0));
    Mockito.doReturn(true).when(service).checkNode(nodes.get(1));
    Mockito.doReturn(true).when(service).checkNode(nodes.get(2));
    Mockito.doReturn(true).when(service).checkNode(nodes.get(3));
    Mockito.doReturn(true).when(service).checkNode(nodes.get(4));
    Mockito.doReturn(true).when(service).checkNode(nodes.get(5));
    List<String> seq = service.getRandomNodesSeq();
    assertEquals(5, seq.size());
    assertFalse(seq.contains(nodes.get(0).getNodeId()));
  }

}
