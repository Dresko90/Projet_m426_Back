package ch.epai.ict.m295.message_api;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import ch.epai.ict.m295.message_api.domain.IdGeneratorManager;
import ch.epai.ict.m295.message_api.domain.User;
import ch.epai.ict.m295.message_api.domain.UserDirectory;
import ch.epai.ict.m295.message_api.data.SqlIdGenerator;
import ch.epai.ict.m295.message_api.data.SqlUserRepository;

@Configuration
public class RepositoryConfig {
    String url;
    String username;
    String password;

    public RepositoryConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Bean("userDirectory")
    public UserDirectory getUserDirectory() {
        DataSource dataSource = new DriverManagerDataSource(
                this.url,
                this.username,
                this.password);
        IdGeneratorManager.register(new SqlIdGenerator(dataSource, "user"), User.class);
        return new SqlUserRepository(dataSource);
    }
}
