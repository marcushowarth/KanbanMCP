package eu.howarth.mcp.kanban;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Keeps README.md honest: every registered MCP tool must be documented in the
 * tools table, and the "Tools (N)" header count must match. Fails the build if
 * a tool is added or removed without updating the README.
 */
@SpringBootTest
class ReadmeToolsTest {

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @Test
    void readmeDocumentsEveryRegisteredTool() throws Exception {
        String readme = Files.readString(Path.of("README.md"));

        Set<String> registeredNames = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(tc -> tc.getToolDefinition().name())
                .collect(Collectors.toSet());

        Set<String> missing = registeredNames.stream()
                .filter(name -> !readme.contains("`" + name + "`"))
                .collect(Collectors.toSet());

        assertThat(missing)
                .as("Tools registered but not documented in README.md")
                .isEmpty();

        Matcher matcher = Pattern.compile("## Tools \\((\\d+)\\)").matcher(readme);
        assertThat(matcher.find()).as("README must have a '## Tools (N)' header").isTrue();
        int documentedCount = Integer.parseInt(matcher.group(1));
        assertThat(documentedCount)
                .as("README 'Tools (N)' count must match the number of registered tools")
                .isEqualTo(registeredNames.size());
    }
}
