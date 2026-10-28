package interpreter.model.adt;

import interpreter.exceptions.ADTException;

public interface IDictionary<K, V> {
    boolean isDefined(K key);

    /** @throws ADTException if the key is not defined */
    V lookup(K key) throws ADTException;

    /** Adds the binding key -> value (replaces the value if the key is already defined). */
    void put(K key, V value);

    /** Replaces the value of an existing key. @throws ADTException if the key is not defined */
    void update(K key, V value) throws ADTException;
}
