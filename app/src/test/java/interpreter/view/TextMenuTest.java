package interpreter.view;

import interpreter.model.exp.VarExp;
import interpreter.model.stmt.AssignStmt;
import interpreter.model.stmt.IStmt;
import interpreter.model.stmt.PrintStmt;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static interpreter.TestSupport.num;
import static org.junit.jupiter.api.Assertions.*;

class TextMenuTest {
    /** Feeds the given lines to a menu over the given programs and returns everything it printed. */
    private static String session(List<IStmt> programs, String... inputLines) {
        String input = String.join("\n", inputLines) + (inputLines.length == 0 ? "" : "\n");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new TextMenu(programs, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8)).run();
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void selectingAndRunningExample1DisplaysEveryStateAndTheResult() {
        String output = session(Examples.all(), "1", "1", "2", "0");
        assertTrue(output.contains("Selected program 1."));
        assertTrue(output.contains("ExeStack: {int v; (v = 2; print(v))}"));
        assertTrue(output.contains("SymTable: {v -> 0}"));
        assertTrue(output.contains("Execution finished. Out: {2}"));
    }

    @Test
    void example2ThroughTheMenuPrintsEighteen() {
        String output = session(Examples.all(), "1", "2", "2", "0");
        assertTrue(output.contains("Execution finished. Out: {18}"));
    }

    @Test
    void example3ThroughTheMenuPrintsTwo() {
        String output = session(Examples.all(), "1", "3", "2", "0");
        assertTrue(output.contains("Execution finished. Out: {2}"));
    }

    @Test
    void theSelectionListShowsAllThePrograms() {
        String output = session(Examples.all(), "1", "1", "0");
        assertTrue(output.contains("1. int v;"));
        assertTrue(output.contains("2. int a;"));
        assertTrue(output.contains("3. bool a;"));
    }

    @Test
    void runningWithoutASelectionIsReportedNotExecuted() {
        String output = session(Examples.all(), "2", "0");
        assertTrue(output.contains("No program selected"));
        assertFalse(output.contains("ExeStack:"));
    }

    @Test
    void anInvalidOptionDoesNotCrashTheMenu() {
        String output = session(Examples.all(), "abc", "7", "", "0");
        assertTrue(output.contains("Invalid option: \"abc\"."));
        assertTrue(output.contains("Invalid option: \"7\"."));
        assertTrue(output.contains("Goodbye."));
    }

    @Test
    void aProgramNumberThatIsNotANumberIsRejected() {
        String output = session(Examples.all(), "1", "x", "2", "0");
        assertTrue(output.contains("Not a number: \"x\"."));
        assertTrue(output.contains("No program selected"));
    }

    @Test
    void aProgramNumberOutOfRangeIsRejected() {
        String output = session(Examples.all(), "1", "4", "2", "0");
        assertTrue(output.contains("There is no program number 4."));
        assertTrue(output.contains("No program selected"));
    }

    @Test
    void aRejectedSelectionKeepsThePreviousOne() {
        String output = session(Examples.all(), "1", "2", "1", "99", "2", "0");
        assertTrue(output.contains("Execution finished. Out: {18}"));
    }

    @Test
    void aRuntimeErrorOfTheProgramIsShownAsAMessageAndTheMenuContinues() {
        IStmt faulty = new AssignStmt("x", num(1)); // x is not declared
        List<IStmt> programs = List.of(faulty, Examples.EX1);
        String output = session(programs, "1", "1", "2", "1", "2", "2", "0");
        assertTrue(output.contains("Error: variable x was not declared"));
        // the menu is still alive: the second program runs fine afterwards
        assertTrue(output.contains("Execution finished. Out: {2}"));
    }

    @Test
    void anExpressionErrorIsShownAsAMessage() {
        IStmt faulty = new PrintStmt(new VarExp("y"));
        String output = session(List.of(faulty), "1", "1", "2", "0");
        assertTrue(output.contains("Error: variable y is not defined"));
    }

    @Test
    void endOfInputEndsTheMenuWithoutAnException() {
        assertDoesNotThrow(() -> session(Examples.all()));
        assertDoesNotThrow(() -> session(Examples.all(), "1"));
    }

    @Test
    void exitEndsTheMenu() {
        String output = session(Examples.all(), "0", "1");
        assertTrue(output.contains("Goodbye."));
        assertFalse(output.contains("Program number"));
    }
}
