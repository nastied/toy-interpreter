package interpreter.repository;

import interpreter.exceptions.MyException;
import interpreter.exceptions.RepositoryException;
import interpreter.model.PrgState;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
/**The logPrgStateExec() method handles the actual writing. It opens the text file in "append" mode (true) so it doesn't overwrite previous steps, dumps the toLogString() text into it, and uses a try-with-resources block to guarantee the file is closed even if the interpreter crashes.*/

public class Repository implements IRepository {
    // a list, because later the repository will hold the states of several threads
    private final List<PrgState> states = new ArrayList<>();
    private final String logFilePath;

    public Repository(PrgState initialState, String logFilePath) {
        states.add(initialState);
        this.logFilePath = logFilePath;
    }

    @Override
    public PrgState getCrtPrg() {
        return states.getFirst();
    }

    /**
     * Opens the log file in append mode, writes the state and closes the file again (try-with-resources),
     * so nothing is lost if the program stops with an error later.
     */
    @Override
    public void logPrgStateExec() throws MyException {
        try (var logFile = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, true)))) {
            logFile.print(getCrtPrg().toLogString() + "\n");
        } catch (IOException e) {
            throw new RepositoryException("cannot write the log file " + logFilePath + ": " + e.getMessage());
        }
    }
}
