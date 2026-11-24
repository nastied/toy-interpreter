package interpreter.model.value;

import interpreter.model.type.IType;
import interpreter.model.type.RefType;

/** A reference value: the address of a heap location and the type of the value stored there. */
public record RefValue(int address, IType locationType) implements IValue {
    @Override
    public IType getType() {
        return new RefType(locationType);
    }

    @Override
    public String toString() {
        return "(" + address + ", " + locationType + ")";
    }
}
