package webapp.configs;

import brave.http.HttpTracing;
import brave.okhttp3.TracingInterceptor;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import webapp.MasterNodeAppStartListener;
import webapp.controllers.MasterNodeController;
import webapp.controllers.MasterNodeControllerAdvice;
import webapp.repositories.EncryptSeqRepository;
import webapp.repositories.NodesRepository;
import webapp.service.MasterNodeService;
import webapp.service.NodeClient;
import webapp.service.NodeResponseHandler;
import webapp.service.NodesRepoCleaner;


@Configuration
@Import({ReposConfig.class})
@EnableScheduling
public class AppConfig {
  @Bean
  public MasterNodeController masterNodeController(EncryptSeqRepository enSeqRep,
                                                   NodesRepository nodesRep,
                                                   MasterNodeService service,
                                                   PrometheusMeterRegistry registry) {
    return new MasterNodeController(nodesRep, enSeqRep, service, registry);
  }

  @Bean
  public MasterNodeControllerAdvice masterNodeControllerAdvice() {
    return new MasterNodeControllerAdvice();
  }

  @Bean
  public MasterNodeAppStartListener masterNodeAppStartListener(NodesRepoCleaner nodesRepoCleaner) {
    return new MasterNodeAppStartListener(nodesRepoCleaner);
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
  public NodesRepoCleaner nodesRepoCleaner(NodesRepository repository,
                                           MasterNodeService service,
                                           PrometheusMeterRegistry registry) {
    return new NodesRepoCleaner(repository, service, registry);
  }

  @Bean
  public OkHttpClient okHttpClient(HttpTracing httpTracing) {
    return new OkHttpClient.Builder()
          .dispatcher(new Dispatcher(
                httpTracing.tracing().currentTraceContext()
                      .executorService(new Dispatcher().executorService())
          ))
          .addNetworkInterceptor(TracingInterceptor.create(httpTracing))
          .build();
  }
}
