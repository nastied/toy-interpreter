package interpreter.controller;

import interpreter.model.type.IntType;
import interpreter.model.type.RefType;
import interpreter.model.value.IValue;
import interpreter.model.value.IntValue;
import interpreter.model.value.RefValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Laboratory 8, exercise 2. The program state:
 * <pre>
 * SymTable: a --> (3, Ref(int))      Heap: 1 --> 7
 *           c --> (0, int)                 2 --> 8
 *                                          3 --> (2, int)
 *                                          4 --> (1, int)
 *                                          5 --> (4, Ref(int))
 * </pre>
 * Reachable: 3 (from a), then 2 (from the content of 3). c is the null reference (address 0, not in the heap).
 * The locations 1, 4 and 5 are garbage, although 5 refers to 4 and 4 refers to 1.
 */
class GarbageCollectorStateTest {
    private final List<IValue> symTable = List.of(
            new RefValue(3, new RefType(new IntType())),   // a --> (3, Ref(int))
            new RefValue(0, new IntType()));               // c --> (0, int)

    private final Map<Integer, IValue> heap = Map.of(
            1, new IntValue(7),
            2, new IntValue(8),
            3, new RefValue(2, new IntType()),
            4, new RefValue(1, new IntType()),
            5, new RefValue(4, new RefType(new IntType())));

    @Test
    void theRootsAreTheAddressesOfTheSymTableIncludingTheNullReference() {
        assertEquals(Set.of(0, 3), GarbageCollector.getAddrFromSymTable(symTable));
    }

    @Test
    void theReachableAddressesAreTwoAndThree() {
        Set<Integer> roots = GarbageCollector.getAddrFromSymTable(symTable);
        assertEquals(Set.of(2, 3), GarbageCollector.reachableAddresses(roots, heap));
    }

    @Test
    void theSafeCollectorKeepsTwoAndThree() {
        Set<Integer> roots = GarbageCollector.getAddrFromSymTable(symTable);
        assertEquals(Set.of(2, 3), GarbageCollector.safeGarbageCollector(roots, heap).keySet());
    }

    @Test
    void theUnsafeCollectorKeepsOnlyThree() {
        Set<Integer> roots = GarbageCollector.getAddrFromSymTable(symTable);
        assertEquals(Set.of(3), GarbageCollector.unsafeGarbageCollector(roots, heap).keySet());
    }

    @Test
    void aChainOfGarbageDoesNotKeepItselfAlive() {
        Set<Integer> roots = GarbageCollector.getAddrFromSymTable(symTable);
        Map<Integer, IValue> kept = GarbageCollector.safeGarbageCollector(roots, heap);
        assertFalse(kept.containsKey(5)); // 5 refers to 4, 4 refers to 1: nobody refers to 5
        assertFalse(kept.containsKey(4));
        assertFalse(kept.containsKey(1));
    }

    @Test
    void theKeptLocationsKeepTheirValues() {
        Set<Integer> roots = GarbageCollector.getAddrFromSymTable(symTable);
        Map<Integer, IValue> kept = GarbageCollector.safeGarbageCollector(roots, heap);
        assertEquals(new RefValue(2, new IntType()), kept.get(3));
        assertEquals(new IntValue(8), kept.get(2));
    }
}
