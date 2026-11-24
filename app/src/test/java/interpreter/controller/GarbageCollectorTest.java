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
}
