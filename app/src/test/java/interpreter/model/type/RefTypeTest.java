package interpreter.model.type;

import interpreter.model.value.RefValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefTypeTest {
    private static RefType ref(IType inner) {
        return new RefType(inner);
    }

    @Test
    void defaultValueIsTheNullReferenceToTheInnerType() {
        assertEquals(new RefValue(0, new IntType()), ref(new IntType()).defaultValue());
        assertEquals(new RefValue(0, new BoolType()), ref(new BoolType()).defaultValue());
    }

    @Test
    void defaultValueOfANestedReferenceIsNullOfTheInnerReferenceType() {
        assertEquals(new RefValue(0, ref(new BoolType())), ref(ref(new BoolType())).defaultValue());
    }

    @Test
    void defaultValueHasTheTypeOfTheReferenceType() {
        IType t = ref(ref(new IntType()));
        assertEquals(t, t.defaultValue().getType());
    }

    @Test
    void nestedReferenceTypesAreComparedRecursively() {
        assertEquals(ref(ref(new IntType())), ref(ref(new IntType())));
        assertEquals(ref(ref(new IntType())).hashCode(), ref(ref(new IntType())).hashCode());
    }

    @Test
    void refRefIntDiffersFromRefInt() {
        assertNotEquals(ref(ref(new IntType())), ref(new IntType()));
        assertNotEquals(ref(new IntType()), ref(ref(new IntType())));
    }

    @Test
    void referencesToDifferentTypesAreDifferent() {
        assertNotEquals(ref(new IntType()), ref(new BoolType()));
        assertNotEquals(ref(ref(new IntType())), ref(ref(new StringType())));
    }

    @Test
    void aReferenceTypeDiffersFromItsInnerType() {
        assertNotEquals(ref(new IntType()), new IntType());
        assertNotEquals(new IntType(), ref(new IntType()));
    }

    @Test
    void toStringShowsTheNesting() {
        assertEquals("Ref(int)", ref(new IntType()).toString());
        assertEquals("Ref(Ref(string))", ref(ref(new StringType())).toString());
    }
}
