package webapp.service;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import webapp.repositories.NodesRepository;

@Slf4j
@AllArgsConstructor
public class NodesRepoCleaner {
  private final NodesRepository nodesRepo;
  private final MasterNodeService service;
  private final MeterRegistry meterRegistry;
  private final AtomicLong aliveNodesCount = new AtomicLong(0);


  @Scheduled(fixedRate = 10000)
  public void cleanNodesRepository() {
    log.debug("Cleaning nodes repository");
    nodesRepo.findAll().stream()
          .filter(n -> !service.checkNode(n))
          .forEach(nodesRepo::deleteNode);
    meterRegistry.gauge("master-node.alive.nodes", aliveNodesCount);
    aliveNodesCount.set(nodesRepo.count());
  }
}
