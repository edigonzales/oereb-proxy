package ch.so.agi.oereb;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class OerebProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(OerebProxyApplication.class, args);
    }
    
    @Bean 
    HttpClient createHttpClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .followRedirects(Redirect.NORMAL)
                .build();
        return httpClient;
    }
}
