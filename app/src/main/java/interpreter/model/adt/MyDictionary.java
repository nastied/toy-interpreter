package interpreter.model.adt;

import interpreter.exceptions.ADTException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MyDictionary<K, V> implements IDictionary<K, V> {
    // a HashMap that remembers the insertion order, so the displayed states are deterministic
    private final Map<K, V> map = new LinkedHashMap<>();

    @Override
    public boolean isDefined(K key) {
        return map.containsKey(key);
    }

    @Override
    public V lookup(K key) throws ADTException {
        if (!map.containsKey(key)) throw new ADTException("key " + key + " is not defined");
        return map.get(key);
    }

    @Override
    public void put(K key, V value) {
        map.put(key, value);
    }

    @Override
    public void update(K key, V value) throws ADTException {
        if (!map.containsKey(key)) throw new ADTException("key " + key + " is not defined");
        map.put(key, value);
    }

    /** As in the lab notation: {v -> 2, a -> false}. */
    @Override
    public String toString() {
        return map.entrySet().stream()
                .map(e -> e.getKey() + " -> " + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
