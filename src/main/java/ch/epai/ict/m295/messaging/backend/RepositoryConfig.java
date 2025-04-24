package ch.epai.ict.m295.messaging.backend;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import ch.epai.ict.m295.messaging.backend.data.SqlIdGenerator;
import ch.epai.ict.m295.messaging.backend.data.SqlUserRepository;
import ch.epai.ict.m295.messaging.backend.domain.IdGeneratorManager;
import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.UserRepository;
import ch.epai.ict.m295.messaging.backend.domain.security.TokenRepository;

@Configuration
public class RepositoryConfig {
    
    private SqlUserRepository sqlUserRepository;

    public RepositoryConfig(String url, String username, String password) {
        DataSource dataSource = new DriverManagerDataSource(url, username, password);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        IdGeneratorManager.register(new SqlIdGenerator(jdbcTemplate, "user"), User.class);
        IdGeneratorManager.register(new SqlIdGenerator(jdbcTemplate, "token"), User.class);

        this.sqlUserRepository = new SqlUserRepository(jdbcTemplate);
    }

    @Bean("userRepository")
    public UserRepository getUserRepository() {
        return this.sqlUserRepository;
    }

    @Bean("tokenRepository")
    public TokenRepository getUserTokenRepository() {
        return this.sqlUserRepository;
    }
}
