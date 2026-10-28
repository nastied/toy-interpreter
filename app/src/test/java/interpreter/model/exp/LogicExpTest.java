package interpreter.model.exp;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.model.adt.IDictionary;
import interpreter.model.adt.MyDictionary;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IValue;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.bool;
import static interpreter.TestSupport.num;
import static org.junit.jupiter.api.Assertions.*;

class LogicExpTest {
    private final IDictionary<String, IValue> symTable = new MyDictionary<>();

    private IValue eval(LogicOp op, boolean a, boolean b) throws MyException {
        return new LogicExp(op, bool(a), bool(b)).eval(symTable);
    }

    @Test
    void andTruthTable() throws MyException {
        assertEquals(new BoolValue(true), eval(LogicOp.AND, true, true));
        assertEquals(new BoolValue(false), eval(LogicOp.AND, true, false));
        assertEquals(new BoolValue(false), eval(LogicOp.AND, false, true));
        assertEquals(new BoolValue(false), eval(LogicOp.AND, false, false));
    }

    @Test
    void orTruthTable() throws MyException {
        assertEquals(new BoolValue(true), eval(LogicOp.OR, true, true));
        assertEquals(new BoolValue(true), eval(LogicOp.OR, true, false));
        assertEquals(new BoolValue(true), eval(LogicOp.OR, false, true));
        assertEquals(new BoolValue(false), eval(LogicOp.OR, false, false));
    }

    @Test
    void integerFirstOperandThrows() {
        IExp e = new LogicExp(LogicOp.AND, num(1), bool(true));
        var ex = assertThrows(ExpressionException.class, () -> e.eval(symTable));
        assertEquals("first operand is not a boolean", ex.getMessage());
    }

    @Test
    void integerSecondOperandThrows() {
        IExp e = new LogicExp(LogicOp.OR, bool(true), num(1));
        var ex = assertThrows(ExpressionException.class, () -> e.eval(symTable));
        assertEquals("second operand is not a boolean", ex.getMessage());
    }

    @Test
    void bothOperandsAreEvaluatedEvenIfTheLeftOneDecidesTheResult() {
        IExp e = new LogicExp(LogicOp.OR, bool(true), new VarExp("undefined"));
        assertThrows(ExpressionException.class, () -> e.eval(symTable));
    }

    @Test
    void variableOperandsAreLookedUp() throws MyException {
        symTable.put("a", new BoolValue(true));
        IExp e = new LogicExp(LogicOp.AND, new VarExp("a"), bool(true));
        assertEquals(new BoolValue(true), e.eval(symTable));
    }

    @Test
    void toStringShowsTheKeyword() {
        assertEquals("(true and false)", new LogicExp(LogicOp.AND, bool(true), bool(false)).toString());
        assertEquals("(true or false)", new LogicExp(LogicOp.OR, bool(true), bool(false)).toString());
    }
}
