package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.IExp;
import interpreter.model.value.BoolValue;

/**
 * Rule S13: while (exp) s -> if exp is true, push the loop itself and then its body; if false, nothing.
 * No Java loop: the statement puts itself back on the ExeStack, under the body.
 */
public record WhileStmt(IExp cond, IStmt body) implements IStmt {
    @Override
    public PrgState execute(PrgState state) throws MyException {
        if (!(cond.eval(state.getSymTable(), state.getHeap()) instanceof BoolValue(boolean c)))
            throw new StatementException("condition " + cond + " is not a boolean");
        if (c) {
            state.getExeStack().push(this);
            state.getExeStack().push(body);
        }
        return state;
    }

    @Override
    public String toString() {
        return "(while (" + cond + ") " + body + ")";
    }
}
