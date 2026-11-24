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
import interpreter.model.stmt.NewStmt;
import interpreter.model.stmt.OpenRFileStmt;
import interpreter.model.stmt.PrintStmt;
import interpreter.model.stmt.ReadFileStmt;
import interpreter.model.type.IntType;
import interpreter.model.value.RefValue;
import interpreter.model.type.RefType;
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
                "ExeStack: {(int v; (v = 2; print(v)))}", "SymTable: {}", "Out: {}", "FileTable: {}", "Heap: {}", "",
                "ExeStack: {int v | (v = 2; print(v))}", "SymTable: {}", "Out: {}", "FileTable: {}", "Heap: {}", "",
                "ExeStack: {(v = 2; print(v))}", "SymTable: {v -> 0}", "Out: {}", "FileTable: {}", "Heap: {}", "",
                "ExeStack: {v = 2 | print(v)}", "SymTable: {v -> 0}", "Out: {}", "FileTable: {}", "Heap: {}", "",
                "ExeStack: {print(v)}", "SymTable: {v -> 2}", "Out: {}", "FileTable: {}", "Heap: {}", "",
                "ExeStack: {}", "SymTable: {v -> 2}", "Out: {2}", "FileTable: {}", "Heap: {}", "");
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
    void logFileContainsTheFiveSectionsInOrder() throws Exception {
        run(Examples.EX1);
        String log = Files.readString(logFile());
        int a = log.indexOf("ExeStack:"), b = log.indexOf("SymTable:"),
                c = log.indexOf("Out:"), d = log.indexOf("FileTable:"), e = log.indexOf("Heap:");
        assertTrue(0 <= a && a < b && b < c && c < d && d < e);
    }

    @Test
    void everyStateInTheLogHasTheFiveSectionsInOrder() throws Exception {
        run(Examples.EX2);
        String[] states = Files.readString(logFile()).split("\n\n");
        for (String state : states) {
            List<String> headers = state.lines().filter(l -> l.endsWith(":") && !l.contains(" ")).toList();
            assertEquals(List.of("ExeStack:", "SymTable:", "Out:", "FileTable:", "Heap:"), headers);
        }
    }

    @Test
    void logFileHasTheInitialStateAndTwoStatesPerStep() throws Exception {
        run(Examples.EX1); // 5 steps: the initial state, then (before and after the collection) twice per step
        String log = Files.readString(logFile());
        assertEquals(11, log.split("ExeStack:", -1).length - 1);
    }

    private static List<String> logBlock(List<String> stack, List<String> sym, List<String> out) {
        List<String> block = new ArrayList<>();
        block.add("ExeStack:");
        block.addAll(stack);
        block.add("SymTable:");
        block.addAll(sym);
        block.add("Out:");
        block.addAll(out);
        block.addAll(List.of("FileTable:", "Heap:", "")); // a state is followed by an empty line
        return block;
    }

    @Test
    void logFileOfExample1HasTheExpectedContent() throws Exception {
        run(Examples.EX1);
        var s0 = logBlock(List.of("(int v; (v = 2; print(v)))"), List.of(), List.of());
        var s1 = logBlock(List.of("int v", "(v = 2; print(v))"), List.of(), List.of());
        var s2 = logBlock(List.of("(v = 2; print(v))"), List.of("v --> 0"), List.of());
        var s3 = logBlock(List.of("v = 2", "print(v)"), List.of("v --> 0"), List.of());
        var s4 = logBlock(List.of("print(v)"), List.of("v --> 2"), List.of());
        var s5 = logBlock(List.of(), List.of("v --> 2"), List.of("2"));
        List<String> expected = new ArrayList<>(s0);
        // after every step the state is logged twice: before and after the garbage collection
        for (var state : List.of(s1, s2, s3, s4, s5)) {
            expected.addAll(state);
            expected.addAll(state);
        }
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
        assertTrue(log.stripTrailing().endsWith("FileTable:\nHeap:")); // the last state: the file is closed
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
        assertEquals(22, Files.readString(logFile()).split("ExeStack:", -1).length - 1);
    }

    @Test
    void anUnwritableLogFileStopsTheExecutionWithARepositoryException() {
        PrgState state = newState(Examples.EX1);
        var repo = new Repository(state, dir.resolve("no-such-folder").resolve("log.txt").toString());
        assertThrows(interpreter.exceptions.RepositoryException.class,
                () -> new Controller(repo, false).allStep());
    }
    @Test
    void locationsNoVariableRefersToAreRemovedFromTheHeapAfterTheStep() throws Exception {
        // Ref int v; new(v, 1); new(v, 2): the location 1 is garbage as soon as v points to 2
        IStmt program = new CompStmt(new VarDeclStmt("v", new RefType(new IntType())),
                new CompStmt(new NewStmt("v", num(1)), new NewStmt("v", num(2))));
        PrgState state = run(program);
        assertEquals("{2 -> 2}", state.getHeap().toString());
        assertEquals(new RefValue(2, new IntType()), state.getSymTable().lookup("v"));
    }

    @Test
    void theLogShowsTheHeapBeforeAndAfterTheCollection() throws Exception {
        IStmt program = new CompStmt(new VarDeclStmt("v", new RefType(new IntType())),
                new CompStmt(new NewStmt("v", num(1)), new NewStmt("v", num(2))));
        run(program);
        List<String> states = List.of(Files.readString(logFile()).split("\n\n"));
        int before = -1;
        for (int i = 0; i < states.size(); i++)
            if (states.get(i).contains("1 --> 1\n2 --> 2")) { before = i; break; }
        assertTrue(before >= 0, "a state with both locations is logged");
        String after = states.get(before + 1);
        assertFalse(after.contains("1 --> 1"));   // the garbage was removed ...
        assertTrue(after.contains("2 --> 2"));    // ... the live location was not
    }

    @Test
    void theHeapOfAProgramWithoutGarbageIsNotChangedByTheCollector() throws Exception {
        IStmt program = new CompStmt(new VarDeclStmt("v", new RefType(new IntType())), new NewStmt("v", num(5)));
        assertEquals("{1 -> 5}", run(program).getHeap().toString());
    }

    /** Ref int v; new(v, 20); Ref Ref int a; new(a, v); new(v, 30); print(rH(rH(a))) - the program of Ex9. */
    private static IStmt programNeedingTheSafeCollector() {
        var refInt = new RefType(new IntType());
        return new CompStmt(new VarDeclStmt("v", refInt),
                new CompStmt(new NewStmt("v", num(20)),
                        new CompStmt(new VarDeclStmt("a", new RefType(refInt)),
                                new CompStmt(new NewStmt("a", new VarExp("v")),
                                        new CompStmt(new NewStmt("v", num(30)),
                                                new PrintStmt(new interpreter.model.exp.ReadHeapExp(
                                                        new interpreter.model.exp.ReadHeapExp(new VarExp("a")))))))));
    }

    @Test
    void aLocationReachableOnlyThroughTheHeapSurvivesTheCollectionSoThePrintWorks() throws Exception {
        PrgState state = run(programNeedingTheSafeCollector());
        assertEquals("{20}", state.getOut().toString());
        assertEquals("{1 -> 20, 2 -> (1, int), 3 -> 30}", state.getHeap().toString());
    }

    @Test
    void theLogShowsThatTheCollectionKeepsTheLocationReachableThroughTheHeap() throws Exception {
        run(programNeedingTheSafeCollector());
        // the state after new(v, 30) is logged twice (before and after the collection): both have 3 locations
        String log = Files.readString(logFile());
        String marker = "ExeStack:\nprint(rH(rH(a)))\nSymTable:";
        assertEquals(2, log.split(java.util.regex.Pattern.quote(marker), -1).length - 1);
        for (String state : log.split("\n\n"))
            if (state.startsWith(marker)) assertTrue(state.contains("Heap:\n1 --> 20\n2 --> (1, int)\n3 --> 30"));
    }

    // ----- the examples of A4 (section 1.4 of the lab) -----

    @Test
    void example6EndsWithTheExpectedHeapSymTableAndOut() throws MyException {
        PrgState state = run(Examples.EX6);
        assertEquals("{1 -> 20, 2 -> (1, int)}", state.getHeap().toString());
        assertEquals("{v -> (1, int), a -> (2, Ref(int))}", state.getSymTable().toString());
        assertEquals("{(1, int), (2, Ref(int))}", state.getOut().toString());
    }

    @Test
    void example7PrintsTwentyAndTwentyFive() throws MyException {
        PrgState state = run(Examples.EX7);
        assertEquals("{1 -> 20, 2 -> (1, int)}", state.getHeap().toString());
        assertEquals("{v -> (1, int), a -> (2, Ref(int))}", state.getSymTable().toString());
        assertEquals("{20, 25}", state.getOut().toString());
    }

    @Test
    void example8PrintsTwentyAndThirtyFive() throws MyException {
        PrgState state = run(Examples.EX8);
        assertEquals("{1 -> 30}", state.getHeap().toString());
        assertEquals("{v -> (1, int)}", state.getSymTable().toString());
        assertEquals("{20, 35}", state.getOut().toString());
    }

    @Test
    void example9PrintsTwentyWithTheSafeGarbageCollector() throws MyException {
        PrgState state = run(Examples.EX9);
        assertEquals("{20}", state.getOut().toString());
        assertEquals("{v -> (3, int), a -> (2, Ref(int))}", state.getSymTable().toString());
        assertEquals("{1 -> 20, 2 -> (1, int), 3 -> 30}", state.getHeap().toString());
    }

    @Test
    void example10CountsDown() throws MyException {
        PrgState state = run(Examples.EX10);
        assertEquals("{4, 3, 2, 1, 0}", state.getOut().toString());
        assertEquals("{v -> 0}", state.getSymTable().toString());
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void example10LogsEveryIterationOfTheLoop() throws Exception {
        run(Examples.EX10);
        String log = Files.readString(logFile());
        // the loop is on top of the stack once per evaluation of the condition (5 times) and every state
        // is logged twice (before and after the collection)
        long onTop = log.lines().filter(l -> l.startsWith("(while ((v > 0))")).count();
        assertTrue(onTop >= 10);
    }

    @Test
    void aLoopAndTheHeapTogether() throws MyException {
        // Ref int v; new(v, 0); while (rH(v) < 3) wH(v, rH(v) + 1); print(rH(v))
        var refInt = new RefType(new IntType());
        var read = new interpreter.model.exp.ReadHeapExp(new VarExp("v"));
        IStmt program = new CompStmt(new VarDeclStmt("v", refInt),
                new CompStmt(new NewStmt("v", num(0)),
                        new CompStmt(new interpreter.model.stmt.WhileStmt(
                                new interpreter.model.exp.RelationalExp(interpreter.model.exp.RelOp.LT, read, num(3)),
                                new interpreter.model.stmt.WriteHeapStmt("v",
                                        new interpreter.model.exp.ArithExp(interpreter.model.exp.ArithOp.ADD, read, num(1)))),
                                new PrintStmt(read))));
        PrgState state = run(program);
        assertEquals("{3}", state.getOut().toString());
        assertEquals("{1 -> 3}", state.getHeap().toString());
    }
}
