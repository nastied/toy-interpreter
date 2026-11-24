package interpreter.model.adt;

import interpreter.exceptions.ADTException;
import interpreter.model.value.IValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MyHeap implements IHeap {
    private Map<Integer, IValue> heap = new HashMap<>();
    private int nextFree = 1; // address 0 means null; addresses are never reused

    @Override
    public int allocate(IValue value) {
        int address = nextFree++;
        heap.put(address, value);
        return address;
    }

    @Override
    public IValue get(int address) throws ADTException {
        if (!heap.containsKey(address)) throw new ADTException("address " + address + " is not in the heap");
        return heap.get(address);
    }

    @Override
    public void update(int address, IValue value) throws ADTException {
        if (!heap.containsKey(address)) throw new ADTException("address " + address + " is not in the heap");
        heap.put(address, value);
    }

    @Override
    public boolean isDefined(int address) {
        return heap.containsKey(address);
    }

    @Override
    public Map<Integer, IValue> getContent() {
        return Collections.unmodifiableMap(new TreeMap<>(heap));
    }

    @Override
    public void setContent(Map<Integer, IValue> content) {
        heap = new HashMap<>(content);
    }

    /** As in the lab notation: {1 -> 20, 2 -> (1, int)}, sorted by address. */
    @Override
    public String toString() {
        return new TreeMap<>(heap).entrySet().stream()
                .map(e -> e.getKey() + " -> " + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
