package interpreter.model.stmt;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.ArithExp;
import interpreter.model.exp.ArithOp;
import interpreter.model.exp.VarExp;
import interpreter.model.type.BoolType;
import interpreter.model.type.IntType;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.bool;
import static interpreter.TestSupport.emptyState;
import static interpreter.TestSupport.num;
import static org.junit.jupiter.api.Assertions.*;

class AssignStmtTest {
    private PrgState state;

    @BeforeEach
    void setUp() throws MyException {
        state = emptyState();
        new VarDeclStmt("v", new IntType()).execute(state);
        new VarDeclStmt("b", new BoolType()).execute(state);
    }

    @Test
    void assignsTheValueOfTheExpression() throws MyException {
        new AssignStmt("v", num(2)).execute(state);
        assertEquals(new IntValue(2), state.getSymTable().lookup("v"));
    }

    @Test
    void assignsABooleanToABooleanVariable() throws MyException {
        new AssignStmt("b", bool(true)).execute(state);
        assertEquals(new BoolValue(true), state.getSymTable().lookup("b"));
    }

    @Test
    void expressionIsEvaluatedAgainstTheCurrentSymTable() throws MyException {
        new AssignStmt("v", num(4)).execute(state);
        new AssignStmt("v", new ArithExp(ArithOp.ADD, new VarExp("v"), num(1))).execute(state);
        assertEquals(new IntValue(5), state.getSymTable().lookup("v"));
    }

    @Test
    void assignmentToAnUndeclaredVariableThrows() {
        var ex = assertThrows(StatementException.class, () -> new AssignStmt("x", num(1)).execute(state));
        assertEquals("variable x was not declared", ex.getMessage());
    }

    @Test
    void assigningABooleanToAnIntVariableThrows() {
        assertThrows(StatementException.class, () -> new AssignStmt("v", bool(true)).execute(state));
    }

    @Test
    void assigningAnIntegerToABooleanVariableThrows() {
        assertThrows(StatementException.class, () -> new AssignStmt("b", num(1)).execute(state));
    }

    @Test
    void typeMismatchLeavesTheVariableUnchanged() throws MyException {
        new AssignStmt("v", num(3)).execute(state);
        assertThrows(StatementException.class, () -> new AssignStmt("v", bool(true)).execute(state));
        assertEquals(new IntValue(3), state.getSymTable().lookup("v"));
    }

    @Test
    void errorOfTheExpressionIsPropagated() {
        assertThrows(ExpressionException.class, () -> new AssignStmt("v", new VarExp("undefined")).execute(state));
    }

    @Test
    void toStringShowsTheAssignment() {
        assertEquals("v = 2", new AssignStmt("v", num(2)).toString());
    }
}
