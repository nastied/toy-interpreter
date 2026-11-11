package interpreter.model.stmt;

import interpreter.model.PrgState;

/** Rule S1: Stmt1; Stmt2 -> push Stmt2, then Stmt1 (so Stmt1 is executed first). */
public record CompStmt(IStmt first, IStmt second) implements IStmt {
    @Override
    public PrgState execute(PrgState state) {
        state.getExeStack().push(second);
        state.getExeStack().push(first);
        return state;
    }

    /** Infix form: left - root - right, e.g. (a = 1; print(a)). */
    @Override
    public String toString() {
        return "(" + first + "; " + second + ")";
    }
}
