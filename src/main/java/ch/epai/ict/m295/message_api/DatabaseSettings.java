package ch.epai.ict.m295.message_api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseSettings {

    @Value("${app.db.user}")
    private String user;

    @Value("${app.db.password}")
    private String password;

    @Value("${app.db.url}")
    private String url;

    @Bean("user")
    public String getUser() {
        return this.user;
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
