package interpreter.model.value;

import interpreter.model.type.IType;
import interpreter.model.type.IntType;

public record IntValue(int val) implements IValue {
    @Override
    public IType getType() {
        return new IntType();
    }

    @Override
    public String toString() {
        return Integer.toString(val);
    }
}
