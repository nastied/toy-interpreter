package interpreter.model.adt;

import interpreter.exceptions.ADTException;

public interface IStack<T> {
    void push(T v);

    /** Removes and returns the top element. @throws ADTException if the stack is empty */
    T pop() throws ADTException;

    boolean isEmpty();
}
