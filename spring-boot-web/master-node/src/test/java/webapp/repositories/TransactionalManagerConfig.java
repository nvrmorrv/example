package webapp.repositories;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransactionalManagerConfig {

  @Bean
  public PlatformTransactionManager platformTransactionManager(MongoDatabaseFactory factory) {
    return new MongoTransactionManager(factory);
  }

}
