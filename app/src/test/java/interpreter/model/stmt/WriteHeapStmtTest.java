package interpreter.model.stmt;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.ReadHeapExp;
import interpreter.model.exp.VarExp;
import interpreter.model.type.IntType;
import interpreter.model.type.RefType;
import interpreter.model.value.IntValue;
import interpreter.model.value.RefValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.bool;
import static interpreter.TestSupport.emptyState;
import static interpreter.TestSupport.num;
import static org.junit.jupiter.api.Assertions.*;

class WriteHeapStmtTest {
    private PrgState state;

    @BeforeEach
    void setUp() throws MyException {
        state = emptyState();
        new VarDeclStmt("v", new RefType(new IntType())).execute(state);
        new NewStmt("v", num(20)).execute(state); // v -> (1, int), heap {1 -> 20}
    }

    @Test
    void changesTheHeapLocation() throws MyException {
        new WriteHeapStmt("v", num(30)).execute(state);
        assertEquals(new IntValue(30), state.getHeap().get(1));
    }

    @Test
    void theVariableStillPointsToTheSameAddress() throws MyException {
        new WriteHeapStmt("v", num(30)).execute(state);
        assertEquals(new RefValue(1, new IntType()), state.getSymTable().lookup("v"));
    }

    @Test
    void allocatesNothing() throws MyException {
        new WriteHeapStmt("v", num(30)).execute(state);
        assertEquals(1, state.getHeap().getContent().size());
    }

    @Test
    void theNewValueMayDependOnTheOldOne() throws MyException {
        var plusOne = new interpreter.model.exp.ArithExp(interpreter.model.exp.ArithOp.ADD,
                new ReadHeapExp(new VarExp("v")), num(1));
        new WriteHeapStmt("v", plusOne).execute(state);
        assertEquals(new IntValue(21), state.getHeap().get(1));
    }

    @Test
    void undeclaredVariableThrows() {
        var ex = assertThrows(StatementException.class, () -> new WriteHeapStmt("x", num(1)).execute(state));
        assertEquals("wH: x is not declared", ex.getMessage());
    }

    @Test
    void variableThatIsNotAReferenceThrows() throws MyException {
        new VarDeclStmt("n", new IntType()).execute(state);
        var ex = assertThrows(StatementException.class, () -> new WriteHeapStmt("n", num(1)).execute(state));
        assertEquals("wH: n is not a reference", ex.getMessage());
    }

    @Test
    void addressNotInTheHeapThrows() throws MyException {
        state.getSymTable().update("v", new RefValue(9, new IntType()));
        var ex = assertThrows(StatementException.class, () -> new WriteHeapStmt("v", num(1)).execute(state));
        assertEquals("wH: address 9 is not in the heap", ex.getMessage());
    }

    @Test
    void theNullReferenceCannotBeWritten() throws MyException {
        new VarDeclStmt("n", new RefType(new IntType())).execute(state); // (0, int)
        var ex = assertThrows(StatementException.class, () -> new WriteHeapStmt("n", num(1)).execute(state));
        assertEquals("wH: address 0 is not in the heap", ex.getMessage());
    }

    @Test
    void valueOfTheWrongTypeThrowsAndLeavesTheHeapUnchanged() throws MyException {
        var ex = assertThrows(StatementException.class, () -> new WriteHeapStmt("v", bool(true)).execute(state));
        assertTrue(ex.getMessage().startsWith("wH: true has type bool, expected int"));
        assertEquals(new IntValue(20), state.getHeap().get(1));
    }

    @Test
    void errorOfTheExpressionIsPropagated() throws MyException {
        assertThrows(ExpressionException.class, () -> new WriteHeapStmt("v", new VarExp("undefined")).execute(state));
        assertEquals(new IntValue(20), state.getHeap().get(1));
    }

    @Test
    void toStringShowsTheCall() {
        assertEquals("wH(v, 30)", new WriteHeapStmt("v", num(30)).toString());
    }
}
