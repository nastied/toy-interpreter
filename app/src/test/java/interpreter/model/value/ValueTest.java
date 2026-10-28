package interpreter.model.value;

import interpreter.model.type.BoolType;
import interpreter.model.type.IntType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueTest {
    @Test
    void intValueHasTypeInt() {
        assertEquals(new IntType(), new IntValue(5).getType());
    }

    @Test
    void boolValueHasTypeBool() {
        assertEquals(new BoolType(), new BoolValue(true).getType());
    }

    @Test
    void valuesWithTheSameContentAreEqual() {
        assertEquals(new IntValue(3), new IntValue(3));
        assertEquals(new BoolValue(true), new BoolValue(true));
    }

    @Test
    void valuesWithDifferentContentOrTypeAreNotEqual() {
        assertNotEquals(new IntValue(3), new IntValue(4));
        assertNotEquals(new BoolValue(true), new BoolValue(false));
        assertNotEquals(new IntValue(1), new BoolValue(true));
    }

    @Test
    void toStringShowsOnlyTheContent() {
        assertEquals("-7", new IntValue(-7).toString());
        assertEquals("true", new BoolValue(true).toString());
        assertEquals("false", new BoolValue(false).toString());
    }

    @Test
    void valueOfOneTypeIsNotOfTheOtherType() {
        assertNotEquals(new IntValue(0).getType(), new BoolValue(false).getType());
    }
}
