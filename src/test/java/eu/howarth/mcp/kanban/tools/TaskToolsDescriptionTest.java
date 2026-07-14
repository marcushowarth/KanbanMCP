package eu.howarth.mcp.kanban.tools;

import io.quarkiverse.mcp.server.Tool;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kanban #945: updateTask has no columnId param, but nothing said so — calling
 * it with a bogus columnId silently succeeds (returns true, task stays put)
 * instead of erroring, with no clue moveTaskPosition/moveTaskToProject is the
 * right tool for that. Pins a catch-all pointer in the description rather than
 * enumerating every unsupported field.
 */
class TaskToolsDescriptionTest {

    @Test
    void updateTaskDescriptionPointsToTheRightToolForColumnMoves() throws NoSuchMethodException {
        Method updateTask = TaskTools.class.getMethod(
                "updateTask", int.class, String.class, String.class, String.class,
                Integer.class, String.class, Integer.class, Integer.class);
        String description = updateTask.getAnnotation(Tool.class).description();
        assertTrue(description.contains("moveTaskPosition"),
                "updateTask description should point agents at moveTaskPosition/moveTaskToProject "
                        + "for column moves, was: " + description);
    }
}
