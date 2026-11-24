package interpreter.controller;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.stmt.IStmt;
import interpreter.repository.IRepository;

import java.io.PrintStream;
/**The allStep() loop is updated to trigger the repository's logger once for the initial state, and then again after every single oneStep().*/

import static interpreter.controller.GarbageCollector.getAddrFromSymTable;
import static interpreter.controller.GarbageCollector.safeGarbageCollector;

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

    /**
     * Executes the current program until its ExeStack is empty. The initial state is logged, and after every
     * step the state is logged, the garbage collector runs and the state is logged again.
     */
    public void allStep() throws MyException {
        PrgState prg = repo.getCrtPrg();
        repo.logPrgStateExec();
        display(prg); // only if the display flag is on
        while (!prg.getExeStack().isEmpty()) {
            oneStep(prg);
            repo.logPrgStateExec(); // after the step, before the collection
            collectGarbage(prg);
            repo.logPrgStateExec(); // after the collection
            display(prg);
        }
    }

    private void collectGarbage(PrgState prg) {
        prg.getHeap().setContent(safeGarbageCollector(
                getAddrFromSymTable(prg.getSymTable().toMap().values()),
                prg.getHeap().getContent()));
    }

    private void display(PrgState state) {
        if (displayFlag) {
            display.println(state);
            display.println();
        }
    }
}
