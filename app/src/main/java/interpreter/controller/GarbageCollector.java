package interpreter.controller;

import interpreter.model.value.IValue;
import interpreter.model.value.RefValue;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class GarbageCollector {
    private GarbageCollector() {
    }

    /** The addresses that occur in the RefValues of the SymTable. */
    public static Set<Integer> getAddrFromSymTable(Collection<IValue> symTableValues) {
        return symTableValues.stream()
                .filter(v -> v instanceof RefValue)
                .map(v -> ((RefValue) v).address())
                .collect(Collectors.toSet());
    }

    /** UNSAFE: keeps only the heap entries referred to directly from the SymTable. */
    public static Map<Integer, IValue> unsafeGarbageCollector(Set<Integer> symTableAddr,
                                                              Map<Integer, IValue> heap) {
        return heap.entrySet().stream()
                .filter(e -> symTableAddr.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * The addresses of the heap locations that can be reached from the roots: the roots themselves
     * (if they are in the heap), and every location whose address occurs in the RefValue stored in a
     * reachable location, repeated until no new address is found. Iterative, so a long chain of
     * references cannot overflow the stack, and a visited set makes cycles harmless.
     */
    public static Set<Integer> reachableAddresses(Set<Integer> roots, Map<Integer, IValue> heap) {
        Set<Integer> reachable = new HashSet<>();
        Deque<Integer> toVisit = new ArrayDeque<>(roots);
        while (!toVisit.isEmpty()) {
            int address = toVisit.pop();
            if (!heap.containsKey(address) || !reachable.add(address)) continue; // not in the heap / seen
            if (heap.get(address) instanceof RefValue ref) toVisit.push(ref.address());
        }
        return reachable;
    }

    /** SAFE: keeps the locations referred to from the SymTable and those reachable through the heap. */
    public static Map<Integer, IValue> safeGarbageCollector(Set<Integer> symTableAddr,
                                                            Map<Integer, IValue> heap) {
        Set<Integer> reachable = reachableAddresses(symTableAddr, heap);
        return heap.entrySet().stream()
                .filter(e -> reachable.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
