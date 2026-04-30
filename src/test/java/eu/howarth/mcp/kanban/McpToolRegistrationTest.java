package eu.howarth.mcp.kanban;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class McpToolRegistrationTest {

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void allToolMethodsAreRegistered() {
        Set<String> registeredNames = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(tc -> tc.getToolDefinition().name())
                .collect(Collectors.toSet());

        // Find every @Tool-annotated method across all beans in the tools package
        Set<String> expectedNames = applicationContext.getBeansOfType(Object.class).values().stream()
                .filter(bean -> bean.getClass().getPackageName()
                        .equals("eu.howarth.mcp.kanban.tools"))
                .flatMap(bean -> Arrays.stream(bean.getClass().getDeclaredMethods()))
                .filter(m -> m.isAnnotationPresent(Tool.class))
                .map(Method::getName)
                .collect(Collectors.toSet());

        assertThat(expectedNames).isNotEmpty();
        assertThat(registeredNames).containsAll(expectedNames);
    }
}
