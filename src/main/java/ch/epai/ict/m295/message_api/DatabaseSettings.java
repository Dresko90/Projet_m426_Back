package ch.epai.ict.m295.message_api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseSettings {

    @Value("${app.db.username}")
    private String username;

    @Value("${app.db.password}")
    private String password;

    @Value("${app.db.url}")
    private String url;

    @Bean("username")
    public String getUserName() {
        return this.username;
    }

    @Bean("password")
    public String getPassword() {
        return this.password;
    }

    @Bean("url")
    public String getUrl() {
        return this.url;
    }
}
