package interpreter.model.value;

import interpreter.model.type.IntType;
import interpreter.model.type.RefType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefValueTest {
    @Test
    void theTypeIsARefToTheLocationType() {
        assertEquals(new RefType(new IntType()), new RefValue(1, new IntType()).getType());
    }

    @Test
    void aReferenceToAReferenceHasTypeRefRef() {
        var v = new RefValue(10, new RefType(new IntType()));
        assertEquals(new RefType(new RefType(new IntType())), v.getType());
    }

    @Test
    void valuesWithTheSameAddressAndLocationTypeAreEqual() {
        assertEquals(new RefValue(1, new IntType()), new RefValue(1, new IntType()));
        assertEquals(new RefValue(1, new IntType()).hashCode(), new RefValue(1, new IntType()).hashCode());
    }

    @Test
    void valuesWithADifferentAddressOrLocationTypeAreDifferent() {
        assertNotEquals(new RefValue(1, new IntType()), new RefValue(2, new IntType()));
        assertNotEquals(new RefValue(1, new IntType()), new RefValue(1, new RefType(new IntType())));
    }

    @Test
    void aReferenceIsNeverEqualToAnInteger() {
        assertNotEquals(new RefValue(1, new IntType()), new IntValue(1));
    }

    @Test
    void toStringShowsTheAddressAndTheLocationType() {
        assertEquals("(1, int)", new RefValue(1, new IntType()).toString());
        assertEquals("(2, Ref(int))", new RefValue(2, new RefType(new IntType())).toString());
    }

    @Test
    void addressZeroIsTheNullReference() {
        assertEquals(0, new RefType(new IntType()).defaultValue() instanceof RefValue r ? r.address() : -1);
    }
}
