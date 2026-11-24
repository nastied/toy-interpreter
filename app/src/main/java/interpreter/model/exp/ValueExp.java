package interpreter.model.exp;

import interpreter.model.adt.IDictionary;
import interpreter.model.adt.IHeap;
import interpreter.model.value.IValue;

/** Rule E1: a constant evaluates to itself. */
public record ValueExp(IValue value) implements IExp {
    @Override
    public IValue eval(IDictionary<String, IValue> symTable, IHeap heap) {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
