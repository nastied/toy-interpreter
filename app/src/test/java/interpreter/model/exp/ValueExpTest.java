package interpreter.model.exp;

import interpreter.exceptions.MyException;
import interpreter.model.adt.IDictionary;
import interpreter.model.adt.MyDictionary;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IValue;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueExpTest {
    private final IDictionary<String, IValue> symTable = new MyDictionary<>();

    @Test
    void evaluatesToItsIntegerValue() throws MyException {
        assertEquals(new IntValue(42), new ValueExp(new IntValue(42)).eval(symTable));
    }

    @Test
    void evaluatesToItsBooleanValue() throws MyException {
        assertEquals(new BoolValue(true), new ValueExp(new BoolValue(true)).eval(symTable));
    }

    @Test
    void toStringShowsTheValue() {
        assertEquals("42", new ValueExp(new IntValue(42)).toString());
    }
}
