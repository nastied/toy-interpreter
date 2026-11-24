package interpreter.model.type;

import interpreter.model.value.IValue;
import interpreter.model.value.RefValue;

/** Ref T: a reference to a heap location holding a value of type T (T may itself be a reference type). */
public record RefType(IType inner) implements IType {
    /** The null reference: address 0 is never allocated. */
    @Override
    public IValue defaultValue() {
        return new RefValue(0, inner);
    }

    @Override
    public String toString() {
        return "Ref(" + inner + ")";
    }
}
