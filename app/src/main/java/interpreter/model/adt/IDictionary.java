package interpreter.model.adt;

import interpreter.exceptions.ADTException;

import java.util.Map;

public interface IDictionary<K, V> {
    boolean isDefined(K key);

    /** @throws ADTException if the key is not defined */
    V lookup(K key) throws ADTException;

    /** Adds the binding key -> value (replaces the value if the key is already defined). */
    void put(K key, V value);

    /** Replaces the value of an existing key. @throws ADTException if the key is not defined */
    void update(K key, V value) throws ADTException;

    /** Removes the binding of the key. @throws ADTException if the key is not defined */
    void remove(K key) throws ADTException;

    /** A read-only copy of the bindings, in insertion order (used by closeRFile's tests and the log file). */
    Map<K, V> toMap();
}
