package interpreter.model.exp;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.model.adt.IDictionary;
import interpreter.model.adt.MyDictionary;
import interpreter.model.value.IValue;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VarExpTest {
    private IDictionary<String, IValue> symTable;

    @BeforeEach
    void setUp() {
        symTable = new MyDictionary<>();
    }

    @Test
    void evaluatesToTheValueOfTheVariable() throws MyException {
        symTable.put("a", new IntValue(2));
        symTable.put("b", new IntValue(3));
        assertEquals(new IntValue(2), new VarExp("a").eval(symTable));
    }

    @Test
    void undefinedVariableThrows() {
        var ex = assertThrows(ExpressionException.class, () -> new VarExp("x").eval(symTable));
        assertEquals("variable x is not defined", ex.getMessage());
    }

    @Test
    void toStringIsTheVariableName() {
        assertEquals("x", new VarExp("x").toString());
    }
}
