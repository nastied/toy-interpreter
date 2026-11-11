package interpreter.controller;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.stmt.IStmt;
import interpreter.repository.IRepository;

import java.io.PrintStream;
/**The allStep() loop is updated to trigger the repository's logger once for the initial state, and then again after every single oneStep().*/

public class Controller {
    private final IRepository repo; // interface type: the implementation can be replaced
    private final boolean displayFlag;
    private final PrintStream display;

    public Controller(IRepository repo, boolean displayFlag) {
        this(repo, displayFlag, System.out);
    }

    public Controller(IRepository repo, boolean displayFlag, PrintStream display) {
        this.repo = repo;
        this.displayFlag = displayFlag;
        this.display = display;
    }

    /** Executes the statement on top of the ExeStack (one rule S1-S10); rule S7 if the stack is empty. */
    public PrgState oneStep(PrgState state) throws MyException {
        var stack = state.getExeStack();
        if (stack.isEmpty()) throw new StatementException("execution stack is empty");
        IStmt crtStmt = stack.pop();
        return crtStmt.execute(state);
    }

    /** Executes the current program until its ExeStack is empty, logging the initial state and every step. */
    public void allStep() throws MyException {
        PrgState prg = repo.getCrtPrg();
        repo.logPrgStateExec();
        display(prg); // only if the display flag is on
        while (!prg.getExeStack().isEmpty()) {
            oneStep(prg);
            repo.logPrgStateExec();
            display(prg);
        }
    }

    private void display(PrgState state) {
        if (displayFlag) {
            display.println(state);
            display.println();
        }
    }
}
