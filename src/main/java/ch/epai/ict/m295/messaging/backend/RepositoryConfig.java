package ch.epai.ict.m295.messaging.backend;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import ch.epai.ict.m295.messaging.backend.data.SqlConversationRepository;
import ch.epai.ict.m295.messaging.backend.data.SqlIdGenerator;
import ch.epai.ict.m295.messaging.backend.data.SqlMessageRepository;
import ch.epai.ict.m295.messaging.backend.data.SqlTokenRepository;
import ch.epai.ict.m295.messaging.backend.data.SqlUserRepository;
import ch.epai.ict.m295.messaging.backend.domain.Conversation;
import ch.epai.ict.m295.messaging.backend.domain.ConversationRepository;
import ch.epai.ict.m295.messaging.backend.domain.IdGeneratorManager;
import ch.epai.ict.m295.messaging.backend.domain.Message;
import ch.epai.ict.m295.messaging.backend.domain.MessageRepository;
import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.UserRepository;
import ch.epai.ict.m295.messaging.backend.domain.security.Token;
import ch.epai.ict.m295.messaging.backend.domain.security.TokenRepository;

@Configuration
public class RepositoryConfig {
    
    private final DataSource dataSource;
    private final SqlUserRepository sqlUserRepository;
    private final SqlTokenRepository sqlTokenRepository;
    private final SqlConversationRepository sqlConversationRepository;
    private final SqlMessageRepository sqlMessageRepository;
    
    public RepositoryConfig(String url, String username, String password) {
        this.dataSource = new DriverManagerDataSource(url, username, password);
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);

        IdGeneratorManager.register(new SqlIdGenerator(jdbcTemplate, "user"), User.class);
        IdGeneratorManager.register(new SqlIdGenerator(jdbcTemplate, "token"), Token.class);
        IdGeneratorManager.register(new SqlIdGenerator(jdbcTemplate, "conversation"), Conversation.class);
        IdGeneratorManager.register(new SqlIdGenerator(jdbcTemplate, "message"), Message.class);

        this.sqlUserRepository = new SqlUserRepository(jdbcTemplate);
        this.sqlTokenRepository = new SqlTokenRepository(jdbcTemplate);
        this.sqlConversationRepository = new SqlConversationRepository(jdbcTemplate, transactionManager);
        this.sqlMessageRepository = new SqlMessageRepository(jdbcTemplate, transactionManager);

    }

    @Bean("userRepository")
    public UserRepository getUserRepository() {
        return this.sqlUserRepository;
    }

    @Bean("tokenRepository")
    public TokenRepository getTokenRepository() {
        return this.sqlTokenRepository;
    }

    @Bean("conversationRepository")
    public ConversationRepository getConversationRepository() {
        return this.sqlConversationRepository;
    }

    @Bean("messageRepository")
    public MessageRepository getMessageRepository() {
        return this.sqlMessageRepository;
    }
}
