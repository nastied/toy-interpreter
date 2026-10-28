package interpreter.controller;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.adt.MyDictionary;
import interpreter.model.adt.MyList;
import interpreter.model.adt.MyStack;
import interpreter.model.exp.VarExp;
import interpreter.model.stmt.AssignStmt;
import interpreter.model.stmt.CompStmt;
import interpreter.model.stmt.IStmt;
import interpreter.model.stmt.NopStmt;
import interpreter.model.stmt.PrintStmt;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import interpreter.repository.Repository;
import interpreter.view.Examples;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static interpreter.TestSupport.emptyState;
import static interpreter.TestSupport.num;
import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {
    private static PrgState newState(IStmt program) {
        return new PrgState(new MyStack<>(), new MyDictionary<>(), new MyList<>(), program);
    }

    private static PrgState run(IStmt program) throws MyException {
        PrgState state = newState(program);
        new Controller(new Repository(state), false).allStep(); // display off
        return state;
    }

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

    @Test
    void theSameExampleCanBeRunTwiceOnFreshStates() throws MyException {
        assertEquals(new IntValue(18), run(Examples.EX2).getOut().get(0));
        assertEquals(new IntValue(18), run(Examples.EX2).getOut().get(0));
    }

    @Test
    void outKeepsTheValuesInThePrintOrder() throws MyException {
        IStmt program = new CompStmt(new PrintStmt(num(1)), new CompStmt(new PrintStmt(num(2)), new PrintStmt(num(3))));
        PrgState state = run(program);
        assertEquals("{1, 2, 3}", state.getOut().toString());
    }

    @Test
    void oneStepOnAnEmptyStackThrows() {
        PrgState state = emptyState();
        Controller controller = new Controller(new Repository(state), false);
        var ex = assertThrows(StatementException.class, () -> controller.oneStep(state));
        assertEquals("execution stack is empty", ex.getMessage());
    }

    @Test
    void aFurtherStepAfterTheEndOfTheExecutionThrows() throws MyException {
        PrgState state = newState(new NopStmt());
        Controller controller = new Controller(new Repository(state), false);
        controller.allStep();
        assertThrows(StatementException.class, () -> controller.oneStep(state));
    }

    @Test
    void oneStepExecutesExactlyOneStatement() throws MyException {
        PrgState state = newState(Examples.EX1);
        Controller controller = new Controller(new Repository(state), false);
        controller.oneStep(state); // S1: int v ; (v = 2; print(v))
        assertEquals("{int v | v = 2; print(v)}", state.getExeStack().toString());
        controller.oneStep(state); // S3
        assertEquals("{v = 2; print(v)}", state.getExeStack().toString());
        assertEquals("{v -> 0}", state.getSymTable().toString());
    }

    @Test
    void oneStepReturnsTheState() throws MyException {
        PrgState state = newState(new NopStmt());
        assertSame(state, new Controller(new Repository(state), false).oneStep(state));
    }

    @Test
    void anErrorInTheProgramStopsTheExecution() {
        PrgState state = newState(new CompStmt(new AssignStmt("x", num(1)), new PrintStmt(num(9))));
        Controller controller = new Controller(new Repository(state), false);
        assertThrows(StatementException.class, controller::allStep);
        assertEquals(0, state.getOut().size());
    }

    @Test
    void anExpressionErrorInTheProgramIsReported() {
        PrgState state = newState(new PrintStmt(new VarExp("undefined")));
        Controller controller = new Controller(new Repository(state), false);
        assertThrows(ExpressionException.class, controller::allStep);
    }

    @Test
    void displayFlagOffPrintsNothing() throws MyException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrgState state = newState(Examples.EX1);
        new Controller(new Repository(state), false, new PrintStream(bytes)).allStep();
        assertEquals("", bytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void example1DisplaysExactlyTheSequenceOfStatesOfTheLabDocument() throws MyException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrgState state = newState(Examples.EX1);
        new Controller(new Repository(state), true, new PrintStream(bytes, true, StandardCharsets.UTF_8)).allStep();

        List<String> expected = List.of(
                "ExeStack: {int v; (v = 2; print(v))}", "SymTable: {}", "Out: {}", "",
                "ExeStack: {int v | v = 2; print(v)}", "SymTable: {}", "Out: {}", "",
                "ExeStack: {v = 2; print(v)}", "SymTable: {v -> 0}", "Out: {}", "",
                "ExeStack: {v = 2 | print(v)}", "SymTable: {v -> 0}", "Out: {}", "",
                "ExeStack: {print(v)}", "SymTable: {v -> 2}", "Out: {}", "",
                "ExeStack: {}", "SymTable: {v -> 2}", "Out: {2}", "");
        assertEquals(expected, bytes.toString(StandardCharsets.UTF_8).lines().toList());
    }

    @Test
    void example3DisplaysTheInitialStateAndOneStatePerStep() throws MyException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrgState state = newState(Examples.EX3);
        new Controller(new Repository(state), true, new PrintStream(bytes, true, StandardCharsets.UTF_8)).allStep();
        String output = bytes.toString(StandardCharsets.UTF_8);
        // initial state + one state per executed step
        long states = output.lines().filter(l -> l.startsWith("ExeStack:")).count();
        assertEquals(11, states); // 10 steps + the initial state
        assertTrue(output.contains("Out: {2}"));
    }
}
