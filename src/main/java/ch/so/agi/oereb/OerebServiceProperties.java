package ch.so.agi.oereb;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oereb")
public class OerebServiceProperties {
    private List<Map<String,String>> services;

    public List<Map<String, String>> getServices() {
        return services;
    }

    public void setServices(List<Map<String, String>> services) {
        this.services = services;
    }
}