package ch.epai.ict.m295.message_api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.epai.ict.m295.message_api.data.InMemoryUserDirectory;
import ch.epai.ict.m295.message_api.domain.UserDirectory;

@Configuration
public class RepositoryConfig {
    
    private String url;
    private String user;
    private String password;

    public RepositoryConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Bean("userDirectory")
    public UserDirectory getUserDirectory() {
        return new InMemoryUserDirectory(this.url, this.user, this.password);
    }
}
