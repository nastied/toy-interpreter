package interpreter.controller;

import interpreter.model.type.BoolType;
import interpreter.model.type.IntType;
import interpreter.model.type.RefType;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IValue;
import interpreter.model.value.IntValue;
import interpreter.model.value.RefValue;
import interpreter.model.value.StringValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GarbageCollectorTest {
    // the state of Ex9 after new(v, 30): v -> (3, int), a -> (2, Ref(int)); location 4 is plain garbage
    private final List<IValue> symTable = List.of(
            new RefValue(3, new IntType()), new RefValue(2, new RefType(new IntType())));
    private final Map<Integer, IValue> heap = Map.of(
            1, new IntValue(20), 2, new RefValue(1, new IntType()),
            3, new IntValue(30), 4, new IntValue(7));

    // ----- getAddrFromSymTable -----

    @Test
    void theAddressesOfTheSymTableAreTheAddressesOfItsReferences() {
        assertEquals(Set.of(2, 3), GarbageCollector.getAddrFromSymTable(symTable));
    }

    @Test
    void valuesThatAreNotReferencesAreIgnored() {
        List<IValue> values = List.of(new IntValue(5), new BoolValue(true), new StringValue("x"),
                new RefValue(7, new BoolType()));
        assertEquals(Set.of(7), GarbageCollector.getAddrFromSymTable(values));
    }

    @Test
    void twoVariablesPointingToTheSameLocationGiveOneAddress() {
        List<IValue> values = List.of(new RefValue(1, new IntType()), new RefValue(1, new IntType()));
        assertEquals(Set.of(1), GarbageCollector.getAddrFromSymTable(values));
    }

    @Test
    void anEmptySymTableHasNoAddresses() {
        assertTrue(GarbageCollector.getAddrFromSymTable(List.of()).isEmpty());
    }

    // ----- the unsafe collector -----

    @Test
    void unsafeCollectorRemovesTheLocationsNoVariableRefersTo() {
        Map<Integer, IValue> kept = GarbageCollector.unsafeGarbageCollector(Set.of(2, 3), heap);
        assertFalse(kept.containsKey(4));
    }

    @Test
    void unsafeCollectorKeepsTheLocationsTheSymTableRefersTo() {
        Map<Integer, IValue> kept = GarbageCollector.unsafeGarbageCollector(Set.of(2, 3), heap);
        assertTrue(kept.containsKey(2) && kept.containsKey(3));
        assertEquals(new IntValue(30), kept.get(3));
    }

    @Test
    void unsafeCollectorRemovesALocationThatIsStillReachableThroughTheHeap() {
        // location 1 is only referred to from location 2: this is why the collector is "unsafe"
        Map<Integer, IValue> kept = GarbageCollector.unsafeGarbageCollector(
                GarbageCollector.getAddrFromSymTable(symTable), heap);
        assertEquals(Set.of(2, 3), kept.keySet());
    }

    @Test
    void unsafeCollectorWithoutReferencesEmptiesTheHeap() {
        assertTrue(GarbageCollector.unsafeGarbageCollector(Set.of(), heap).isEmpty());
    }

    @Test
    void unsafeCollectorIgnoresAddressesThatAreNotInTheHeap() {
        Map<Integer, IValue> kept = GarbageCollector.unsafeGarbageCollector(Set.of(0, 99), heap);
        assertTrue(kept.isEmpty());
    }

    @Test
    void unsafeCollectorDoesNotChangeTheMapItIsGiven() {
        GarbageCollector.unsafeGarbageCollector(Set.of(), heap); // heap is an immutable Map.of: it would throw
        assertEquals(4, heap.size());
    }

    // ----- reachableAddresses -----

    private static IValue ref(int address) {
        return new RefValue(address, new IntType());
    }

    @Test
    void reachableAddressesFollowTheReferencesStoredInTheHeap() {
        assertEquals(Set.of(1, 2, 3), GarbageCollector.reachableAddresses(Set.of(2, 3), heap));
    }

    @Test
    void reachableAddressesFollowAChainOfReferences() {
        Map<Integer, IValue> chain = Map.of(1, ref(2), 2, ref(3), 3, new IntValue(0), 4, new IntValue(9));
        assertEquals(Set.of(1, 2, 3), GarbageCollector.reachableAddresses(Set.of(1), chain));
    }

    @Test
    void aLocationNobodyRefersToIsNotReachable() {
        assertFalse(GarbageCollector.reachableAddresses(Set.of(2, 3), heap).contains(4));
    }

    @Test
    void aRootThatIsNotInTheHeapIsIgnored() {
        assertEquals(Set.of(3), GarbageCollector.reachableAddresses(Set.of(0, 99, 3), heap));
    }

    @Test
    void noRootsReachNothing() {
        assertTrue(GarbageCollector.reachableAddresses(Set.of(), heap).isEmpty());
    }

    @Test
    void aReferenceInTheHeapToAnAddressThatIsNotThereIsIgnored() {
        Map<Integer, IValue> dangling = Map.of(1, ref(50));
        assertEquals(Set.of(1), GarbageCollector.reachableAddresses(Set.of(1), dangling));
    }

    @Test
    void aCycleOfReferencesTerminates() {
        Map<Integer, IValue> cycle = Map.of(1, ref(2), 2, ref(1));
        assertEquals(Set.of(1, 2), GarbageCollector.reachableAddresses(Set.of(1), cycle));
    }

    @Test
    void aLocationThatRefersToItselfTerminates() {
        assertEquals(Set.of(1), GarbageCollector.reachableAddresses(Set.of(1), Map.of(1, ref(1))));
    }

    @Test
    void twoReferencesToTheSameLocationReachItOnce() {
        Map<Integer, IValue> shared = Map.of(1, ref(3), 2, ref(3), 3, new IntValue(7));
        assertEquals(Set.of(1, 2, 3), GarbageCollector.reachableAddresses(Set.of(1, 2), shared));
    }

    @Test
    void aVeryLongChainDoesNotOverflowTheStack() {
        int n = 20_000;
        Map<Integer, IValue> longChain = new java.util.HashMap<>();
        for (int i = 1; i < n; i++) longChain.put(i, ref(i + 1));
        longChain.put(n, new IntValue(0));
        assertEquals(n, GarbageCollector.reachableAddresses(Set.of(1), longChain).size());
    }

    // ----- the safe collector -----

    @Test
    void keepsTheLocationsReachableThroughTheHeap() {
        Set<Integer> roots = GarbageCollector.getAddrFromSymTable(symTable);
        Map<Integer, IValue> kept = GarbageCollector.safeGarbageCollector(roots, heap);
        assertEquals(Set.of(1, 2, 3), kept.keySet()); // 4 is garbage, 1 is not
    }

    @Test
    void safeCollectorKeepsTheValuesOfTheKeptLocations() {
        Map<Integer, IValue> kept = GarbageCollector.safeGarbageCollector(Set.of(2, 3), heap);
        assertEquals(new IntValue(20), kept.get(1));
        assertEquals(new RefValue(1, new IntType()), kept.get(2));
        assertEquals(new IntValue(30), kept.get(3));
    }

    @Test
    void safeCollectorRemovesTheUnreachableLocations() {
        Map<Integer, IValue> kept = GarbageCollector.safeGarbageCollector(Set.of(2, 3), heap);
        assertFalse(kept.containsKey(4));
    }

    @Test
    void safeCollectorRemovesAGarbageCycle() {
        Map<Integer, IValue> withCycle = Map.of(1, new IntValue(1), 5, ref(6), 6, ref(5));
        assertEquals(Set.of(1), GarbageCollector.safeGarbageCollector(Set.of(1), withCycle).keySet());
    }

    @Test
    void safeCollectorWithoutRootsEmptiesTheHeap() {
        assertTrue(GarbageCollector.safeGarbageCollector(Set.of(), heap).isEmpty());
    }

    @Test
    void safeCollectorKeepsEverythingIfEverythingIsReachable() {
        Map<Integer, IValue> kept = GarbageCollector.safeGarbageCollector(Set.of(1, 2, 3, 4), heap);
        assertEquals(heap, kept);
    }

    @Test
    void safeCollectorDoesNotChangeTheMapItIsGiven() {
        GarbageCollector.safeGarbageCollector(Set.of(), heap); // heap is an immutable Map.of: it would throw
        assertEquals(4, heap.size());
    }

    @Test
    void safeCollectorKeepsAtLeastWhatTheUnsafeOneKeeps() {
        Set<Integer> roots = GarbageCollector.getAddrFromSymTable(symTable);
        var safe = GarbageCollector.safeGarbageCollector(roots, heap).keySet();
        var unsafe = GarbageCollector.unsafeGarbageCollector(roots, heap).keySet();
        assertTrue(safe.containsAll(unsafe));
        assertTrue(safe.size() > unsafe.size()); // here: the location 1
    }
}
