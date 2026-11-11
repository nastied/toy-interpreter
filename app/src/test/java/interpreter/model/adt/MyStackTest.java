package interpreter.model.adt;

import interpreter.exceptions.ADTException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyStackTest {
    private IStack<Integer> stack;

    @BeforeEach
    void setUp() {
        stack = new MyStack<>();
    }

    @Test
    void newStackIsEmpty() {
        assertTrue(stack.isEmpty());
    }

    @Test
    void stackWithAnElementIsNotEmpty() {
        stack.push(1);
        assertFalse(stack.isEmpty());
    }

    @Test
    void popReturnsElementsInLifoOrder() throws ADTException {
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertEquals(3, stack.pop());
        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
        assertTrue(stack.isEmpty());
    }

    @Test
    void popOnEmptyStackThrows() {
        var ex = assertThrows(ADTException.class, () -> stack.pop());
        assertEquals("pop from an empty stack", ex.getMessage());
    }

    @Test
    void popOnStackEmptiedByPopsThrows() throws ADTException {
        stack.push(1);
        stack.pop();
        assertThrows(ADTException.class, () -> stack.pop());
    }

    @Test
    void toStringShowsTheTopFirst() {
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertEquals("{3 | 2 | 1}", stack.toString());
    }

    @Test
    void toStringOfEmptyStack() {
        assertEquals("{}", stack.toString());
    }

    @Test
    void toListHasTheTopOfTheStackFirst() {
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertEquals(java.util.List.of(3, 2, 1), stack.toList());
    }

    @Test
    void toListOfAnEmptyStackIsEmpty() {
        assertTrue(stack.toList().isEmpty());
    }

    @Test
    void toListIsAReadOnlyCopy() throws ADTException {
        stack.push(1);
        var copy = stack.toList();
        assertThrows(UnsupportedOperationException.class, () -> copy.add(2));
        stack.push(2);
        assertEquals(1, copy.size()); // later changes of the stack do not show in the copy
        assertEquals(2, stack.pop());
    }
}
