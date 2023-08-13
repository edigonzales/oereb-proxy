# oereb-proxy

- https://oereb.geo.bl.ch/getegrid/xml/?EN=2611819,1260126
- https://geo.so.ch/api/oereb/getegrid/xml/?EN=2600595,1215629
- https://geo.so.ch/api/oereb/getegrid/xml/?IDENTDN=SO0200002457&NUMBER=168
- https://geo.so.ch/api/oereb/getegrid/xml/?GNSS=7.44646,47.09171
- https://map.geo.tg.ch/services/oereb/getegrid/xml/?EN=2709869,1268547
- https://rdppfvs.geopol.ch/getegrid/xml/?EN=2643445,1130616


- http://localhost:8080/getegrid/xml/?EN=2600595,1215629
- http://localhost:8080/getegrid/xml/?IDENTDN=SO0200002457&NUMBER=168
- http://localhost:8080/getegrid/xml/?GNSS=7.44646,47.09171

- https://geo.so.ch/api/oereb/extract/xml/?EGRID=CH807306583219
- http://localhost:8080/extract/xml/?EGRID=CH807306583219
- https://oereb.geo.bl.ch/extract/xml/?EGRID=CH767982496078
- http://localhost:8080/extract/xml/?EGRID=CH767982496078


## Unterschiede zur Spez

## Verhalten

- Falls Kanton nicht "freigeschaltet", wird 204 zurückgeliefert.

## Beschreibung

Das Repository verwaltet den Quellcode der Datensuche. Die Datensuche ist das Web-GUI zum Beziehen von Geodaten.

## Komponenten

Die Datensuche besteht aus einer einzelnen Komponente (einer Webanwendung). Sie wiederum ist Bestandteil der funktionalen Einheit "Datenbezug" (https://github.com/sogis/dok/blob/dok/dok_funktionale_einheiten/Documents/Datenbezug/Datenbezug.md).

## Konfigurieren und Starten

Die Anwendung kann am einfachsten mittels Env-Variablen gesteuert werden. Es stehen aber auch die normalen Spring Boot Konfigurationsmöglichkeiten zur Verfügung (siehe "Externalized Configuration").

| Name | Beschreibung | Standard |
|-----|-----|-----|
| `CONFIG_FILE` | Vollständiger, absoluter Pfad der Themebereitstellungs-Konfigurations-XML-Datei. | `/config/datasearch.xml` |
| `ITEMS_GEOJSON_DIR` | Verzeichnis, in das die GeoJSON-Dateien der Regionen gespeichert werden. Sämtliche JSON-Dateien in diesem Verzeichnis werden öffentlich exponiert. | `#{systemProperties['java.io.tmpdir']}` (= Temp-Verzeichnis des OS) |
| ~~`FILES_SERVER_URL`~~ | ~~Url des Servers, auf dem die Geodaten gespeichert sind.~~ | ~~`https://files.geo.so.ch`~~ |

### Java

Falls die _datasearch.xml_-Datei im Verzeichnis _/config/_ vorliegt, reicht:
```
java -jar sodata-server/target/sodata.jar 
```

Ansonsten kann die Datei explizit angegeben werden:

```
java -jar sodata-server/target/sodata.jar --app.configFile=/path/to/datasearch.xml
```

### Native Image

Analog Java:

```
./sodata-server/target/sodata-server [...]
```

### Docker

Die _datasearch.xml_-Datei kann direkt in das Image gebrannt werden. In diesem Fall sollte sie in den Ordner _/config/_ gebrannt werden, was zu folgendem Start-Befehl führt:

```
docker run -p8080:8080 sogis/sodata:latest
```

Wird die Datei nicht in das Image gebrannt, ergibt sich folgender Befehl:

```
docker run -p8080:8080 -v /path/to/datasearch.xml:/config/datasearch.xml sogis/sodata:latest
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

