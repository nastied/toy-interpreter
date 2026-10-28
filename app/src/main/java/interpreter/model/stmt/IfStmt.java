package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.IExp;
import interpreter.model.value.BoolValue;

/** Rule S5: if Exp then S1 else S2 -> push S1 if Exp is true, otherwise S2. */
public record IfStmt(IExp cond, IStmt thenS, IStmt elseS) implements IStmt {
    @Override
    public PrgState execute(PrgState state) throws MyException {
        if (!(cond.eval(state.getSymTable()) instanceof BoolValue(boolean c)))
            throw new StatementException("condition of the if statement is not a boolean");
        state.getExeStack().push(c ? thenS : elseS);
        return state;
    }

    @Override
    public String toString() {
        return "(if " + cond + " then " + StmtFormat.nested(thenS) + " else " + StmtFormat.nested(elseS) + ")";
    }
}
