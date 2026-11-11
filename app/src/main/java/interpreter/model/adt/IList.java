package interpreter.model.adt;

import interpreter.exceptions.ADTException;

import java.util.List;

public interface IList<T> {
    void add(T v);

    /** @throws ADTException if the index is out of bounds */
    T get(int index) throws ADTException;

    int size();

    /** A read-only copy of the elements, in insertion order. */
    List<T> toList();
}
