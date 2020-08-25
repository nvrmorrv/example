package webapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static webapp.HelpMethods.getNewEncryptionNode;
import static webapp.HelpMethods.getNewResponse;
import static webapp.HelpMethods.getNodeInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MongoDBContainer;
import webapp.configs.ReposConfig;
import webapp.controllers.MasterNodeController;
import webapp.controllers.MasterNodeControllerAdvice;
import webapp.documents.EncryptionNode;
import webapp.repositories.EncryptSeqRepository;
import webapp.repositories.NodesRepository;
import webapp.repositories.TransactionalManagerConfig;
import webapp.service.MasterNodeService;
import webapp.service.NodeClient;
import webapp.service.NodeResponseHandler;
import webapp.service.dto.CanDecryptResponse;
import webapp.service.dto.DecryptionErrorResult;
import webapp.service.dto.DecryptionResponse;
import webapp.service.dto.DecryptionResult;
import webapp.service.dto.EncryptionNodeInfo;
import webapp.service.dto.EncryptionResult;
import webapp.service.dto.ErrorResponse;
import webapp.service.dto.MonitorResult;
import webapp.service.exceptions.ServerConnectionException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
      ReposConfig.class,
      TransactionalManagerConfig.class,
      MasterNodeTest.MasterNodeTestConfig.class})
@AutoConfigureMockMvc
@Transactional
public class MasterNodeTest {
  @ClassRule
  public static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.2.0");
  private final String URL = "http://fakeurl";
  private final ObjectMapper mapper = new ObjectMapper();
  @Autowired
  MockMvc mockMvc;
  @Autowired
  MasterNodeController controller;
  @Autowired
  NodesRepository nodesRepo;
  @Autowired
  EncryptSeqRepository seqRepo;
  @MockBean
  NodeClient client;
  @SpyBean
  MasterNodeService service;

  @DynamicPropertySource
  public static void dynamicPropertySource(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.port", mongoDBContainer::getFirstMappedPort);
    registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
  }

  @BeforeClass
  public static void setupMongoContainer() throws IOException, InterruptedException {
    mongoDBContainer.execInContainer("mongo", "--eval", "db.createCollection('encryptionSequence')");
    mongoDBContainer.execInContainer("mongo", "--eval", "db.createCollection('encryptionNode')");
  }

  @Test
  public void shouldPassOnRegisteringNewNode() throws Exception {
    EncryptionNodeInfo info = getNodeInfo(getNewEncryptionNode());
    String content = mapper.writeValueAsString(info);
    mockMvc.perform(post("/notify")
          .content(content)
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
    assertTrue(nodesRepo.findByDecryptionPath(info.getDecryptionPath()).isPresent());
    EncryptionNode node = nodesRepo.findByDecryptionPath(info.getDecryptionPath()).get();
    assertEquals(info.getEncryptionPath(), node.getEncryptionPath());
    assertEquals(info.getDecryptionPath(), node.getDecryptionPath());
    assertEquals(info.getMonitorPath(), node.getMonitorPath());
  }

  @Test
  public void shouldPassOnGettingAllOperations() throws Exception {
    EncryptionNode node = nodesRepo.saveNode(getNewEncryptionNode());
    String expectedStr = mapper.writeValueAsString(getNodeInfo(node));
    MvcResult result = mockMvc.perform(get("/operations"))
          .andExpect(status().isOk())
          .andReturn();
    assertTrue(result.getResponse().getContentAsString().contains(expectedStr));
  }

  @Test
  public void shouldPassOnCheckingSequence() throws Exception {
    EncryptionNode node = nodesRepo.saveNode(getNewEncryptionNode());
    EncryptionNode node1 = nodesRepo.saveNode(getNewEncryptionNode());
    String seqId = seqRepo.saveSequence(Arrays.asList(node.getNodeId(), node1.getNodeId()));
    String nodeResBody = mapper.writeValueAsString(new MonitorResult("UP"));
    String resBody = mapper.writeValueAsString(new CanDecryptResponse("yes"));
    Response response = getNewResponse(200, URL, Optional.of(nodeResBody));
    Response response1 = getNewResponse(200, URL, Optional.of(nodeResBody));
    Mockito.when(client.makeRequest(Mockito.anyString()))
          .thenReturn(response).thenReturn(response1);
    mockMvc.perform(get("/decryptable/" + seqId))
          .andExpect(status().isOk())
          .andExpect(content().string(resBody));
  }

  @Test
  public void shouldFailOnCheckingSequenceWithClosedNode() throws Exception {
    EncryptionNode node = nodesRepo.saveNode(getNewEncryptionNode());
    EncryptionNode node1 = nodesRepo.saveNode(getNewEncryptionNode());
    String seqId = seqRepo.saveSequence(Arrays.asList(node.getNodeId(), node1.getNodeId()));
    String nodeResBody = mapper.writeValueAsString(new MonitorResult("DOWN"));
    String resBody = mapper.writeValueAsString(new CanDecryptResponse("no"));
    Response response = getNewResponse(200, URL, Optional.of(nodeResBody));
    Mockito.when(client.makeRequest(Mockito.anyString()))
          .thenReturn(response)
          .thenThrow(new ServerConnectionException("Server connection exception"));
    mockMvc.perform(get("/decryptable/" + seqId))
          .andExpect(status().isOk())
          .andExpect(content().string(resBody));
    mockMvc.perform(get("/decryptable/" + seqId))
          .andExpect(status().isOk())
          .andExpect(content().string(resBody));
  }

  @Test
  public void shouldFailOnCheckingSequenceWhoseNodesReturnNotOKCode() throws Exception {
    EncryptionNode node = nodesRepo.saveNode(getNewEncryptionNode());
    EncryptionNode node1 = nodesRepo.saveNode(getNewEncryptionNode());
    String seqId = seqRepo.saveSequence(Arrays.asList(node.getNodeId(), node1.getNodeId()));
    String resBody = mapper.writeValueAsString(new ErrorResponse("internal server error"));
    Response response = getNewResponse(500, URL, Optional.empty());
    Mockito.when(client.makeRequest(Mockito.anyString())).thenReturn(response);
    mockMvc.perform(get("/decryptable/" + seqId))
          .andExpect(status().is(500))
          .andExpect(content().string(resBody));
  }


  @Test
  public void shouldPassOnEncryptionByAccessibleNodes() throws Exception {
    EncryptionNode node = nodesRepo.saveNode(getNewEncryptionNode());
    EncryptionNode node1 = nodesRepo.saveNode(getNewEncryptionNode());
    List<String> genSeq = Arrays.asList(node.getNodeId(), node1.getNodeId());
    String encRes = "result2343bhb";
    String nodeEncResBody = mapper.writeValueAsString(new EncryptionResult(encRes));
    Response encResp = getNewResponse(200, URL, Optional.of(nodeEncResBody));
    Response encResp1 = getNewResponse(200, URL, Optional.of(nodeEncResBody));
    Mockito.doReturn(genSeq).when(service).getRandomNodesSeq();
    Mockito.when(client.makeRequest(Mockito.anyString()))
          .thenReturn(encResp).thenReturn(encResp1);
    mockMvc.perform(get("/encrypt/" + "string"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result").value(encRes));
  }

  @Test
  public void shouldFailOnEncryptionByNotAccessibleNodes() throws Exception {
    EncryptionNode node = nodesRepo.saveNode(getNewEncryptionNode());
    EncryptionNode node1 = nodesRepo.saveNode(getNewEncryptionNode());
    List<String> genSeq = Arrays.asList(node.getNodeId(), node1.getNodeId());
    String encRes = "result2343bhb";
    String nodeEncResBody = mapper.writeValueAsString(new EncryptionResult(encRes));
    Response encResponse = getNewResponse(200, URL, Optional.of(nodeEncResBody));
    Mockito.doReturn(genSeq).when(service).getRandomNodesSeq();
    Mockito.when(client.makeRequest(Mockito.anyString()))
          .thenReturn(encResponse)
          .thenThrow(new ServerConnectionException("Connection error"));
    mockMvc.perform(get("/encrypt/" + "string"))
          .andExpect(status().is(500));
  }

  @Test
  public void shouldPassOnDecryptionByAccessibleNodes() throws Exception {
    EncryptionNode node = nodesRepo.saveNode(getNewEncryptionNode());
    EncryptionNode node1 = nodesRepo.saveNode(getNewEncryptionNode());
    String seqId = seqRepo.saveSequence(
          Arrays.asList(node.getNodeId(), node1.getNodeId()));
    String encStr = "result2343bhb";
    String decRes = "string";
    String finalDecRes = mapper.writeValueAsString(new DecryptionResponse(decRes));
    String nodeDecRes = mapper.writeValueAsString(new DecryptionResult(decRes));
    String nodeCheckRes = mapper.writeValueAsString(new MonitorResult("UP"));
    Response decResponse = getNewResponse(200, URL, Optional.of(nodeDecRes));
    Response decResponse1 = getNewResponse(200, URL, Optional.of(nodeDecRes));
    Response checkResponse = getNewResponse(200, URL, Optional.of(nodeCheckRes));
    Response checkResponse1 = getNewResponse(200, URL, Optional.of(nodeCheckRes));
    Mockito.doReturn(decResponse).doReturn(decResponse1).when(client)
          .makeRequest(Mockito.argThat(s -> s.contains("decPath")));
    Mockito.doReturn(checkResponse).doReturn(checkResponse1).when(client)
          .makeRequest(Mockito.argThat(s -> s.contains("mPath")));
    mockMvc.perform(get(String.format("/decrypt/%1s/%2s", seqId, encStr)))
          .andExpect(status().isOk())
          .andExpect(content().string(finalDecRes));
  }

  @Test
  public void shouldPassOnDecryptionByNotAccessibleNodes() throws Exception {
    EncryptionNode node = nodesRepo.saveNode(getNewEncryptionNode());
    String seqId = seqRepo.saveSequence(Collections.singletonList(node.getNodeId()));
    String encStr = "result2343bhb";
    String errorMes = "internal server error";
    String nodeCheckRes = mapper.writeValueAsString(new MonitorResult("UP"));
    Response checkResponse = getNewResponse(200, URL, Optional.of(nodeCheckRes));
    Response checkResponse1 = getNewResponse(200, URL, Optional.of(nodeCheckRes));
    Mockito.doThrow(new ServerConnectionException("Connection error"))
          .when(client)
          .makeRequest(Mockito.argThat(s -> s.contains("decPath")));
    Mockito.doReturn(checkResponse).doReturn(checkResponse1).when(client)
          .makeRequest(Mockito.argThat(s -> s.contains("mPath")));
    String response = mockMvc.perform(get(String.format("/decrypt/%1s/%2s", seqId, encStr)))
          .andExpect(status().is(500))
          .andReturn().getResponse().getContentAsString();
    assertTrue(response.contains(errorMes) && response.contains("error"));
  }

  @Test
  public void shouldPassOnDecryptionByNodesThatReturnBadRequest() throws Exception {
    EncryptionNode node = nodesRepo.saveNode(getNewEncryptionNode());
    String seqId = seqRepo.saveSequence(Collections.singletonList(node.getNodeId()));
    String encStr = "result2343bhb";
    String errorMes = "wrong data";
    String errorNodeDecRes = mapper.writeValueAsString(new DecryptionErrorResult("wrong data"));
    String nodeCheckRes = mapper.writeValueAsString(new MonitorResult("UP"));
    Response decResponse = getNewResponse(400, URL, Optional.of(errorNodeDecRes));
    Response checkResponse = getNewResponse(200, URL, Optional.of(nodeCheckRes));
    Response checkResponse1 = getNewResponse(200, URL, Optional.of(nodeCheckRes));
    Mockito.doReturn(decResponse).when(client)
          .makeRequest(Mockito.argThat(s -> s.contains("decPath")));
    Mockito.doReturn(checkResponse).doReturn(checkResponse1).when(client)
          .makeRequest(Mockito.argThat(s -> s.contains("mPath")));
    String response = mockMvc.perform(get(String.format("/decrypt/%1s/%2s", seqId, encStr)))
          .andExpect(status().is(400))
          .andReturn().getResponse().getContentAsString();
    assertTrue(response.contains(errorMes) && response.contains("error"));
  }

  @Configuration
  @EnableAutoConfiguration
  public static class MasterNodeTestConfig {
    @Bean
    public MasterNodeController masterNodeController(EncryptSeqRepository enSeqRep,
                                                     NodesRepository nodesRep,
                                                     MasterNodeService service) {
      return new MasterNodeController(nodesRep, enSeqRep, service, new SimpleMeterRegistry());
    }

    @Bean
    public MasterNodeControllerAdvice masterNodeControllerAdvice() {
      return new MasterNodeControllerAdvice();
    }

    @Bean
    public MasterNodeService masterNodeService(NodesRepository nodesRepo,
                                               NodeResponseHandler handler,
                                               NodeClient client) {
      return new MasterNodeService(nodesRepo, handler, client);
    }

    @Bean
    public NodeClient nodeClient(OkHttpClient client) {
      return new NodeClient(client);
    }

    @Bean
    public NodeResponseHandler responseHandler() {
      return new NodeResponseHandler();
    }

    @Bean
    public OkHttpClient okHttpClient() {
      return new OkHttpClient();
    }
  }

}
