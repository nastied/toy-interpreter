package interpreter.controller;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.VarExp;
import interpreter.model.stmt.AssignStmt;
import interpreter.model.stmt.CloseRFileStmt;
import interpreter.model.stmt.CompStmt;
import interpreter.model.stmt.IStmt;
import interpreter.model.stmt.NopStmt;
import interpreter.model.stmt.OpenRFileStmt;
import interpreter.model.stmt.PrintStmt;
import interpreter.model.stmt.ReadFileStmt;
import interpreter.model.type.IntType;
import interpreter.model.stmt.VarDeclStmt;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import interpreter.model.value.StringValue;
import interpreter.repository.Repository;
import interpreter.view.Examples;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static interpreter.TestSupport.closeOpenFiles;
import static interpreter.TestSupport.emptyState;
import static interpreter.TestSupport.num;
import static interpreter.TestSupport.str;
import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {
    @TempDir
    Path dir; // a fresh folder for the log file and the input files of every test

    private final List<PrgState> states = new ArrayList<>();

    @AfterEach
    void closeFiles() throws IOException {
        for (PrgState s : states) closeOpenFiles(s); // so that @TempDir can delete the files on Windows
    }

    private Path logFile() {
        return dir.resolve("log.txt");
    }

    private PrgState newState(IStmt program) {
        PrgState state = PrgState.of(program);
        states.add(state);
        return state;
    }

    private Repository repoFor(PrgState state) {
        return new Repository(state, logFile().toString());
    }

    private PrgState run(IStmt program) throws MyException {
        PrgState state = newState(program);
        new Controller(repoFor(state), false).allStep(); // display off
        return state;
    }

    private Path inputFile(String content) throws IOException {
        Path in = dir.resolve("test.in");
        Files.writeString(in, content);
        return in;
    }

    // ----- the examples of A2 -----

    @Test
    void example1PrintsTwo() throws MyException {
        PrgState state = run(Examples.EX1);
        assertTrue(state.getExeStack().isEmpty());
        assertEquals(1, state.getOut().size());
        assertEquals(new IntValue(2), state.getOut().get(0));
        assertEquals(new IntValue(2), state.getSymTable().lookup("v"));
    }

    @Test
    void example2PrintsEighteen() throws MyException {
        PrgState state = run(Examples.EX2);
        assertTrue(state.getExeStack().isEmpty());
        assertEquals(1, state.getOut().size());
        assertEquals(new IntValue(18), state.getOut().get(0));
        assertEquals(new IntValue(17), state.getSymTable().lookup("a"));
        assertEquals(new IntValue(18), state.getSymTable().lookup("b"));
    }

    @Test
    void example3PrintsTwo() throws MyException {
        PrgState state = run(Examples.EX3);
        assertTrue(state.getExeStack().isEmpty());
        assertEquals(1, state.getOut().size());
        assertEquals(new IntValue(2), state.getOut().get(0));
        assertEquals(new BoolValue(true), state.getSymTable().lookup("a"));
        assertEquals(new IntValue(2), state.getSymTable().lookup("v"));
    }

    // ----- the examples of A3 -----

    @Test
    void fileExampleReadsBothNumbersAndClosesTheFile() throws Exception {
        Path in = inputFile("15\n50\n");
        PrgState s = run(Examples.fileExample(in.toString()));
        assertEquals(2, s.getOut().size());
        assertEquals(new IntValue(15), s.getOut().get(0));
        assertEquals(new IntValue(50), s.getOut().get(1));
        assertTrue(s.getFileTable().toMap().isEmpty());
        assertTrue(s.getExeStack().isEmpty());
    }

    @Test
    void fileExampleOnAnEmptyFilePrintsTwoZeros() throws Exception {
        Path in = inputFile("");
        PrgState s = run(Examples.fileExample(in.toString()));
        assertEquals(new IntValue(0), s.getOut().get(0));
        assertEquals(new IntValue(0), s.getOut().get(1));
        assertEquals(2, s.getOut().size());
        assertTrue(s.getFileTable().toMap().isEmpty());
    }

    @Test
    void fileExampleOnAFileWithOneNumberPrintsItThenZero() throws Exception {
        Path in = inputFile("15\n");
        PrgState s = run(Examples.fileExample(in.toString()));
        assertEquals("{15, 0}", s.getOut().toString());
    }

    @Test
    void fileExampleOnAMissingFileThrows() {
        IStmt program = Examples.fileExample(dir.resolve("missing.in").toString());
        var ex = assertThrows(StatementException.class, () -> run(program));
        assertTrue(ex.getMessage().startsWith("openRFile: cannot open"));
    }

    @Test
    void fileExampleOnAFileWithATextLineThrows() throws Exception {
        Path in = inputFile("15\nabc\n");
        assertThrows(StatementException.class, () -> run(Examples.fileExample(in.toString())));
    }

    @Test
    void example5PrintsFive() throws MyException {
        PrgState state = run(Examples.EX5);
        assertTrue(state.getExeStack().isEmpty());
        assertEquals(1, state.getOut().size());
        assertEquals(new IntValue(5), state.getOut().get(0));
        assertEquals(new IntValue(5), state.getSymTable().lookup("a"));
        assertEquals(new IntValue(7), state.getSymTable().lookup("b"));
    }

    @Test
    void aFileThatIsNeverClosedStaysInTheFileTable() throws Exception {
        Path in = inputFile("1\n");
        PrgState s = run(new CompStmt(new OpenRFileStmt(str(in.toString())), new NopStmt()));
        assertTrue(s.getFileTable().isDefined(new StringValue(in.toString())));
    }

    @Test
    void readingAfterCloseThrows() throws Exception {
        Path in = inputFile("1\n");
        IStmt file = new OpenRFileStmt(str(in.toString()));
        IStmt program = new CompStmt(new VarDeclStmt("x", new IntType()),
                new CompStmt(file,
                        new CompStmt(new CloseRFileStmt(str(in.toString())),
                                new ReadFileStmt(str(in.toString()), "x"))));
        var ex = assertThrows(StatementException.class, () -> run(program));
        assertTrue(ex.getMessage().contains("is not open"));
    }

    // ----- one step / all steps -----

    @Test
    void theSameExampleCanBeRunTwiceOnFreshStates() throws MyException {
        assertEquals(new IntValue(18), run(Examples.EX2).getOut().get(0));
        assertEquals(new IntValue(18), run(Examples.EX2).getOut().get(0));
    }

    @Test
    void outKeepsTheValuesInThePrintOrder() throws MyException {
        IStmt program = new CompStmt(new PrintStmt(num(1)),
                new CompStmt(new PrintStmt(num(2)), new PrintStmt(num(3))));
        assertEquals("{1, 2, 3}", run(program).getOut().toString());
    }

    @Test
    void oneStepOnAnEmptyStackThrows() {
        PrgState state = emptyState();
        Controller controller = new Controller(repoFor(state), false);
        var ex = assertThrows(StatementException.class, () -> controller.oneStep(state));
        assertEquals("execution stack is empty", ex.getMessage());
    }

    @Test
    void aFurtherStepAfterTheEndOfTheExecutionThrows() throws MyException {
        PrgState state = newState(new NopStmt());
        Controller controller = new Controller(repoFor(state), false);
        controller.allStep();
        assertThrows(StatementException.class, () -> controller.oneStep(state));
    }

    @Test
    void oneStepExecutesExactlyOneStatement() throws MyException {
        PrgState state = newState(Examples.EX1);
        Controller controller = new Controller(repoFor(state), false);
        controller.oneStep(state); // S1: (int v; (v = 2; print(v)))
        assertEquals("{int v | (v = 2; print(v))}", state.getExeStack().toString());
        controller.oneStep(state); // S3
        assertEquals("{(v = 2; print(v))}", state.getExeStack().toString());
        assertEquals("{v -> 0}", state.getSymTable().toString());
    }

    @Test
    void oneStepReturnsTheState() throws MyException {
        PrgState state = newState(new NopStmt());
        assertSame(state, new Controller(repoFor(state), false).oneStep(state));
    }

    @Test
    void anErrorInTheProgramStopsTheExecution() {
        PrgState state = newState(new CompStmt(new AssignStmt("x", num(1)), new PrintStmt(num(9))));
        Controller controller = new Controller(repoFor(state), false);
        assertThrows(StatementException.class, controller::allStep);
        assertEquals(0, state.getOut().size());
    }

    @Test
    void anExpressionErrorInTheProgramIsReported() {
        PrgState state = newState(new PrintStmt(new VarExp("undefined")));
        Controller controller = new Controller(repoFor(state), false);
        assertThrows(ExpressionException.class, controller::allStep);
    }

    // ----- display -----

    private String display(IStmt program) throws MyException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrgState state = newState(program);
        new Controller(repoFor(state), true, new PrintStream(bytes, true, StandardCharsets.UTF_8)).allStep();
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void displayFlagOffPrintsNothing() throws MyException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrgState state = newState(Examples.EX1);
        new Controller(repoFor(state), false, new PrintStream(bytes)).allStep();
        assertEquals("", bytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void example1DisplaysTheStateAfterEveryStep() throws MyException {
        List<String> expected = List.of(
                "ExeStack: {(int v; (v = 2; print(v)))}", "SymTable: {}", "Out: {}", "FileTable: {}", "",
                "ExeStack: {int v | (v = 2; print(v))}", "SymTable: {}", "Out: {}", "FileTable: {}", "",
                "ExeStack: {(v = 2; print(v))}", "SymTable: {v -> 0}", "Out: {}", "FileTable: {}", "",
                "ExeStack: {v = 2 | print(v)}", "SymTable: {v -> 0}", "Out: {}", "FileTable: {}", "",
                "ExeStack: {print(v)}", "SymTable: {v -> 2}", "Out: {}", "FileTable: {}", "",
                "ExeStack: {}", "SymTable: {v -> 2}", "Out: {2}", "FileTable: {}", "");
        assertEquals(expected, display(Examples.EX1).lines().toList());
    }

    @Test
    void example3DisplaysTheInitialStateAndOneStatePerStep() throws MyException {
        String output = display(Examples.EX3);
        long states = output.lines().filter(l -> l.startsWith("ExeStack:")).count();
        assertEquals(11, states); // 10 steps + the initial state
        assertTrue(output.contains("Out: {2}"));
    }

    @Test
    void displayShowsTheOpenFilesOfTheFileExample() throws Exception {
        Path in = inputFile("15\n50\n");
        String output = display(Examples.fileExample(in.toString()));
        assertTrue(output.contains("FileTable: {\"" + in + "\" -> reader}"));
        assertTrue(output.lines().anyMatch(l -> l.equals("Out: {15, 50}")));
    }

    // ----- log file -----

    @Test
    void logFileContainsTheFourSectionsInOrder() throws Exception {
        run(Examples.EX1);
        String log = Files.readString(logFile());
        int a = log.indexOf("ExeStack:"), b = log.indexOf("SymTable:"),
                c = log.indexOf("Out:"), d = log.indexOf("FileTable:");
        assertTrue(0 <= a && a < b && b < c && c < d);
    }

    @Test
    void everyStateInTheLogHasTheFourSectionsInOrder() throws Exception {
        run(Examples.EX2);
        String[] states = Files.readString(logFile()).split("\n\n");
        for (String state : states) {
            List<String> headers = state.lines().filter(l -> l.endsWith(":") && !l.contains(" ")).toList();
            assertEquals(List.of("ExeStack:", "SymTable:", "Out:", "FileTable:"), headers);
        }
    }

    @Test
    void logFileHasTheInitialStateAndOneStatePerStep() throws Exception {
        run(Examples.EX1); // 5 steps
        String log = Files.readString(logFile());
        assertEquals(6, log.split("ExeStack:", -1).length - 1);
    }

    @Test
    void logFileOfExample1HasTheExpectedContent() throws Exception {
        run(Examples.EX1);
        List<String> expected = List.of(
                "ExeStack:", "(int v; (v = 2; print(v)))", "SymTable:", "Out:", "FileTable:", "",
                "ExeStack:", "int v", "(v = 2; print(v))", "SymTable:", "Out:", "FileTable:", "",
                "ExeStack:", "(v = 2; print(v))", "SymTable:", "v --> 0", "Out:", "FileTable:", "",
                "ExeStack:", "v = 2", "print(v)", "SymTable:", "v --> 0", "Out:", "FileTable:", "",
                "ExeStack:", "print(v)", "SymTable:", "v --> 2", "Out:", "FileTable:", "",
                "ExeStack:", "SymTable:", "v --> 2", "Out:", "2", "FileTable:", "");
        assertEquals(expected, Files.readAllLines(logFile()));
    }

    @Test
    void logFileOfTheFileExampleShowsTheOpenFile() throws Exception {
        Path in = inputFile("15\n50\n");
        run(Examples.fileExample(in.toString()));
        String log = Files.readString(logFile());
        assertTrue(log.contains("varf --> \"" + in + "\"\n"));
        assertTrue(log.contains("FileTable:\n" + in + "\n")); // the file is listed while it is open
        assertTrue(log.contains("Out:\n15\n50\n"));
        assertTrue(log.stripTrailing().endsWith("FileTable:")); // the last state: the file is closed
    }

    @Test
    void logFileKeepsTheStatesBeforeAnError() throws Exception {
        PrgState state = newState(new AssignStmt("x", num(1))); // x is not declared
        assertThrows(StatementException.class, () -> new Controller(repoFor(state), false).allStep());
        String log = Files.readString(logFile());
        assertEquals(1, log.split("ExeStack:", -1).length - 1); // only the initial state was logged
        assertTrue(log.contains("x = 1"));
    }

    @Test
    void logFileIsWrittenEvenWhenTheDisplayIsOff() throws Exception {
        run(Examples.EX1); // display off, log still written
        assertTrue(Files.size(logFile()) > 0);
    }

    @Test
    void runningTwiceOnTheSameLogFileAppends() throws Exception {
        run(Examples.EX1);
        run(Examples.EX1);
        assertEquals(12, Files.readString(logFile()).split("ExeStack:", -1).length - 1);
    }

    @Test
    void anUnwritableLogFileStopsTheExecutionWithARepositoryException() {
        PrgState state = newState(Examples.EX1);
        var repo = new Repository(state, dir.resolve("no-such-folder").resolve("log.txt").toString());
        assertThrows(interpreter.exceptions.RepositoryException.class,
                () -> new Controller(repo, false).allStep());
    }
}
