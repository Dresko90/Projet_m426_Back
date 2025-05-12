package ch.epai.ict.m295.messaging.backend;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@SpringBootApplication
public class MessagingApp {

    private final DataSource dataSource;

    public MessagingApp(String url, String username, String password) {
        this.dataSource = new DriverManagerDataSource(url, username, password);
    }
    
    @Bean
    public DataSource getDataSource() {
        return dataSource;
    }

    public static void main(String[] args) {
        SpringApplication.run(MessagingApp.class, args);
    }

}
