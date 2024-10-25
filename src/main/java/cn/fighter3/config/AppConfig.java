package cn.fighter3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


import org.springframework.beans.factory.annotation.Value;


@Configuration
public class AppConfig {
    @Value("${wenxin.apikey}")
    private String apiKey;

    @Value("${wenxin.secretkey}")
    private String secretKey;

    // Getters and Setters
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
