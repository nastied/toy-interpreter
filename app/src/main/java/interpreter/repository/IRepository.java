package interpreter.repository;

import interpreter.exceptions.MyException;
import interpreter.model.PrgState;

public interface IRepository {
    PrgState getCrtPrg();

    /** Appends the current program state to the log file (new in A3). */
    void logPrgStateExec() throws MyException;
}
