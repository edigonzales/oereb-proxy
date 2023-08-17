package ch.so.agi.oereb;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private static final String PARAM_CONST_PDF = "pdf";
    private static final String PARAM_CONST_XML = "xml";
    private static final String PARAM_GNSS = "GNSS";
    private static final String PARAM_EN = "EN";
    private static final String PARAM_LOCALISATION = "LOCALISATION";
    private static final String PARAM_POSTALCODE = "POSTALCODE";
    private static final String PARAM_NUMBER = "NUMBER";
    private static final String PARAM_EGRID = "EGRID";
    private static final String METHOD_EXTRACT = "extract";
    private static final String METHOD_GETEGRID = "getegrid";

    @Value("${app.proxyMode}")
    private String proxyMode;

    @Value("${app.identifyServiceUrl}")
    private String identifyServiceUrl;

    @Value("${app.searchServiceUrl}")
    private String searchServiceUrl;

    @Value("${app.reframeServiceUrl}")
    private String reframeServiceUrl;

    @Autowired
    HttpClient httpClient;

    @Autowired
    ObjectMapper objectMapper;
    
    //@Autowired
    OerebServiceProperties oerebServiceProperties;
    
    public MainController(OerebServiceProperties oerebServiceProperties) {
        this.oerebServiceProperties = oerebServiceProperties;
    }

    @GetMapping("/")
    public ResponseEntity<String> ping() {
        log.info(proxyMode);
        return new ResponseEntity<String>("oereb-proxy", HttpStatus.OK);
    }
        
    @GetMapping("/{canton}/{method:getegrid|extract}/{format:xml|pdf}/")
    public ResponseEntity<Object> versions(@PathVariable String canton, @PathVariable String method, @PathVariable String format, @RequestParam Map<String, String> queryParameters) throws URISyntaxException, IOException, InterruptedException {
        log.debug("canton: {}", canton);
        log.debug("method: {}", method);
        log.debug("format: {}", format);
        
        if (method.equals(METHOD_GETEGRID) && format.equals(PARAM_CONST_PDF)) {
            throw new IllegalArgumentException("format not supported");
        }
        
        String serviceEndpoint = oerebServiceProperties.getServices().get(canton.toUpperCase());
        log.debug("service endpoint: {}", serviceEndpoint);

        if (serviceEndpoint == null) {
            return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
        }

        String requestUrl = serviceEndpoint + method + "/xml";

        int i=0;
        for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
            if (i==0) {
                requestUrl += "?";
            } else {
                requestUrl += "&";
            }
            requestUrl += entry.getKey() + "=" + entry.getValue();
            i++;
        }
        log.debug("ows "+method+" request url: {}", requestUrl);

        URI requestUri = new URI(requestUrl);

        if (proxyMode.equalsIgnoreCase("proxy")) {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
            requestBuilder.GET().uri(requestUri);
            HttpRequest request = requestBuilder.timeout(Duration.ofMinutes(2L)).build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            int statusCode = response.statusCode();
            String contentType = response.headers().firstValue("content-type").orElse("text/plain"); // Bewusst, zwecks Debugging.
            
            log.debug("ows "+method+" response status code: {}", statusCode);
            log.debug("ows "+method+" content type: {}", contentType);
            
            InputStreamResource inputStreamResource = new InputStreamResource(response.body());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", contentType);
            return new ResponseEntity<>(inputStreamResource, httpHeaders, HttpStatus.valueOf(statusCode));
        } else if (proxyMode.equalsIgnoreCase("redirect"))  {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(requestUri);
            return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);        
        } else {
            throw new IllegalArgumentException("not valid proxy mode: " + proxyMode);
        }
    }
    
    @GetMapping("/{canton}/{method:versions|capabilities}/xml")
    public ResponseEntity<Object> getMetaInfo(@PathVariable String canton, @PathVariable String method) throws URISyntaxException, IOException, InterruptedException {
        log.debug("canton: {}", canton);
        log.debug("method: {}", method);

        String serviceEndpoint = oerebServiceProperties.getServices().get(canton.toUpperCase());
        log.debug("service endpoint: {}", serviceEndpoint);

        if (serviceEndpoint == null) {
            return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
        }

        String requestUrl = serviceEndpoint + method + "/xml";
        log.debug("ows "+method+" request url: {}", requestUrl);

        URI requestUri = new URI(requestUrl);

        if (proxyMode.equalsIgnoreCase("proxy")) {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
            requestBuilder.GET().uri(requestUri);
            HttpRequest request = requestBuilder.timeout(Duration.ofMinutes(2L)).build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            int statusCode = response.statusCode();
            String contentType = response.headers().firstValue("content-type").orElse("text/plain"); // Bewusst, zwecks Debugging.
            
            log.debug("ows "+method+" response status code: {}", statusCode);
            log.debug("ows "+method+" content type: {}", contentType);
            
            InputStreamResource inputStreamResource = new InputStreamResource(response.body());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", contentType);
            return new ResponseEntity<>(inputStreamResource, httpHeaders, HttpStatus.valueOf(statusCode));
        } else if (proxyMode.equalsIgnoreCase("redirect"))  {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(requestUri);
            return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);        
        } else {
            throw new IllegalArgumentException("not valid proxy mode: " + proxyMode);
        }
    }
    
    @GetMapping("/extract/{format}/")
    public ResponseEntity<Object> getExtract(@PathVariable String format, @RequestParam Map<String, String> queryParameters) throws URISyntaxException, IOException, InterruptedException {
        if(!format.equals(PARAM_CONST_XML) && !format.equals(PARAM_CONST_PDF)) {
            throw new IllegalArgumentException("unsupported format <"+format+">");
        }
        
        String egrid = queryParameters.get(PARAM_EGRID);
        log.debug("egrid: {}", egrid);
        
        if (egrid == null) {
            throw new IllegalArgumentException("parameter EGRID expected");
        }
        
        String canton = null;
        try {
            canton = getCantonFromEgrid(egrid);
            log.debug("canton by egrid from rest service request: " + canton);
        } catch (NullPointerException e) {            
            return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
        }

        String serviceEndpoint = oerebServiceProperties.getServices().get(canton.toUpperCase());
        log.debug("service endpoint: {}", serviceEndpoint);
        
        if (serviceEndpoint == null) {
            return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
        }

        String requestUrl = serviceEndpoint + "extract/"+format+"/";

        int i=0;
        for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
            if (i==0) {
                requestUrl += "?";
            } else {
                requestUrl += "&";
            }
            requestUrl += entry.getKey() + "=" + entry.getValue();
            i++;
        }
        log.debug("ows extract request url: {}", requestUrl);

        URI requestUri = new URI(requestUrl);

        if (proxyMode.equalsIgnoreCase("proxy")) {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
            requestBuilder.GET().uri(requestUri);
            HttpRequest request = requestBuilder.timeout(Duration.ofMinutes(2L)).build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            int statusCode = response.statusCode();
            String contentType = response.headers().firstValue("content-type").orElse("text/plain"); // Bewusst, zwecks Debugging.
            
            log.debug("ows extract response status code: {}", statusCode);
            log.debug("ows extract content type: {}", contentType);
            
            InputStreamResource inputStreamResource = new InputStreamResource(response.body());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", contentType);
            return new ResponseEntity<>(inputStreamResource, httpHeaders, HttpStatus.valueOf(statusCode));
        } else if (proxyMode.equalsIgnoreCase("redirect"))  {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(requestUri);
            return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);        
        } else {
            throw new IllegalArgumentException("not valid proxy mode: " + proxyMode);
        }
    }    

    @GetMapping("/getegrid/xml/")
    public ResponseEntity<Object> getEgrid(@RequestParam Map<String, String> queryParameters) throws URISyntaxException, IOException, InterruptedException {
        String en = queryParameters.get(PARAM_EN);
        String gnss = queryParameters.get(PARAM_GNSS);
        String postalcode = queryParameters.get(PARAM_POSTALCODE);
        String localisation = queryParameters.get(PARAM_LOCALISATION);
        String number = queryParameters.get(PARAM_NUMBER);

        String canton = null;
        if (en != null || gnss != null) {
            // Koordinaten ggf. von WGS84 nach LV95 transfromieren.
            Coordinate coord;
            if (gnss != null) {
                double lon = Double.valueOf(gnss.split(",")[0]);
                double lat = Double.valueOf(gnss.split(",")[1]);
                coord = lv95ToWgs84(lon, lat);
                log.debug("coordinate after transformation: {}", coord);
            } else {
                coord = new Coordinate(Double.valueOf(en.split(",")[0]), Double.valueOf(en.split(",")[1]));
                log.debug("coordinate: {}", coord);
            }
            
            // Betroffener Kanton via geo.admin.ch rest api eruieren.
            // Falls der Kanton nicht gefunden werden kann, wird 204 
            // zurückgeliefert. Beim Parsen der Antwort werden Exceptions
            // geworfen, wenn kein Kanton in der Antwort vorhanden ist.
            try {
                canton = getCantonFromCoord(coord);
                log.debug("canton by coordinate from rest service request: " + canton);
            } catch (NullPointerException e) {            
                return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
            }
        } else if (postalcode != null && localisation != null) {
            //getEgridByAddress()            
            try {
                canton = getCantonByAddress(postalcode, localisation, number);
                log.debug("canton by address from rest service request: " + canton);
            } catch (NullPointerException e) {            
                return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
            }
        } else {
            throw new IllegalArgumentException("request is not supported");            
        }

        // ÖREB-Webservice-URL des betroffenen Kantons aus den Settings lesen.
        String serviceEndpoint = oerebServiceProperties.getServices().get(canton.toUpperCase());
        log.debug("service endpoint: {}", serviceEndpoint);
        
        if (serviceEndpoint == null) {
            return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
        }

        String requestUrl = serviceEndpoint + "getegrid/xml/";
        
        int i=0;
        for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
            if (i==0) {
                requestUrl += "?";
            } else {
                requestUrl += "&";
            }
            requestUrl += entry.getKey() + "=" + entry.getValue();
            i++;
        }
        log.debug("ows getegrid request url: {}", requestUrl);
        
        URI requestUri = new URI(requestUrl);

        if (proxyMode.equalsIgnoreCase("proxy")) {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
            requestBuilder.GET().uri(requestUri);
            HttpRequest request = requestBuilder.timeout(Duration.ofMinutes(2L)).build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            int statusCode = response.statusCode();
            String contentType = response.headers().firstValue("content-type").orElse("text/plain"); // Bewusst, zwecks Debugging.
            
            log.debug("ows getegrid response status code: {}", statusCode);
            log.debug("ows getegrid content type: {}", contentType);
            
            InputStreamResource inputStreamResource = new InputStreamResource(response.body());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", contentType);
            return new ResponseEntity<>(inputStreamResource, httpHeaders, HttpStatus.valueOf(statusCode));
        } else if (proxyMode.equalsIgnoreCase("redirect"))  {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(requestUri);
            return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);        
        } else {
            throw new IllegalArgumentException("not valid proxy mode: " + proxyMode);
        }
    }
    
    private String getCantonByAddress(String postalcode, String localisation, String number) throws IOException, InterruptedException, URISyntaxException {
        String requestUrl = (searchServiceUrl + postalcode + "%20" + localisation + (number!=null?"%20"+number:"")).trim();
        URI requestUri = new URI(requestUrl);
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder.GET().uri(requestUri);
        HttpRequest request = requestBuilder.timeout(Duration.ofMinutes(2L)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        HashMap<String,Object> responseObj = objectMapper.readValue(response.body(), HashMap.class);
        
        // Es können mehrere Adressen zurückgeliefert werden. Wir verwenden die erste.
        Coordinate coord = null;
        try {
            ArrayList<Object> resultList = (ArrayList<Object>) responseObj.get("results");
            HashMap<String,Object> attrs = (HashMap<String, Object>) ((HashMap<String,Object>)resultList.get(0)).get("attrs");
            double easting = (Double) attrs.get("y");
            double northing = (Double) attrs.get("x");
            coord = new Coordinate(easting, northing);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }

        String canton = getCantonFromCoord(coord);

        return canton;
    }
    
    private String getCantonFromEgrid(String egrid) throws URISyntaxException, IOException, InterruptedException {
        if (egrid.startsWith("LI")) {
            return "LI";
        }
        
        String requestUrl = searchServiceUrl + egrid;
        URI requestUri = new URI(requestUrl);
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder.GET().uri(requestUri);
        HttpRequest request = requestBuilder.timeout(Duration.ofMinutes(2L)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HashMap<String,Object> responseObj = objectMapper.readValue(response.body(), HashMap.class);
        
        Coordinate coord = null;
        try {
            ArrayList<Object> resultList = (ArrayList<Object>) responseObj.get("results");
            HashMap<String,Object> attrs = (HashMap<String, Object>) ((HashMap<String,Object>)resultList.get(0)).get("attrs");
            double easting = (Double) attrs.get("y");
            double northing = (Double) attrs.get("x");
            coord = new Coordinate(easting, northing);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        
        String canton = getCantonFromCoord(coord);

        return canton;
    }
    
    private String getCantonFromCoord(Coordinate coord) throws URISyntaxException, IOException, InterruptedException {
        String coordString = String.valueOf(coord.easting()) + "," + String.valueOf(coord.northing());
        String requestUrl = identifyServiceUrl + coordString;
        URI requestUri = new URI(requestUrl);
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder.GET().uri(requestUri);
        HttpRequest request = requestBuilder.timeout(Duration.ofMinutes(2L)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HashMap<String,Object> responseObj = objectMapper.readValue(response.body(), HashMap.class);
        
        String canton = null;
        try {
            ArrayList<Object> resultList = (ArrayList<Object>) responseObj.get("results");
            HashMap<String,Object> properties = (HashMap<String, Object>) ((HashMap<String,Object>)resultList.get(0)).get("properties");
            canton = (String) properties.get("kanton");
            String municipality = (String) properties.get("gemname");
            if (canton.isBlank() && !municipality.isBlank()) {
                return "LI";
            }
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        
        return canton;
    } 
    
    private Coordinate lv95ToWgs84(double easting, double northing) throws URISyntaxException, IOException, InterruptedException {
        String requestUrl = reframeServiceUrl + "?easting=" + String.valueOf(easting) + "&northing=" + String.valueOf(northing);
        URI requestUri = new URI(requestUrl);
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder.GET().uri(requestUri);
        HttpRequest request = requestBuilder.timeout(Duration.ofMinutes(2L)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HashMap<String,Object> responseObj = objectMapper.readValue(response.body(), HashMap.class);
        List<Double> coords = (List<Double>) responseObj.get("coordinates");
        
        return new Coordinate(coords.get(0), coords.get(1));        
    }
    
}
