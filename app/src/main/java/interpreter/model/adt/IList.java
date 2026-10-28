package interpreter.model.adt;

import interpreter.exceptions.ADTException;

public interface IList<T> {
    void add(T v);

    /** @throws ADTException if the index is out of bounds */
    T get(int index) throws ADTException;

    int size();
}
