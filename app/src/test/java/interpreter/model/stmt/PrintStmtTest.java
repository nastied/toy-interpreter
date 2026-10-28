package interpreter.model.stmt;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.model.PrgState;
import interpreter.model.exp.VarExp;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.bool;
import static interpreter.TestSupport.emptyState;
import static interpreter.TestSupport.num;
import static org.junit.jupiter.api.Assertions.*;

class PrintStmtTest {
    private PrgState state;

    @BeforeEach
    void setUp() {
        state = emptyState();
    }

    @Test
    void appendsTheValueToOut() throws MyException {
        new PrintStmt(num(7)).execute(state);
        assertEquals(1, state.getOut().size());
        assertEquals(new IntValue(7), state.getOut().get(0));
    }

    @Test
    void valuesAreAppendedInTheOrderOfExecution() throws MyException {
        new PrintStmt(num(1)).execute(state);
        new PrintStmt(bool(false)).execute(state);
        assertEquals(new IntValue(1), state.getOut().get(0));
        assertEquals(new BoolValue(false), state.getOut().get(1));
    }

    @Test
    void printsTheValueOfAVariable() throws MyException {
        state.getSymTable().put("v", new IntValue(9));
        new PrintStmt(new VarExp("v")).execute(state);
        assertEquals(new IntValue(9), state.getOut().get(0));
    }

    @Test
    void errorOfTheExpressionIsPropagatedAndNothingIsPrinted() {
        assertThrows(ExpressionException.class, () -> new PrintStmt(new VarExp("x")).execute(state));
        assertEquals(0, state.getOut().size());
    }

    @Test
    void toStringShowsThePrintCall() {
        assertEquals("print(v)", new PrintStmt(new VarExp("v")).toString());
    }
}
