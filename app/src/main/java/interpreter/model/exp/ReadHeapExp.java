package interpreter.model.exp;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.model.adt.IDictionary;
import interpreter.model.adt.IHeap;
import interpreter.model.value.IValue;
import interpreter.model.value.RefValue;

/** Rule E6: rH(exp) -> exp must evaluate to a reference (a, T); the value is the content of the heap at a. */
public record ReadHeapExp(IExp exp) implements IExp {
    @Override
    public IValue eval(IDictionary<String, IValue> symTable, IHeap heap) throws MyException {
        if (!(exp.eval(symTable, heap) instanceof RefValue ref))
            throw new ExpressionException("rH: " + exp + " is not a reference");
        if (!heap.isDefined(ref.address()))
            throw new ExpressionException("rH: address " + ref.address() + " is not in the heap");
        return heap.get(ref.address());
    }

    @Override
    public String toString() {
        return "rH(" + exp + ")";
    }
}
