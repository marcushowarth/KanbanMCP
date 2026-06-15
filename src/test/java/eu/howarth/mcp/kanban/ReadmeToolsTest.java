package eu.howarth.mcp.kanban;

import eu.howarth.mcp.kanban.tools.BoardTools;
import eu.howarth.mcp.kanban.tools.CategoryTools;
import eu.howarth.mcp.kanban.tools.CommentTools;
import eu.howarth.mcp.kanban.tools.ExternalLinkTools;
import eu.howarth.mcp.kanban.tools.ProjectTools;
import eu.howarth.mcp.kanban.tools.SubtaskTools;
import eu.howarth.mcp.kanban.tools.TagTools;
import eu.howarth.mcp.kanban.tools.TaskLinkTools;
import eu.howarth.mcp.kanban.tools.TaskTools;
import io.quarkiverse.mcp.server.Tool;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Keeps README.md honest: every @Tool method must be documented in the tools
 * table, and the "Tools (N)" header count must match. Fails the build if a tool
 * is added or removed without updating the README. The tool set is derived from
 * the code (reflection over the tool beans), so drift is caught automatically.
 */
class ReadmeToolsTest {

    private static final List<Class<?>> TOOL_CLASSES = List.of(
            ProjectTools.class, BoardTools.class, TaskTools.class, CommentTools.class,
            SubtaskTools.class, ExternalLinkTools.class, TaskLinkTools.class,
            CategoryTools.class, TagTools.class);

    private static Set<String> toolNames() {
        return TOOL_CLASSES.stream()
                .flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                .filter(m -> m.isAnnotationPresent(Tool.class))
                .map(Method::getName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Test
    void readmeDocumentsEveryTool() throws Exception {
        String readme = Files.readString(Path.of("README.md"));
        Set<String> toolNames = toolNames();

        assertTrue(toolNames.size() >= 30, "expected at least 30 tools, found: " + toolNames.size());

        Set<String> missing = toolNames.stream()
                .filter(name -> !readme.contains("`" + name + "`"))
                .collect(Collectors.toCollection(TreeSet::new));
        assertTrue(missing.isEmpty(), "Tools not documented in README.md: " + missing);

        Matcher matcher = Pattern.compile("## Tools \\((\\d+)\\)").matcher(readme);
        assertTrue(matcher.find(), "README must have a '## Tools (N)' header");
        int documentedCount = Integer.parseInt(matcher.group(1));
        assertEquals(toolNames.size(), documentedCount,
                "README 'Tools (N)' count must match the number of @Tool methods");
    }
}
