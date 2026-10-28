package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.ValueExp;
import interpreter.model.type.BoolType;
import interpreter.model.type.IntType;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.emptyState;
import static org.junit.jupiter.api.Assertions.*;

class VarDeclStmtTest {
    private PrgState state;

    @BeforeEach
    void setUp() {
        state = emptyState();
    }

    @Test
    void declaringAnIntVariableGivesItTheValueZero() throws MyException {
        new VarDeclStmt("v", new IntType()).execute(state);
        assertEquals(new IntValue(0), state.getSymTable().lookup("v"));
    }

    @Test
    void declaringABoolVariableGivesItTheValueFalse() throws MyException {
        new VarDeclStmt("b", new BoolType()).execute(state);
        assertEquals(new BoolValue(false), state.getSymTable().lookup("b"));
    }

    @Test
    void declaringTheSameVariableTwiceThrows() throws MyException {
        new VarDeclStmt("v", new IntType()).execute(state);
        var ex = assertThrows(StatementException.class,
                () -> new VarDeclStmt("v", new IntType()).execute(state));
        assertEquals("variable v is already declared", ex.getMessage());
    }

    @Test
    void redeclarationDoesNotOverwriteTheCurrentValue() throws MyException {
        new VarDeclStmt("v", new IntType()).execute(state);
        state.getSymTable().update("v", new IntValue(5));
        assertThrows(StatementException.class, () -> new VarDeclStmt("v", new BoolType()).execute(state));
        assertEquals(new IntValue(5), state.getSymTable().lookup("v"));
    }

    @Test
    void redeclaringWithAnotherTypeAlsoThrows() throws MyException {
        new VarDeclStmt("v", new IntType()).execute(state);
        assertThrows(StatementException.class, () -> new VarDeclStmt("v", new BoolType()).execute(state));
    }

    @Test
    void toStringShowsTypeAndName() {
        assertEquals("int v", new VarDeclStmt("v", new IntType()).toString());
        assertEquals("bool b", new VarDeclStmt("b", new BoolType()).toString());
    }
}
