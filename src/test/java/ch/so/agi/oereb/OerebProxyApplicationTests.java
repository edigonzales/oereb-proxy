package ch.so.agi.oereb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
//@TestPropertySource("/application-test.properties")
class OerebProxyApplicationTests {
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }
    
    @Test
    public void getEgridByCoord_Ok() {
        
        String requestUrl = "http://localhost:" + port + "/getegrid/xml/?EN=2600595,1215629";
        
        System.out.println("****************************"+requestUrl);
        
        ResponseEntity<String> entity = restTemplate.getForEntity(requestUrl, String.class);
        
        assertTrue(entity.getBody().contains("CH807306583219"));
    }

    // READ ME!!!
    // Wegen ConfigurationProperties (??) wird immer auch das application.yml gelesen.
    // Was dazu führt, dass das application-test.yml hier keinen Einfluss hat.
    // Muss ich mir anschauen. Momentan noch nicht schlimm, da es immer noch Kantone
    // ohne OEREB-V2 gibt.
    
    // Koordinate liegt im Kanton Glarus. Der Service für GL ist nicht freigeschaltet in 
    // der Konfiguration.
    @Test
    public void getEgridByCoord_canton_not_available() {
        String requestUrl = "http://localhost:" + port + "/getegrid/xml/?EN=2724020,1210945";
        ResponseEntity<String> entity = restTemplate.getForEntity(requestUrl, String.class);
        
        assertEquals(204, entity.getStatusCode().value());
    }
    
    @Test
    public void getEgridByCoord_coord_outside_switzerland() {
        String requestUrl = "http://localhost:" + port + "/getegrid/xml/?EN=92600595,91215629";
        ResponseEntity<String> entity = restTemplate.getForEntity(requestUrl, String.class);
        
        assertEquals(204, entity.getStatusCode().value());
    }
    
    @Test
    public void extract_Ok() {
        
        String requestUrl = "http://localhost:" + port + "/extract/xml/?EGRID=CH807306583219";
        ResponseEntity<String> entity = restTemplate.getForEntity(requestUrl, String.class);
        
        assertTrue(entity.getBody().contains("ConcernedTheme"));
        assertTrue(entity.getBody().contains("CH807306583219"));
    }    
}
