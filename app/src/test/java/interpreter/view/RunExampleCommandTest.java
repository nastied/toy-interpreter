package interpreter.view;

import interpreter.controller.Controller;
import interpreter.model.PrgState;
import interpreter.model.stmt.AssignStmt;
import interpreter.model.stmt.IStmt;
import interpreter.model.value.IntValue;
import interpreter.repository.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static interpreter.TestSupport.num;
import static org.junit.jupiter.api.Assertions.*;

class RunExampleCommandTest {
    @TempDir
    Path dir;

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);

    private Path log() {
        return dir.resolve("log.txt");
    }

    private RunExampleCommand command(PrgState state, String logPath) {
        var controller = new Controller(new Repository(state, logPath), false, out);
        return new RunExampleCommand("1", state.getOriginalProgram().toString(), controller, out);
    }

    private String output() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void hasTheKeyAndTheDescriptionItWasCreatedWith() {
        PrgState state = PrgState.of(Examples.EX1);
        RunExampleCommand c = command(state, log().toString());
        assertEquals("1", c.getKey());
        assertEquals("(int v; (v = 2; print(v)))", c.getDescription());
    }

    @Test
    void executeRunsTheWholeProgram() throws Exception {
        PrgState state = PrgState.of(Examples.EX2);
        command(state, log().toString()).execute();
        assertTrue(state.getExeStack().isEmpty());
        assertEquals(new IntValue(18), state.getOut().get(0));
    }

    @Test
    void executeWritesTheLogFile() throws IOException {
        command(PrgState.of(Examples.EX1), log().toString()).execute();
        assertTrue(Files.readString(log()).contains("ExeStack:"));
    }

    @Test
    void anExampleRunsOnlyOnce() throws IOException {
        RunExampleCommand c = command(PrgState.of(Examples.EX1), log().toString());
        c.execute();
        String logAfterFirstRun = Files.readString(log());
        c.execute();
        assertEquals(logAfterFirstRun, Files.readString(log())); // nothing was run or logged again
        assertTrue(output().contains("This example has already been executed."));
    }

    @Test
    void theFirstExecutionDoesNotPrintTheAlreadyExecutedMessage() {
        command(PrgState.of(Examples.EX1), log().toString()).execute();
        assertFalse(output().contains("already been executed"));
    }

    @Test
    void anErrorOfTheProgramIsPrintedAndNotThrown() {
        IStmt faulty = new AssignStmt("x", num(1)); // x is not declared
        RunExampleCommand c = command(PrgState.of(faulty), log().toString());
        assertDoesNotThrow(c::execute);
        assertTrue(output().contains("Error: variable x was not declared"));
    }

    @Test
    void anExampleThatFailedIsNotRunAgain() {
        RunExampleCommand c = command(PrgState.of(new AssignStmt("x", num(1))), log().toString());
        c.execute();
        c.execute();
        assertTrue(output().contains("This example has already been executed."));
    }

    @Test
    void aLogFileErrorIsPrintedAndNotThrown() {
        String bad = dir.resolve("no-such-folder").resolve("log.txt").toString();
        RunExampleCommand c = command(PrgState.of(Examples.EX1), bad);
        assertDoesNotThrow(c::execute);
        assertTrue(output().contains("Error: cannot write the log file"));
    }

    @Test
    void theDefaultConstructorUsesTheStandardOutput() {
        PrgState state = PrgState.of(Examples.EX1);
        var controller = new Controller(new Repository(state, log().toString()), false);
        RunExampleCommand c = new RunExampleCommand("1", "one", controller);
        assertDoesNotThrow(c::execute);
        assertTrue(state.getExeStack().isEmpty());
    }
}
