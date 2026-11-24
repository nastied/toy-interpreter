package interpreter.model.stmt;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.ArithExp;
import interpreter.model.exp.ArithOp;
import interpreter.model.exp.RelOp;
import interpreter.model.exp.RelationalExp;
import interpreter.model.exp.VarExp;
import interpreter.model.type.IntType;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.bool;
import static interpreter.TestSupport.emptyState;
import static interpreter.TestSupport.num;
import static org.junit.jupiter.api.Assertions.*;

class WhileStmtTest {
    private PrgState state;

    @BeforeEach
    void setUp() {
        state = emptyState();
    }

    /**
     * Executes the statements of the ExeStack one by one until it is empty (no controller, no log).
     * A step limit makes a loop that never ends fail the test instead of hanging the build.
     */
    private void runToEnd() throws MyException {
        int steps = 0;
        while (!state.getExeStack().isEmpty()) {
            if (++steps > 10_000) fail("the program does not terminate");
            state.getExeStack().pop().execute(state);
        }
    }

    /** i = 0; while (i < n) (print(i); i = i + 1) */
    private IStmt countingLoop(int n) {
        return new CompStmt(new VarDeclStmt("i", new IntType()),
                new WhileStmt(new RelationalExp(RelOp.LT, new VarExp("i"), num(n)),
                        new CompStmt(new PrintStmt(new VarExp("i")),
                                new AssignStmt("i", new ArithExp(ArithOp.ADD, new VarExp("i"), num(1))))));
    }

    private void runCountingLoop(int n) throws MyException {
        state.getExeStack().push(countingLoop(n));
        runToEnd();
    }

    @Test
    void falseConditionPushesNothing() throws MyException {
        new WhileStmt(bool(false), new PrintStmt(num(1))).execute(state);
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void trueConditionPushesTheLoopAndThenTheBodySoTheBodyRunsFirst() throws MyException {
        IStmt body = new PrintStmt(num(1));
        var loop = new WhileStmt(bool(true), body);
        loop.execute(state);
        assertSame(body, state.getExeStack().pop());
        assertSame(loop, state.getExeStack().pop());
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void theConditionIsEvaluatedOnlyOncePerExecution() throws MyException {
        new WhileStmt(bool(true), new NopStmt()).execute(state);
        assertEquals(2, state.getExeStack().toList().size());
    }

    @Test
    void aLoopThatRunsZeroTimes() throws MyException {
        runCountingLoop(0);
        assertEquals(0, state.getOut().size());
        assertEquals(new IntValue(0), state.getSymTable().lookup("i"));
    }

    @Test
    void aLoopThatRunsOnce() throws MyException {
        runCountingLoop(1);
        assertEquals("{0}", state.getOut().toString());
        assertEquals(new IntValue(1), state.getSymTable().lookup("i"));
    }

    @Test
    void aLoopThatRunsSeveralTimes() throws MyException {
        runCountingLoop(4);
        assertEquals("{0, 1, 2, 3}", state.getOut().toString());
        assertEquals(new IntValue(4), state.getSymTable().lookup("i"));
    }

    @Test
    void theStatementAfterTheLoopRunsAfterTheLastIteration() throws MyException {
        state.getExeStack().push(new CompStmt(countingLoop(2), new PrintStmt(num(99))));
        runToEnd();
        assertEquals("{0, 1, 99}", state.getOut().toString());
    }

    @Test
    void nestedLoops() throws MyException {
        // i = 0; while (i < 2) (j = 0; while (j < 2) (print(j); j = j + 1); i = i + 1) -> prints 0 1 0 1
        IStmt inner = new WhileStmt(new RelationalExp(RelOp.LT, new VarExp("j"), num(2)),
                new CompStmt(new PrintStmt(new VarExp("j")),
                        new AssignStmt("j", new ArithExp(ArithOp.ADD, new VarExp("j"), num(1)))));
        IStmt outer = new WhileStmt(new RelationalExp(RelOp.LT, new VarExp("i"), num(2)),
                new CompStmt(new AssignStmt("j", num(0)),
                        new CompStmt(inner,
                                new AssignStmt("i", new ArithExp(ArithOp.ADD, new VarExp("i"), num(1))))));
        state.getExeStack().push(new CompStmt(new VarDeclStmt("i", new IntType()),
                new CompStmt(new VarDeclStmt("j", new IntType()), outer)));
        runToEnd();
        assertEquals("{0, 1, 0, 1}", state.getOut().toString());
    }

    @Test
    void nonBooleanConditionThrows() {
        var loop = new WhileStmt(num(1), new NopStmt());
        var ex = assertThrows(StatementException.class, () -> loop.execute(state));
        assertEquals("condition 1 is not a boolean", ex.getMessage());
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void errorOfTheConditionIsPropagated() {
        var loop = new WhileStmt(new VarExp("undefined"), new NopStmt());
        assertThrows(ExpressionException.class, () -> loop.execute(state));
    }

    @Test
    void errorInTheBodyStopsTheLoop() {
        state.getExeStack().push(new WhileStmt(bool(true), new AssignStmt("x", num(1)))); // x is not declared
        assertThrows(StatementException.class, this::runToEnd);
    }

    @Test
    void toStringShowsTheConditionAndTheBody() {
        var loop = new WhileStmt(new RelationalExp(RelOp.GT, new VarExp("v"), num(0)),
                new CompStmt(new PrintStmt(new VarExp("v")),
                        new AssignStmt("v", new ArithExp(ArithOp.SUB, new VarExp("v"), num(1)))));
        assertEquals("(while ((v > 0)) (print(v); v = (v - 1)))", loop.toString());
    }

    @Test
    void aLoopWhoseConditionStaysTrueIsStoppedByTheStepLimitOfTheTest() {
        state.getExeStack().push(new WhileStmt(bool(true), new NopStmt()));
        var error = assertThrows(AssertionError.class, this::runToEnd);
        assertEquals("the program does not terminate", error.getMessage());
    }
}
