package ch.so.agi.oereb;

import java.net.http.HttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.proxyMode}")
    private String proxyMode;
    
    @Autowired
    HttpClient httpClient;
    
    @GetMapping("/")
    public ResponseEntity<String> ping() {
        log.info(proxyMode);
        return new ResponseEntity<String>("oereb-proxy", HttpStatus.OK);
    }

}
