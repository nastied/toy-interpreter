package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.model.PrgState;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.emptyState;
import static org.junit.jupiter.api.Assertions.*;

class NopStmtTest {
    @Test
    void changesNothingInTheState() throws MyException {
        PrgState state = emptyState();
        PrgState result = new NopStmt().execute(state);
        assertSame(state, result);
        assertTrue(state.getExeStack().isEmpty());
        assertEquals("{}", state.getSymTable().toString());
        assertEquals(0, state.getOut().size());
    }

    @Test
    void toStringIsNop() {
        assertEquals("nop", new NopStmt().toString());
    }
}
