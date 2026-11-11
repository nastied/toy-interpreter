package interpreter.model.value;

import interpreter.model.type.StringType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StringValueTest {
    @Test
    void hasTypeString() {
        assertEquals(new StringType(), new StringValue("a").getType());
    }

    @Test
    void equalStringsAreEqualValuesWithTheSameHashCode() {
        StringValue a = new StringValue("test.in");
        StringValue b = new StringValue(new String("test.in")); // a different String object, same content
        assertNotSame(a.val(), b.val());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentStringsAreNotEqual() {
        assertNotEquals(new StringValue("a"), new StringValue("b"));
        assertNotEquals(new StringValue("a"), new StringValue("A"));
    }

    @Test
    void aStringValueIsNeverEqualToAValueOfAnotherType() {
        assertNotEquals(new StringValue("1"), new IntValue(1));
        assertNotEquals(new StringValue("true"), new BoolValue(true));
    }

    @Test
    void toStringShowsTheTextInQuotes() {
        assertEquals("\"test.in\"", new StringValue("test.in").toString());
        assertEquals("\"\"", new StringValue("").toString());
    }

    @Test
    void canBeUsedAsAKeyOfAHashMapByContent() {
        Map<StringValue, String> map = new HashMap<>();
        map.put(new StringValue("test.in"), "reader");
        assertEquals("reader", map.get(new StringValue("test.in")));
        assertNull(map.get(new StringValue("other.in")));
    }
}
