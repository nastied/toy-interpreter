package interpreter.model.exp;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.model.adt.IDictionary;
import interpreter.model.adt.IHeap;
import interpreter.model.adt.MyDictionary;
import interpreter.model.adt.MyHeap;
import interpreter.model.type.IntType;
import interpreter.model.type.RefType;
import interpreter.model.value.IValue;
import interpreter.model.value.IntValue;
import interpreter.model.value.RefValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.bool;
import static interpreter.TestSupport.num;
import static org.junit.jupiter.api.Assertions.*;

class ReadHeapExpTest {
    private IDictionary<String, IValue> symTable;
    private IHeap heap;

    @BeforeEach
    void setUp() {
        symTable = new MyDictionary<>();
        heap = new MyHeap();
        // the state of Ex7: v -> (1, int), a -> (2, Ref(int)), heap {1 -> 20, 2 -> (1, int)}
        heap.allocate(new IntValue(20));
        heap.allocate(new RefValue(1, new IntType()));
        symTable.put("v", new RefValue(1, new IntType()));
        symTable.put("a", new RefValue(2, new RefType(new IntType())));
    }

    @Test
    void readsTheValueAtTheAddress() throws MyException {
        assertEquals(new IntValue(20), new ReadHeapExp(new VarExp("v")).eval(symTable, heap));
    }

    @Test
    void readsThroughTwoLevelsOfReferences() throws MyException {
        IExp e = new ReadHeapExp(new ReadHeapExp(new VarExp("a")));
        assertEquals(new IntValue(20), e.eval(symTable, heap));
    }

    @Test
    void theValueReadMayItselfBeAReference() throws MyException {
        assertEquals(new RefValue(1, new IntType()), new ReadHeapExp(new VarExp("a")).eval(symTable, heap));
    }

    @Test
    void canBeUsedInsideOtherExpressions() throws MyException {
        IExp e = new ArithExp(ArithOp.ADD, new ReadHeapExp(new ReadHeapExp(new VarExp("a"))), num(5));
        assertEquals(new IntValue(25), e.eval(symTable, heap));
    }

    @Test
    void doesNotChangeTheHeap() throws MyException {
        String before = heap.toString();
        new ReadHeapExp(new VarExp("v")).eval(symTable, heap);
        assertEquals(before, heap.toString());
    }

    @Test
    void anIntegerIsNotAReference() {
        var ex = assertThrows(ExpressionException.class, () -> new ReadHeapExp(num(5)).eval(symTable, heap));
        assertEquals("rH: 5 is not a reference", ex.getMessage());
    }

    @Test
    void aBooleanIsNotAReference() {
        assertThrows(ExpressionException.class, () -> new ReadHeapExp(bool(true)).eval(symTable, heap));
    }

    @Test
    void anAddressThatIsNotInTheHeapThrows() {
        symTable.put("p", new RefValue(7, new IntType()));
        var ex = assertThrows(ExpressionException.class, () -> new ReadHeapExp(new VarExp("p")).eval(symTable, heap));
        assertEquals("rH: address 7 is not in the heap", ex.getMessage());
    }

    @Test
    void theNullReferenceCannotBeRead() {
        symTable.put("n", new RefType(new IntType()).defaultValue());
        var ex = assertThrows(ExpressionException.class, () -> new ReadHeapExp(new VarExp("n")).eval(symTable, heap));
        assertEquals("rH: address 0 is not in the heap", ex.getMessage());
    }

    @Test
    void errorOfTheOperandIsPropagated() {
        assertThrows(ExpressionException.class, () -> new ReadHeapExp(new VarExp("undefined")).eval(symTable, heap));
    }

    @Test
    void toStringShowsTheCall() {
        assertEquals("rH(v)", new ReadHeapExp(new VarExp("v")).toString());
        assertEquals("rH(rH(a))", new ReadHeapExp(new ReadHeapExp(new VarExp("a"))).toString());
    }
}
