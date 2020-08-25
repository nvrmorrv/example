package webapp.service;

import static org.junit.Assert.assertEquals;
import static webapp.HelpMethods.getNodes;
import static webapp.HelpMethods.setNodesId;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import webapp.documents.EncryptionNode;
import webapp.repositories.NodesRepository;

@RunWith(MockitoJUnitRunner.class)
public class NodesRepoCleanerTest {

  @Mock
  MasterNodeService service;

  @Mock
  NodesRepository repository;

  @Mock
  MeterRegistry registry;

  @Captor
  ArgumentCaptor<EncryptionNode> deleteMethodArgument;

  @Test
  public void shouldPassOnCleaningTheRepository() {
    NodesRepoCleaner cleaner = new NodesRepoCleaner(repository, service, registry);
    List<EncryptionNode> nodes = getNodes(4);
    setNodesId(nodes);
    Mockito.when(repository.findAll()).thenReturn(nodes);
    Mockito.when(service.checkNode(nodes.get(0))).thenReturn(true);
    Mockito.when(service.checkNode(nodes.get(1))).thenReturn(false);
    Mockito.when(service.checkNode(nodes.get(2))).thenReturn(true);
    Mockito.when(service.checkNode(nodes.get(3))).thenReturn(false);
    cleaner.cleanNodesRepository();
    Mockito.verify(repository, Mockito.times(2)).deleteNode(deleteMethodArgument.capture());
    assertEquals(Arrays.asList(nodes.get(1), nodes.get(3)), deleteMethodArgument.getAllValues());
  }

}
