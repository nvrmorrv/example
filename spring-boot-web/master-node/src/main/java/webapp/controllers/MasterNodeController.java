package webapp.controllers;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import webapp.documents.EncryptionNode;
import webapp.repositories.EncryptSeqRepository;
import webapp.repositories.NodesRepository;
import webapp.service.MasterNodeService;
import webapp.service.dto.CanDecryptResponse;
import webapp.service.dto.DecryptionResponse;
import webapp.service.dto.EncryptionNodeInfo;
import webapp.service.dto.EncryptionResponse;
import webapp.service.exceptions.BadRequestException;

@RestController
@AllArgsConstructor
@Slf4j
public class MasterNodeController {
  private final NodesRepository nodesRepo;
  private final EncryptSeqRepository seqRepo;
  private final MasterNodeService service;
  private final MeterRegistry meterRegistry;

  @PostMapping("/notify")
  @NewSpan
  public void registerNode(@RequestBody EncryptionNodeInfo info) {
    log.debug("Registering new node request: enPath: {}, decPath: {}, checkPath: {}",
          info.getEncryptionPath(),
          info.getDecryptionPath(),
          info.getMonitorPath());
    Optional<EncryptionNode> node = nodesRepo.findByDecryptionPath(info.getDecryptionPath());
    if (!node.isPresent()) {
      nodesRepo.saveNode(new EncryptionNode(
            info.getEncryptionPath(),
            info.getDecryptionPath(),
            info.getMonitorPath()));
    } else {
      log.debug("Notifying node is in repository already, id: {}", node.get().getNodeId());
    }
  }

  @GetMapping("/encrypt/{string}")
  @ResponseBody
  @NewSpan
  public EncryptionResponse encrypt(@PathVariable(name = "string") String string) {
    Timer.Sample sample = Timer.start(meterRegistry);
    meterRegistry.counter("master-node.encryption.requestCount").increment();
    log.debug("Encryption request");
    List<String> seq = service.getRandomNodesSeq();
    log.debug("Encryption sequence : {}",
          seq.stream().reduce((s1, s2) -> s1 + ", " + s2).get());
    String result = service.performEncryption(seq, string);
    log.debug("Encryption is done successfully");
    String seqId = seqRepo.saveSequence(seq);
    sample.stop(meterRegistry.timer("master-node.encryptionTime"));
    return new EncryptionResponse(seqId, result);
  }

  @GetMapping("/operations")
  @ResponseBody
  @NewSpan
  public List<EncryptionNodeInfo> getAllNodes() {
    log.debug("Encryption operations request.");
    return service.getOperationList(nodesRepo.findAll());
  }

  @GetMapping("/decrypt/{seqId}/{string}")
  @ResponseBody
  @NewSpan
  public DecryptionResponse decrypt(@PathVariable(name = "string") String string,
                                    @PathVariable(name = "seqId") String seqId) {
    Timer.Sample sample = Timer.start(meterRegistry);
    meterRegistry.counter("master-node.decryption.requestCount").increment();
    log.debug("Decryption request: seqId : {}", seqId);
    List<String> seq = seqRepo.getSequence(seqId);
    log.debug("Sequence in enc order : {}",
          seq.stream().reduce((s1, s2) -> s1 + ", " + s2).get());
    if (!service.checkSequence(seq)) {
      throw new BadRequestException("Some of decryption nodes is not accessible already");
    }
    String result = service.performDecryption(seq, string);
    log.debug("Decryption is done successfully");
    meterRegistry.counter("master-node.decryption.successCount").increment();
    sample.stop(meterRegistry.timer("master-node.decryptionTime"));
    return new DecryptionResponse(result);
  }

  @GetMapping("/decryptable/{seqId}")
  @ResponseBody
  @NewSpan
  public CanDecryptResponse canDecrypt(@PathVariable(name = "seqId") String seqId) {
    log.debug("Checking request: seqId : {}", seqId);
    List<String> seq = seqRepo.getSequence(seqId);
    boolean result = service.checkSequence(seq);
    log.debug("Result : {}", result);
    return result ? new CanDecryptResponse("yes") :
          new CanDecryptResponse("no");
  }

}
