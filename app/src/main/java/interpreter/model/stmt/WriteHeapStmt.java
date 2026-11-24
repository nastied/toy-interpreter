package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.IExp;
import interpreter.model.value.IValue;
import interpreter.model.value.RefValue;

/** Rule S12: wH(v, exp) -> v = (a, T); update the heap at a with Eval(exp), whose type must be T. */
public record WriteHeapStmt(String varName, IExp exp) implements IStmt {
    @Override
    public PrgState execute(PrgState state) throws MyException {
        var symTable = state.getSymTable();
        var heap = state.getHeap();
        if (!symTable.isDefined(varName))
            throw new StatementException("wH: " + varName + " is not declared");
        if (!(symTable.lookup(varName) instanceof RefValue ref))
            throw new StatementException("wH: " + varName + " is not a reference");
        if (!heap.isDefined(ref.address()))
            throw new StatementException("wH: address " + ref.address() + " is not in the heap");
        IValue value = exp.eval(symTable, heap);
        if (!value.getType().equals(ref.locationType()))
            throw new StatementException("wH: " + exp + " has type " + value.getType()
                    + ", expected " + ref.locationType());
        heap.update(ref.address(), value);
        return state;
    }

    @Override
    public String toString() {
        return "wH(" + varName + ", " + exp + ")";
    }
}
