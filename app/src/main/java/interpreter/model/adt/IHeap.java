package interpreter.model.adt;

import interpreter.exceptions.ADTException;
import interpreter.model.value.IValue;

import java.util.Map;

/** The heap: addresses (ints from 1) mapped to values; the heap itself chooses the address of a new value. */
public interface IHeap {
    /** Stores the value at a new address and returns that address (1, 2, 3 ...; 0 means null). */
    int allocate(IValue value);

    /** @throws ADTException if the address is not in the heap */
    IValue get(int address) throws ADTException;

    /** Replaces the value at an existing address. @throws ADTException if the address is not in the heap */
    void update(int address, IValue value) throws ADTException;

    boolean isDefined(int address);

    /** A read-only copy of the content, sorted by address. */
    Map<Integer, IValue> getContent();

    /** Replaces the content (used by the garbage collector); the next free address does not change. */
    void setContent(Map<Integer, IValue> content);
}
