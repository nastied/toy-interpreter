package interpreter;

import interpreter.exceptions.ADTException;
import interpreter.model.PrgState;
import interpreter.model.adt.MyDictionary;
import interpreter.model.adt.MyList;
import interpreter.model.adt.MyStack;
import interpreter.model.exp.IExp;
import interpreter.model.exp.ValueExp;
import interpreter.model.stmt.NopStmt;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;

/** Small helpers shared by the tests. */
public final class TestSupport {
    private TestSupport() {
    }

    /** A program state whose execution stack, symbol table and output are all empty. */
    public static PrgState emptyState() {
        PrgState state = new PrgState(new MyStack<>(), new MyDictionary<>(), new MyList<>(), new NopStmt());
        try {
            state.getExeStack().pop(); // remove the program pushed by the constructor
        } catch (ADTException e) {
            throw new IllegalStateException(e);
        }
        return state;
    }

    public static IExp num(int n) {
        return new ValueExp(new IntValue(n));
    }

    public static IExp bool(boolean b) {
        return new ValueExp(new BoolValue(b));
    }
}
