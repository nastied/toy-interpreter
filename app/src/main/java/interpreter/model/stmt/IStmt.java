package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.model.PrgState;

public interface IStmt {
    /** Applies the rule of this statement to the program state and returns the state. */
    PrgState execute(PrgState state) throws MyException;
}
