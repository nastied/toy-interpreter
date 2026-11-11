package interpreter.model.exp;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.model.adt.IDictionary;
import interpreter.model.adt.MyDictionary;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IValue;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.bool;
import static interpreter.TestSupport.num;
import static interpreter.TestSupport.str;
import static org.junit.jupiter.api.Assertions.*;

class RelationalExpTest {
    private final IDictionary<String, IValue> symTable = new MyDictionary<>();

    private IValue eval(RelOp op, int a, int b) throws MyException {
        return new RelationalExp(op, num(a), num(b)).eval(symTable);
    }

    private static final BoolValue TRUE = new BoolValue(true);
    private static final BoolValue FALSE = new BoolValue(false);

    @Test
    void lessThan() throws MyException {
        assertEquals(TRUE, eval(RelOp.LT, 1, 2));
        assertEquals(FALSE, eval(RelOp.LT, 2, 2));
        assertEquals(FALSE, eval(RelOp.LT, 3, 2));
    }

    @Test
    void lessOrEqual() throws MyException {
        assertEquals(TRUE, eval(RelOp.LE, 1, 2));
        assertEquals(TRUE, eval(RelOp.LE, 2, 2));
        assertEquals(FALSE, eval(RelOp.LE, 3, 2));
    }

    @Test
    void equal() throws MyException {
        assertEquals(TRUE, eval(RelOp.EQ, 2, 2));
        assertEquals(FALSE, eval(RelOp.EQ, 2, 3));
    }

    @Test
    void notEqual() throws MyException {
        assertEquals(TRUE, eval(RelOp.NE, 2, 3));
        assertEquals(FALSE, eval(RelOp.NE, 2, 2));
    }

    @Test
    void greaterThan() throws MyException {
        assertEquals(TRUE, eval(RelOp.GT, 3, 2));
        assertEquals(FALSE, eval(RelOp.GT, 2, 2));
        assertEquals(FALSE, eval(RelOp.GT, 1, 2));
    }

    @Test
    void greaterOrEqual() throws MyException {
        assertEquals(TRUE, eval(RelOp.GE, 3, 2));
        assertEquals(TRUE, eval(RelOp.GE, 2, 2));
        assertEquals(FALSE, eval(RelOp.GE, 1, 2));
    }

    @Test
    void worksWithNegativeNumbers() throws MyException {
        assertEquals(TRUE, eval(RelOp.LT, -5, -1));
        assertEquals(TRUE, eval(RelOp.GT, 0, -1));
    }

    @Test
    void resultIsABoolean() throws MyException {
        assertInstanceOf(BoolValue.class, eval(RelOp.LT, 1, 2));
    }

    @Test
    void booleanFirstOperandThrows() {
        IExp e = new RelationalExp(RelOp.LT, bool(true), num(1));
        var ex = assertThrows(ExpressionException.class, () -> e.eval(symTable));
        assertEquals("first operand is not an integer", ex.getMessage());
    }

    @Test
    void stringSecondOperandThrows() {
        IExp e = new RelationalExp(RelOp.EQ, num(1), str("1"));
        var ex = assertThrows(ExpressionException.class, () -> e.eval(symTable));
        assertEquals("second operand is not an integer", ex.getMessage());
    }

    @Test
    void undefinedVariableOperandThrows() {
        IExp e = new RelationalExp(RelOp.GE, new VarExp("x"), num(1));
        assertThrows(ExpressionException.class, () -> e.eval(symTable));
    }

    @Test
    void variableOperandsAreLookedUp() throws MyException {
        symTable.put("a", new IntValue(5));
        symTable.put("b", new IntValue(7));
        assertEquals(TRUE, new RelationalExp(RelOp.LT, new VarExp("a"), new VarExp("b")).eval(symTable));
    }

    @Test
    void operandsMayBeArithmeticExpressions() throws MyException {
        IExp sum = new ArithExp(ArithOp.ADD, num(2), num(3));
        assertEquals(TRUE, new RelationalExp(RelOp.EQ, sum, num(5)).eval(symTable));
    }

    @Test
    void toStringShowsTheOperatorSymbol() {
        assertEquals("(1 < 2)", new RelationalExp(RelOp.LT, num(1), num(2)).toString());
        assertEquals("(1 <= 2)", new RelationalExp(RelOp.LE, num(1), num(2)).toString());
        assertEquals("(1 == 2)", new RelationalExp(RelOp.EQ, num(1), num(2)).toString());
        assertEquals("(1 != 2)", new RelationalExp(RelOp.NE, num(1), num(2)).toString());
        assertEquals("(1 > 2)", new RelationalExp(RelOp.GT, num(1), num(2)).toString());
        assertEquals("(1 >= 2)", new RelationalExp(RelOp.GE, num(1), num(2)).toString());
    }
}
