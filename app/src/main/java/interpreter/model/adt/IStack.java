package interpreter.model.adt;

import interpreter.exceptions.ADTException;

import java.util.List;

public interface IStack<T> {
    void push(T v);

    /** Removes and returns the top element. @throws ADTException if the stack is empty */
    T pop() throws ADTException;

    boolean isEmpty();

    /** A read-only copy of the elements, the top of the stack first. */
    List<T> toList();
}
