package eu.howarth.mcp.kanban;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KanbanApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KanbanApiApplication.class, args);
    }
}
