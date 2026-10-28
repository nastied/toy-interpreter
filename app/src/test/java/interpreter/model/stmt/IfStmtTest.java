package interpreter.model.stmt;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.VarExp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.bool;
import static interpreter.TestSupport.emptyState;
import static interpreter.TestSupport.num;
import static org.junit.jupiter.api.Assertions.*;

class IfStmtTest {
    private final IStmt thenS = new PrintStmt(num(1));
    private final IStmt elseS = new PrintStmt(num(2));
    private PrgState state;

    @BeforeEach
    void setUp() {
        state = emptyState();
    }

    @Test
    void trueConditionPushesTheThenBranch() throws MyException {
        new IfStmt(bool(true), thenS, elseS).execute(state);
        assertSame(thenS, state.getExeStack().pop());
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void falseConditionPushesTheElseBranch() throws MyException {
        new IfStmt(bool(false), thenS, elseS).execute(state);
        assertSame(elseS, state.getExeStack().pop());
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void integerConditionThrows() {
        var ex = assertThrows(StatementException.class, () -> new IfStmt(num(1), thenS, elseS).execute(state));
        assertEquals("condition of the if statement is not a boolean", ex.getMessage());
    }

    @Test
    void failedConditionPushesNothing() {
        assertThrows(StatementException.class, () -> new IfStmt(num(1), thenS, elseS).execute(state));
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void errorOfTheConditionIsPropagated() {
        assertThrows(ExpressionException.class,
                () -> new IfStmt(new VarExp("undefined"), thenS, elseS).execute(state));
    }

    @Test
    void toStringShowsBothBranches() {
        assertEquals("(if true then print(1) else print(2))", new IfStmt(bool(true), thenS, elseS).toString());
    }

    @Test
    void compoundBranchesAreParenthesisedInToString() {
        IStmt s = new IfStmt(bool(true), new CompStmt(thenS, thenS), elseS);
        assertEquals("(if true then (print(1); print(1)) else print(2))", s.toString());
    }
}
