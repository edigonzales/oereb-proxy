# oereb-proxy

## Beschreibung

_OEREB-Proxy_ ist ein einfacher Proxy-Service für die ÖREB-Kataster Webservice der Kantone. Es gibt zwei Modi: "proxy" und "redirect". "Redirect" leitet zum kantonalen Webservice weiter. Im "proxy"-Modus wird der eigentliche Request vom _OEREB-Proxy_-Server gemacht und zurück an den Client geschickt. Anhand eines Requests muss der Kanton eruiert werden. Dazu wird die REST-API der BGDI verwendet.

Ist ein Kanton nicht im Proxy freigeschaltet und es wird via REST-API ein Kanton gefunden, ist der Rückgabe Status Code "204". Auch wenn die Koordinate nicht innerhalb eines Kantones liegt, wird "204" zurückgeliefert.

CORS wurde für GET- und OPTIONS-Request freigeschaltet, damit man die Antwort (im "proxy"-Modus) auch in einer Web-Anwendung verwenden kann.

Was wird unterstützt?

Requests:
 - GetEgrid
 - Extract

Formate:
 - XML
 - PDF

 Aufrufe:
 - EN, GNSS und IdentDN und GB-Nummer für GetEgrid. Meines Erachtes ist es nicht möglich eindeutig den EGRID via BGDI und Adresse herauszufinden.

Beispiele:

 - http://localhost:8080/getegrid/xml/?EN=2600595,1215629
 - http://localhost:8080/getegrid/xml/?EN=2757735,1224129 (LI)
 - http://localhost:8080/getegrid/xml/?IDENTDN=SO0200002457&NUMBER=168
 - http://localhost:8080/getegrid/xml/?GNSS=7.44646,47.09171
 - http://localhost:8080/extract/xml/?EGRID=CH807306583219
 - http://localhost:8080/extract/xml/?EGRID=CH767982496078

 - https://oereb.geo.bl.ch/getegrid/xml/?EN=2611819,1260126
 - https://geo.so.ch/api/oereb/getegrid/xml/?EN=2600595,1215629
 - https://geo.so.ch/api/oereb/getegrid/xml/?IDENTDN=SO0200002457&NUMBER=168
 - https://geo.so.ch/api/oereb/getegrid/xml/?GNSS=7.44646,47.09171
 - https://map.geo.tg.ch/services/oereb/getegrid/xml/?EN=2709869,1268547
 - https://rdppfvs.geopol.ch/getegrid/xml/?EN=2643445,1130616
 - https://geo.so.ch/api/oereb/extract/xml/?EGRID=CH807306583219
 - https://oereb.geo.bl.ch/extract/xml/?EGRID=CH767982496078


## Komponenten

todo

## Konfigurieren und Starten

Die Anwendung kann am einfachsten mittels Env-Variablen gesteuert werden. Es stehen aber auch die normalen Spring Boot Konfigurationsmöglichkeiten zur Verfügung (siehe "Externalized Configuration").

| Name | Beschreibung | Standard |
|-----|-----|-----|
| `PROXY_MODE` | Proxy-Modus. | `proxy` |
| `CANTON_SERVICE_URL` | BGDI-REST-API, um Kanton anhand einer Koordinate zu eruieren. | `https://api3.geo.admin.ch/rest/services/all/MapServer/identify?geometryFormat=geojson&geometryType=esriGeometryPoint&lang=en&layers=all:ch.swisstopo.swissboundaries3d-kanton-flaeche.fill&limit=1&returnGeometry=false&sr=2056&tolerance=0&geometry=` |
| `EGRID_SERVICE_URL` | BGDI-REST-API, um Koordinate anhand eines EGRID zu eruieren. | `https://api3.geo.admin.ch/rest/services/ech/SearchServer?sr=2056&lang=en&type=locations&searchText=` |
| `REFRAME_SERVICE_URL` | BGDI-REST-API, um Koordinate von WGS84 nach LV95 zu transformieren. | `https://geodesy.geo.admin.ch/reframe/wgs84tolv95` |
| `LOG_LEVEL_APP` | Loglevel der eigenen Businesslogik. | `DEBUG` |
| `LOG_LEVEL_FRAMEWORK` | Loglevel des Frameworks. | `INFO` |
| `TOMCAT_THREADS_MAX` | When you know, you know. | `50` |
| `TOMCAT_ACCEPT_COUNT` | ... | `100` |
| `TOMCAT_MAX_CONNECTIONS` | ... | `2000` |

Die Kantone sind im Property `oereb.service` als Map konfiguriert.

### Java

```
java -jar build/libs/oereb-proxy-0.0.X-exec.jar
```

### Native Image


```
./build/native/nativeCompile/oereb-proxy
```

### Docker

```
docker run -p8080:8080 sogis/oereb-proxy[-jvm]:latest
```

## Externe Abhängigkeiten

- REST-API geo.admin.ch:
 * Identify
 * Search
 * Reframe

## Konfiguration und Betrieb in der GDI

todo: Link auf Openshift Templates und nginx.

## Interne Struktur

todo

## Entwicklung

### Run 

`./gradlew bootRun` oder mit STS (Eclipse)

### Build

#### JVM
```
./gradlew clean build
```

```
docker build -t sogis/oereb-proxy-jvm:latest -f Dockerfile.jvm .
```


#### Native
```
./gradlew compileNative
```

```
docker build -t sogis/oereb-proxy:latest -f Dockerfile.native .
```

