package webapp;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import webapp.service.NodesRepoCleaner;

@AllArgsConstructor
public class MasterNodeAppStartListener implements ApplicationListener<ContextRefreshedEvent> {
  private final NodesRepoCleaner nodesRepoCleaner;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    nodesRepoCleaner.cleanNodesRepository();
  }
}
