package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.model.PrgState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.emptyState;
import static interpreter.TestSupport.num;
import static org.junit.jupiter.api.Assertions.*;

class CompStmtTest {
    private PrgState state;

    @BeforeEach
    void setUp() {
        state = emptyState();
    }

    @Test
    void pushesTheSecondStatementThenTheFirstSoTheFirstIsOnTop() throws MyException {
        IStmt first = new NopStmt();
        IStmt second = new PrintStmt(num(1));
        new CompStmt(first, second).execute(state);
        assertSame(first, state.getExeStack().pop());
        assertSame(second, state.getExeStack().pop());
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void doesNotChangeSymTableOrOut() throws MyException {
        new CompStmt(new NopStmt(), new NopStmt()).execute(state);
        assertEquals("{}", state.getSymTable().toString());
        assertEquals(0, state.getOut().size());
    }

    @Test
    void toStringHasNoOuterParenthesesButNestedCompoundsHaveThem() {
        IStmt s = new CompStmt(new NopStmt(), new CompStmt(new NopStmt(), new NopStmt()));
        assertEquals("nop; (nop; nop)", s.toString());
    }
}
