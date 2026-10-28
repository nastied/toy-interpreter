package interpreter.model.type;

import interpreter.model.value.BoolValue;
import interpreter.model.value.IValue;

public record BoolType() implements IType {
    @Override
    public IValue defaultValue() {
        return new BoolValue(false);
    }

    @Override
    public String toString() {
        return "bool";
    }
}
