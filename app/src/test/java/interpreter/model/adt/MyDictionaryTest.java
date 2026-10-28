package interpreter.model.adt;

import interpreter.exceptions.ADTException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyDictionaryTest {
    private IDictionary<String, Integer> dict;

    @BeforeEach
    void setUp() {
        dict = new MyDictionary<>();
    }

    @Test
    void putThenLookupReturnsTheValue() throws ADTException {
        dict.put("a", 1);
        assertEquals(1, dict.lookup("a"));
    }

    @Test
    void isDefinedIsTrueOnlyForPutKeys() {
        dict.put("a", 1);
        assertTrue(dict.isDefined("a"));
        assertFalse(dict.isDefined("b"));
    }

    @Test
    void updateReplacesTheValue() throws ADTException {
        dict.put("a", 1);
        dict.update("a", 5);
        assertEquals(5, dict.lookup("a"));
    }

    @Test
    void putOnExistingKeyReplacesTheValue() throws ADTException {
        dict.put("a", 1);
        dict.put("a", 2);
        assertEquals(2, dict.lookup("a"));
    }

    @Test
    void lookupOfMissingKeyThrows() {
        assertThrows(ADTException.class, () -> dict.lookup("missing"));
    }

    @Test
    void updateOfMissingKeyThrowsAndDoesNotAddTheKey() {
        assertThrows(ADTException.class, () -> dict.update("missing", 1));
        assertFalse(dict.isDefined("missing"));
    }

    @Test
    void toStringUsesTheLabNotation() {
        dict.put("v", 2);
        dict.put("a", 7);
        assertEquals("{v -> 2, a -> 7}", dict.toString());
    }

    @Test
    void toStringOfEmptyDictionary() {
        assertEquals("{}", dict.toString());
    }
}
