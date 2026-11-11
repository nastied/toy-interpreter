package interpreter.model.type;

import interpreter.model.value.StringValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringTypeTest {
    @Test
    void defaultValueIsTheEmptyString() {
        assertEquals(new StringValue(""), new StringType().defaultValue());
    }

    @Test
    void stringTypesAreEqual() {
        assertEquals(new StringType(), new StringType());
        assertEquals(new StringType().hashCode(), new StringType().hashCode());
    }

    @Test
    void stringTypeDiffersFromTheOtherTypes() {
        assertNotEquals(new StringType(), new IntType());
        assertNotEquals(new StringType(), new BoolType());
        assertNotEquals(new IntType(), new StringType());
    }

    @Test
    void toStringIsTheLanguageKeyword() {
        assertEquals("string", new StringType().toString());
    }
}
