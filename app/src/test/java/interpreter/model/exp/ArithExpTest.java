package interpreter.model.exp;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.model.adt.IDictionary;
import interpreter.model.adt.MyDictionary;
import interpreter.model.value.IValue;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.bool;
import static interpreter.TestSupport.num;
import static org.junit.jupiter.api.Assertions.*;

class ArithExpTest {
    private final IDictionary<String, IValue> symTable = new MyDictionary<>();

    @Test
    void addition() throws MyException {
        assertEquals(new IntValue(10), new ArithExp(ArithOp.ADD, num(7), num(3)).eval(symTable));
    }

    @Test
    void subtraction() throws MyException {
        assertEquals(new IntValue(4), new ArithExp(ArithOp.SUB, num(7), num(3)).eval(symTable));
    }

    @Test
    void subtractionCanBeNegative() throws MyException {
        assertEquals(new IntValue(-4), new ArithExp(ArithOp.SUB, num(3), num(7)).eval(symTable));
    }

    @Test
    void multiplication() throws MyException {
        assertEquals(new IntValue(21), new ArithExp(ArithOp.MUL, num(7), num(3)).eval(symTable));
    }

    @Test
    void divisionIsIntegerDivision() throws MyException {
        assertEquals(new IntValue(2), new ArithExp(ArithOp.DIV, num(7), num(3)).eval(symTable));
    }

    @Test
    void exactDivision() throws MyException {
        assertEquals(new IntValue(5), new ArithExp(ArithOp.DIV, num(10), num(2)).eval(symTable));
    }

    @Test
    void respectsOperatorPrecedenceEncodedInTheTree() throws MyException {
        IExp e = new ArithExp(ArithOp.ADD, num(2), new ArithExp(ArithOp.MUL, num(3), num(5)));
        assertEquals(new IntValue(17), e.eval(symTable));
    }

    @Test
    void divisionByZeroThrows() {
        IExp e = new ArithExp(ArithOp.DIV, num(1), num(0));
        var ex = assertThrows(ExpressionException.class, () -> e.eval(symTable));
        assertEquals("division by zero", ex.getMessage());
    }

    @Test
    void zeroDividedByANumberIsFine() throws MyException {
        assertEquals(new IntValue(0), new ArithExp(ArithOp.DIV, num(0), num(5)).eval(symTable));
    }

    @Test
    void booleanFirstOperandThrows() {
        IExp e = new ArithExp(ArithOp.ADD, bool(true), num(1));
        var ex = assertThrows(ExpressionException.class, () -> e.eval(symTable));
        assertEquals("first operand is not an integer", ex.getMessage());
    }

    @Test
    void booleanSecondOperandThrows() {
        IExp e = new ArithExp(ArithOp.MUL, num(1), bool(false));
        var ex = assertThrows(ExpressionException.class, () -> e.eval(symTable));
        assertEquals("second operand is not an integer", ex.getMessage());
    }

    @Test
    void errorInANestedOperandIsPropagated() {
        IExp e = new ArithExp(ArithOp.ADD, num(1), new ArithExp(ArithOp.DIV, num(1), num(0)));
        assertThrows(ExpressionException.class, () -> e.eval(symTable));
    }

    @Test
    void undefinedVariableOperandThrows() {
        IExp e = new ArithExp(ArithOp.ADD, new VarExp("x"), num(1));
        assertThrows(ExpressionException.class, () -> e.eval(symTable));
    }

    @Test
    void variableOperandsAreLookedUp() throws MyException {
        symTable.put("a", new IntValue(5));
        IExp e = new ArithExp(ArithOp.ADD, new VarExp("a"), num(1));
        assertEquals(new IntValue(6), e.eval(symTable));
    }

    @Test
    void toStringIsFullyParenthesised() {
        IExp e = new ArithExp(ArithOp.ADD, num(2), new ArithExp(ArithOp.MUL, num(3), num(5)));
        assertEquals("(2 + (3 * 5))", e.toString());
    }
}
