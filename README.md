![](https://img.shields.io/github/license/CraftTogetherMC/TC-Portals?style=flat-square)
![](https://img.shields.io/github/last-commit/CraftTogetherMC/TC-Portals?style=flat-square)
![](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.craft-together-mc.de%2Fjob%2FTC%2520Portals&style=flat-square)
![](https://shields-io-visitor-counter.herokuapp.com/badge?page=CraftTogetherMC.TC-Portals&style=flat-square)
![](https://img.shields.io/spiget/download-size/107439?style=flat-square)
![](https://img.shields.io/spiget/downloads/107439?style=flat-square)
  
[Development Builds](https://ci.craft-together-mc.de/job/CTCommons) / 
[Javadocs](https://ci.craft-together-mc.de/job/CT%20Commons/javadoc) / 
[Modrinth](https://modrinth.com/plugin/ctcommons) / 
[SpigotMC](https://www.spigotmc.org/resources/ctcommons.107439/)
  
# CT-Commons
This is a library we use to create plugins for the [CraftTogetherMC](https://github.com/CraftTogetherMC) minecraft server

### Plugins using this library:
- [TC-Portals](https://modrinth.com/plugin/tc-portals)  
- [TC-Destinations](https://modrinth.com/plugin/tc-destinations)  

## Maven
```xml
<repositories>
    <repository>
        <id>ctogether</id>
        <url>https://maven.craft-together-mc.de/</url>
    </repository>
</repositories>
```   
```xml
<dependencies>
    <dependency>
        <groupId>de.crafttogether</groupId>
        <artifactId>CTCommons</artifactId>
        <version>1.0-BETA</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Includes:
- [Platform-abstraction-Layer](https://github.com/J0schlZ/Platform-abstraction-layer) (Library for abstracting away some server platform specific implementations)
- [HikariCP](https://github.com/brettwooldridge/HikariCP) (High-performance, JDBC connection pool)
- [MariaDB Connector/J](https://mariadb.com/kb/en/about-mariadb-connector-j/) (JDBC-Driver)
- [Adventure](https://docs.adventure.kyori.net) (Very neat UI-Framework for Bukkit/Bungeecord and more)
- [MiniMessage](https://docs.adventure.kyori.net/minimessage) (Text format to represent chat components)
- [BStats](https://bstats.org) (Plugin Metrics)
