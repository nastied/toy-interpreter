package interpreter.model.type;

import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeTest {
    @Test
    void defaultValueOfIntIsZero() {
        assertEquals(new IntValue(0), new IntType().defaultValue());
    }

    @Test
    void defaultValueOfBoolIsFalse() {
        assertEquals(new BoolValue(false), new BoolType().defaultValue());
    }

    @Test
    void sameTypesAreEqual() {
        assertEquals(new IntType(), new IntType());
        assertEquals(new BoolType(), new BoolType());
    }

    @Test
    void differentTypesAreNotEqual() {
        assertNotEquals(new IntType(), new BoolType());
        assertNotEquals(new BoolType(), new IntType());
    }

    @Test
    void toStringIsTheLanguageKeyword() {
        assertEquals("int", new IntType().toString());
        assertEquals("bool", new BoolType().toString());
    }
}
