package interpreter.view;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TextMenuTest {
    /** A command that only counts how many times it was executed. */
    private static class CountingCommand extends Command {
        int executions = 0;

        CountingCommand(String key, String description) {
            super(key, description);
        }

        @Override
        public void execute() {
            executions++;
        }
    }

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    /** Runs the menu on the given input lines (one per line) and returns everything it printed. */
    private String show(String input, Command... commands) {
        TextMenu menu = new TextMenu(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8));
        for (Command c : commands) menu.addCommand(c);
        menu.show();
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void listsTheCommandsInTheOrderTheyWereAdded() {
        String out = show("", new CountingCommand("b", "second"), new CountingCommand("a", "first"),
                new CountingCommand("10", "tenth"));
        int b = out.indexOf("b : second"), a = out.indexOf("a : first"), ten = out.indexOf("10 : tenth");
        assertTrue(0 <= b && b < a && a < ten);
    }

    @Test
    void printsTheKeyAndTheDescriptionOfEveryCommand() {
        String out = show("", new CountingCommand("1", "run example one"));
        assertTrue(out.contains("   1 : run example one"));
    }

    @Test
    void executesTheCommandOfTheTypedKeyAndNoOtherCommand() {
        var one = new CountingCommand("1", "one");
        var two = new CountingCommand("2", "two");
        show("2\n", one, two);
        assertEquals(0, one.executions);
        assertEquals(1, two.executions);
    }

    @Test
    void aCommandCanBeChosenSeveralTimes() {
        var one = new CountingCommand("1", "one");
        show("1\n1\n1\n", one);
        assertEquals(3, one.executions);
    }

    @Test
    void anInvalidOptionPrintsAMessageAndTheMenuContinues() {
        var one = new CountingCommand("1", "one");
        String out = show("x\n1\n", one);
        assertTrue(out.contains("Invalid option"));
        assertEquals(1, one.executions);
    }

    @Test
    void anEmptyLineIsAnInvalidOption() {
        var one = new CountingCommand("1", "one");
        String out = show("\n", one);
        assertTrue(out.contains("Invalid option"));
        assertEquals(0, one.executions);
    }

    @Test
    void spacesAroundTheKeyAreIgnored() {
        var one = new CountingCommand("1", "one");
        show("  1  \n", one);
        assertEquals(1, one.executions);
    }

    @Test
    void theMenuIsShownAgainBeforeEveryInput() {
        String out = show("1\n1\n", new CountingCommand("1", "one"));
        assertEquals(3, out.split("Input the option: ", -1).length - 1); // before 1st, 2nd and the end of input
    }

    @Test
    void endOfInputEndsTheMenuWithoutAnException() {
        assertDoesNotThrow(() -> show(""));
        assertDoesNotThrow(() -> show("1", new CountingCommand("1", "one"))); // last line without a newline
    }

    @Test
    void aMenuWithoutCommandsRejectsEveryInput() {
        String out = show("1\n");
        assertTrue(out.contains("Invalid option"));
    }
}
