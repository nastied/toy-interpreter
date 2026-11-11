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

    @Test
    void removeDeletesTheBinding() throws ADTException {
        dict.put("a", 1);
        dict.remove("a");
        assertFalse(dict.isDefined("a"));
        assertThrows(ADTException.class, () -> dict.lookup("a"));
    }

    @Test
    void removeKeepsTheOtherBindings() throws ADTException {
        dict.put("a", 1);
        dict.put("b", 2);
        dict.remove("a");
        assertEquals(2, dict.lookup("b"));
    }

    @Test
    void removeOfMissingKeyThrows() {
        assertThrows(ADTException.class, () -> dict.remove("missing"));
    }

    @Test
    void aRemovedKeyCanBePutAgain() throws ADTException {
        dict.put("a", 1);
        dict.remove("a");
        dict.put("a", 5);
        assertEquals(5, dict.lookup("a"));
    }

    @Test
    void toMapContainsAllTheBindingsInInsertionOrder() {
        dict.put("z", 1);
        dict.put("a", 2);
        dict.put("m", 3);
        assertEquals(java.util.List.of("z", "a", "m"), new java.util.ArrayList<>(dict.toMap().keySet()));
        assertEquals(2, dict.toMap().get("a"));
    }

    @Test
    void toMapOfAnEmptyDictionaryIsEmpty() {
        assertTrue(dict.toMap().isEmpty());
    }

    @Test
    void toMapIsAReadOnlyCopy() {
        dict.put("a", 1);
        var copy = dict.toMap();
        assertThrows(UnsupportedOperationException.class, () -> copy.put("b", 2));
        dict.put("b", 2);
        assertEquals(1, copy.size()); // later changes of the dictionary do not show in the copy
    }
}
