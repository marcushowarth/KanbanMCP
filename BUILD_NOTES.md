# Build Notes

Notes from the initial project setup.

## Java Version

The plan originally targeted Java 21 (GraalVM CE 21.0.2 at `~/Library/Java/JavaVirtualMachines/graalvm-ce-21.0.2`). However, Maven picks up its JDK from `~/.mavenrc`, which is pinned to Temurin 17:

```
# ~/.mavenrc
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
```

Setting `JAVA_HOME` in the shell or via env prefix does not override `.mavenrc` for the Maven wrapper. The project was changed to target Java 17 (`<java.version>17</java.version>` in `pom.xml`).

To switch to a different JDK, update both:
1. `~/.mavenrc` — change the `JAVA_HOME` export
2. `pom.xml` — change `<java.version>`

## Maven Mirror

`~/.m2/settings.xml` contains a Vision 360 Nexus mirror:

```xml
<mirror>
    <id>central</id>
    <name>central</name>
    <url>https://nexus.vision-development.co.uk/nexus/repository/maven-public</url>
    <mirrorOf>*</mirrorOf>
</mirror>
```

All dependency downloads go through this mirror. It works and has all required artifacts (Spring AI, MCP SDK, etc.).

The Maven wrapper's `distributionUrl` in `.mvn/wrapper/maven-wrapper.properties` was initially also pointing at this Nexus — it was corrected to the official Maven repo at `repo.maven.apache.org`.

## Dependencies

The only non-Spring-Boot dependency is `spring-ai-starter-mcp-server-webmvc` (via the Spring AI BOM at 1.0.0). This pulls in:

- `spring-ai-mcp` — Spring AI MCP integration
- `mcp` (io.modelcontextprotocol SDK 0.10.0) — core MCP protocol
- `mcp-spring-webmvc` — WebMVC transport for MCP
- `spring-ai-model` / `spring-ai-commons` — Spring AI core
- Spring Boot Web (Tomcat) — embedded server

No extra HTTP client libraries — `KanboardClient` uses `java.net.http.HttpClient` built into the JDK.
