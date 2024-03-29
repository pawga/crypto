## Micronaut 4.3.6 Documentation

- [User Guide](https://docs.micronaut.io/4.3.6/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.3.6/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.3.6/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---

- [Shadow Gradle Plugin](https://plugins.gradle.org/plugin/com.github.johnrengelman.shadow)
- [Micronaut Gradle Plugin documentation](https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/)
- [GraalVM Gradle Plugin documentation](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html)
## Feature validation documentation

- [Micronaut Validation documentation](https://micronaut-projects.github.io/micronaut-validation/latest/guide/)


## Feature flyway documentation

- [Micronaut Flyway Database Migration documentation](https://micronaut-projects.github.io/micronaut-flyway/latest/guide/index.html)

- [https://flywaydb.org/](https://flywaydb.org/)


## Feature data-r2dbc documentation

- [Micronaut Data R2DBC documentation](https://micronaut-projects.github.io/micronaut-data/latest/guide/#dbc)

- [https://r2dbc.io](https://r2dbc.io)


## Feature r2dbc documentation

- [Micronaut R2DBC documentation](https://micronaut-projects.github.io/micronaut-r2dbc/latest/guide/)

- [https://r2dbc.io](https://r2dbc.io)


## Feature swagger-ui documentation

- [Micronaut Swagger UI documentation](https://micronaut-projects.github.io/micronaut-openapi/latest/guide/index.html)

- [https://swagger.io/tools/swagger-ui/](https://swagger.io/tools/swagger-ui/)


## Feature test-resources documentation

- [Micronaut Test Resources documentation](https://micronaut-projects.github.io/micronaut-test-resources/latest/guide/)


## Feature mockito documentation

- [https://site.mockito.org](https://site.mockito.org)


## Feature jdbc-hikari documentation

- [Micronaut Hikari JDBC Connection Pool documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/index.html#jdbc)


## Feature openapi documentation

- [Micronaut OpenAPI Support documentation](https://micronaut-projects.github.io/micronaut-openapi/latest/guide/index.html)

- [https://www.openapis.org](https://www.openapis.org)


## Feature micronaut-aot documentation

- [Micronaut AOT documentation](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)


## Feature ksp documentation

- [Micronaut Kotlin Symbol Processing (KSP) documentation](https://docs.micronaut.io/latest/guide/#kotlin)

- [https://kotlinlang.org/docs/ksp-overview.html](https://kotlinlang.org/docs/ksp-overview.html)


## Feature serialization-jackson documentation

- [Micronaut Serialization Jackson Core documentation](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)

## swagger-ui documentation

- [swagger crypto ](http://localhost:8080/swagger-ui/index.html#/)
- [https swagger crypto ](https://localhost:8443/swagger-ui/index.html#/)

## Description
```
mn create-app --build=gradle_kotlin --jdk=21 --lang=kotlin \
--test=junit --features=flyway,graalvm,jdbc-hikari,mockito,ksp,\
logback,micronaut-aot,micronaut-http-validation,netty-server,postgres,\
data-r2dbc,r2dbc,serialization-jackson,shade,test-resources,yaml,\
openapi,swagger-ui,validation com.pawga.crypto

```


## externals info 
[Micronaut | 3 ways to upload files via HTTP and how to test it ](https://medium.com/nerd-for-tech/micronaut-3-ways-to-upload-files-via-http-ddfa6118ab99)

## Launching the application
```
docker run -d -p 8443:8443 crypto:latest
docker run -d -p 8443:8443 pawga777/crypto:latest
```
