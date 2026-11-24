package interpreter.model.stmt;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.VarExp;
import interpreter.model.type.BoolType;
import interpreter.model.type.IntType;
import interpreter.model.type.RefType;
import interpreter.model.type.StringType;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import interpreter.model.value.RefValue;
import interpreter.model.value.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.bool;
import static interpreter.TestSupport.emptyState;
import static interpreter.TestSupport.num;
import static interpreter.TestSupport.str;
import static org.junit.jupiter.api.Assertions.*;

class NewStmtTest {
    private PrgState state;

    @BeforeEach
    void setUp() throws MyException {
        state = emptyState();
        new VarDeclStmt("v", new RefType(new IntType())).execute(state); // Ref int v
    }

    @Test
    void allocatesAtAddressOneAndUpdatesTheVariable() throws MyException {
        new NewStmt("v", num(20)).execute(state);
        assertEquals(new RefValue(1, new IntType()), state.getSymTable().lookup("v"));
        assertEquals(new IntValue(20), state.getHeap().get(1));
    }

    @Test
    void theSecondNewGetsTheNextAddressAndTheOldLocationStaysInTheHeap() throws MyException {
        new NewStmt("v", num(20)).execute(state);
        new NewStmt("v", num(30)).execute(state);
        assertEquals(new RefValue(2, new IntType()), state.getSymTable().lookup("v"));
        assertEquals(new IntValue(20), state.getHeap().get(1)); // not collected: only the controller does that
        assertEquals(new IntValue(30), state.getHeap().get(2));
    }

    @Test
    void aReferenceToAReferenceStoresTheInnerReference() throws MyException {
        new NewStmt("v", num(20)).execute(state);
        new VarDeclStmt("a", new RefType(new RefType(new IntType()))).execute(state);
        new NewStmt("a", new VarExp("v")).execute(state);
        assertEquals(new RefValue(2, new RefType(new IntType())), state.getSymTable().lookup("a"));
        assertEquals(new RefValue(1, new IntType()), state.getHeap().get(2));
    }

    @Test
    void worksForBooleanAndStringLocations() throws MyException {
        new VarDeclStmt("b", new RefType(new BoolType())).execute(state);
        new VarDeclStmt("s", new RefType(new StringType())).execute(state);
        new NewStmt("b", bool(true)).execute(state);
        new NewStmt("s", str("hi")).execute(state);
        assertEquals(new BoolValue(true), state.getHeap().get(1));
        assertEquals(new StringValue("hi"), state.getHeap().get(2));
    }

    @Test
    void theExpressionIsEvaluatedAgainstTheSymTableAndTheHeap() throws MyException {
        new NewStmt("v", num(20)).execute(state);
        new VarDeclStmt("w", new RefType(new IntType())).execute(state);
        new NewStmt("w", new interpreter.model.exp.ReadHeapExp(new VarExp("v"))).execute(state);
        assertEquals(new IntValue(20), state.getHeap().get(2));
    }

    @Test
    void aValueOfTheWrongTypeThrowsAndChangesNothing() {
        var ex = assertThrows(StatementException.class, () -> new NewStmt("v", bool(true)).execute(state));
        assertTrue(ex.getMessage().startsWith("new: true has type bool, expected int"));
        assertTrue(state.getHeap().getContent().isEmpty());
    }

    @Test
    void aReferenceValueWhereAnIntegerIsExpectedThrows() throws MyException {
        new NewStmt("v", num(1)).execute(state);
        assertThrows(StatementException.class, () -> new NewStmt("v", new VarExp("v")).execute(state));
    }

    @Test
    void anIntegerWhereAReferenceIsExpectedThrows() throws MyException {
        new VarDeclStmt("a", new RefType(new RefType(new IntType()))).execute(state);
        assertThrows(StatementException.class, () -> new NewStmt("a", num(1)).execute(state));
    }

    @Test
    void undeclaredVariableThrows() {
        var ex = assertThrows(StatementException.class, () -> new NewStmt("x", num(1)).execute(state));
        assertEquals("new: x is not declared", ex.getMessage());
    }

    @Test
    void variableThatIsNotAReferenceThrows() throws MyException {
        new VarDeclStmt("n", new IntType()).execute(state);
        var ex = assertThrows(StatementException.class, () -> new NewStmt("n", num(1)).execute(state));
        assertEquals("new: n is not a reference", ex.getMessage());
        assertTrue(state.getHeap().getContent().isEmpty());
    }

    @Test
    void errorOfTheExpressionIsPropagated() {
        assertThrows(ExpressionException.class, () -> new NewStmt("v", new VarExp("undefined")).execute(state));
        assertTrue(state.getHeap().getContent().isEmpty());
    }

    @Test
    void toStringShowsTheCall() {
        assertEquals("new(v, 20)", new NewStmt("v", num(20)).toString());
    }
}
