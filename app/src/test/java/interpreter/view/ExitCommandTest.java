package interpreter.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** execute() calls System.exit(0), which would stop the test JVM, so only the properties are tested. */
class ExitCommandTest {
    @Test
    void hasTheKeyAndTheDescriptionItWasCreatedWith() {
        Command c = new ExitCommand("0", "exit");
        assertEquals("0", c.getKey());
        assertEquals("exit", c.getDescription());
    }
}
