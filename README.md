# Thematic Plugin

## How can install the Thematic plugin?

From the root directory, execute:
```
mvn install
```

## How can I add the Thematic Plugin to an a existing project?

The project must be based on 13 (or later).
- In the essentials/pom.xml add
```
    <dependency>
      <groupId>com.dxpfc.thematic</groupId>
      <artifactId>dxpfc-thematic-plugin-essentials</artifactId>
      <version>2.X.X-SNAPSHOT</version>
    </dependency>
```
Don't forget to replace X.X with the plugin version installed 

- Rebuild and start you project 
- Open essentials (http://localhost:8080/essentials) and install the "Thematic" plugin under the Library
- Rebuild your project again
```
mvn verify && mvn cargo.run
```