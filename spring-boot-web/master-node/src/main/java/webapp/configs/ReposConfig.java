package webapp.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import webapp.repositories.EncryptSeqRepository;
import webapp.repositories.NodesRepository;

@Configuration
@EnableMongoRepositories(basePackageClasses = {NodesRepository.class, EncryptSeqRepository.class})
public class ReposConfig {
}
