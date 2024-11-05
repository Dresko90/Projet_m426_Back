package ch.epai.ict.m295.message_api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
