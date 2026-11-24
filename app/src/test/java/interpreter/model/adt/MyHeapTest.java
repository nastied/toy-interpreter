package interpreter.model.adt;

import interpreter.exceptions.ADTException;
import interpreter.model.type.IntType;
import interpreter.model.value.IValue;
import interpreter.model.value.IntValue;
import interpreter.model.value.RefValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MyHeapTest {
    private IHeap heap;

    @BeforeEach
    void setUp() {
        heap = new MyHeap();
    }

    @Test
    void allocateReturnsTheAddressesOneTwoThree() {
        assertEquals(1, heap.allocate(new IntValue(10)));
        assertEquals(2, heap.allocate(new IntValue(20)));
        assertEquals(3, heap.allocate(new IntValue(30)));
    }

    @Test
    void addressZeroIsNeverAllocated() {
        heap.allocate(new IntValue(1));
        assertFalse(heap.isDefined(0));
    }

    @Test
    void getReturnsTheValueStoredAtTheAddress() throws ADTException {
        int a = heap.allocate(new IntValue(10));
        int b = heap.allocate(new IntValue(20));
        assertEquals(new IntValue(10), heap.get(a));
        assertEquals(new IntValue(20), heap.get(b));
    }

    @Test
    void getOfAMissingAddressThrows() {
        var ex = assertThrows(ADTException.class, () -> heap.get(1));
        assertEquals("address 1 is not in the heap", ex.getMessage());
    }

    @Test
    void getOfTheNullAddressThrows() {
        heap.allocate(new IntValue(1));
        assertThrows(ADTException.class, () -> heap.get(0));
    }

    @Test
    void updateReplacesTheValueAndKeepsTheAddress() throws ADTException {
        int a = heap.allocate(new IntValue(10));
        heap.update(a, new IntValue(99));
        assertEquals(new IntValue(99), heap.get(a));
        assertEquals(1, heap.getContent().size());
    }

    @Test
    void updateOfAMissingAddressThrowsAndAllocatesNothing() {
        assertThrows(ADTException.class, () -> heap.update(5, new IntValue(1)));
        assertFalse(heap.isDefined(5));
        assertTrue(heap.getContent().isEmpty());
    }

    @Test
    void isDefinedIsTrueOnlyForAllocatedAddresses() {
        int a = heap.allocate(new IntValue(10));
        assertTrue(heap.isDefined(a));
        assertFalse(heap.isDefined(a + 1));
        assertFalse(heap.isDefined(-1));
    }

    @Test
    void getContentIsASortedReadOnlyCopy() {
        heap.allocate(new IntValue(10));
        heap.allocate(new IntValue(20));
        Map<Integer, IValue> copy = heap.getContent();
        assertEquals(List.of(1, 2), List.copyOf(copy.keySet()));
        assertThrows(UnsupportedOperationException.class, () -> copy.put(3, new IntValue(30)));
        heap.allocate(new IntValue(30));
        assertEquals(2, copy.size()); // later changes do not show in the copy
    }

    @Test
    void setContentReplacesTheWholeContent() throws ADTException {
        heap.allocate(new IntValue(10));
        heap.allocate(new IntValue(20));
        heap.setContent(Map.of(2, new IntValue(20)));
        assertFalse(heap.isDefined(1));
        assertEquals(new IntValue(20), heap.get(2));
    }

    @Test
    void setContentCopiesTheMapItIsGiven() {
        Map<Integer, IValue> content = new HashMap<>();
        content.put(1, new IntValue(1));
        heap.setContent(content);
        content.put(2, new IntValue(2));
        assertFalse(heap.isDefined(2));
    }

    @Test
    void addressesAreNotReusedAfterTheContentWasReplaced() {
        heap.allocate(new IntValue(10));
        heap.allocate(new IntValue(20));
        heap.setContent(Map.of()); // everything was garbage
        assertEquals(3, heap.allocate(new IntValue(30)));
    }

    @Test
    void toStringUsesTheLabNotationSortedByAddress() {
        heap.allocate(new IntValue(20));
        heap.allocate(new RefValue(1, new IntType()));
        assertEquals("{1 -> 20, 2 -> (1, int)}", heap.toString());
    }

    @Test
    void toStringOfAnEmptyHeap() {
        assertEquals("{}", heap.toString());
    }
}
