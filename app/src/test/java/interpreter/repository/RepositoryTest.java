package interpreter.repository;

import interpreter.exceptions.MyException;
import interpreter.exceptions.RepositoryException;
import interpreter.model.PrgState;
import interpreter.model.stmt.NopStmt;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static interpreter.TestSupport.emptyState;
import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {
    @TempDir
    Path dir;

    private String logPath() {
        return dir.resolve("log.txt").toString();
    }

    @Test
    void getCrtPrgReturnsTheStateGivenToTheConstructor() {
        PrgState state = emptyState();
        IRepository repo = new Repository(state, logPath());
        assertSame(state, repo.getCrtPrg());
    }

    @Test
    void getCrtPrgReturnsTheSameStateEveryTime() {
        IRepository repo = new Repository(emptyState(), logPath());
        assertSame(repo.getCrtPrg(), repo.getCrtPrg());
    }

    @Test
    void logWritesTheStateInTheLabFormat() throws Exception {
        IRepository repo = new Repository(PrgState.of(new NopStmt()), logPath());
        repo.logPrgStateExec();
        // a state is followed by an empty line
        assertEquals(List.of("ExeStack:", "nop", "SymTable:", "Out:", "FileTable:", ""),
                Files.readAllLines(dir.resolve("log.txt")));
    }

    @Test
    void logCreatesTheFileIfItDoesNotExist() throws Exception {
        assertFalse(Files.exists(dir.resolve("log.txt")));
        new Repository(emptyState(), logPath()).logPrgStateExec();
        assertTrue(Files.exists(dir.resolve("log.txt")));
    }

    @Test
    void logAppendsInsteadOfOverwriting() throws Exception {
        PrgState state = emptyState();
        IRepository repo = new Repository(state, logPath());
        repo.logPrgStateExec();
        state.getOut().add(new IntValue(1));
        repo.logPrgStateExec();
        String log = Files.readString(dir.resolve("log.txt"));
        assertEquals(2, log.split("ExeStack:", -1).length - 1);
        assertTrue(log.contains("Out:\n1\n"));
    }

    @Test
    void logShowsTheStateAtTheMomentOfTheCall() throws Exception {
        PrgState state = emptyState();
        IRepository repo = new Repository(state, logPath());
        repo.logPrgStateExec();
        state.getSymTable().put("v", new IntValue(2));
        repo.logPrgStateExec();
        List<String> lines = Files.readAllLines(dir.resolve("log.txt"));
        assertEquals(1, lines.stream().filter(l -> l.equals("v --> 2")).count()); // only in the second state
    }

    @Test
    void aDifferentRepositoryOnTheSameFileAppendsToo() throws Exception {
        new Repository(emptyState(), logPath()).logPrgStateExec();
        new Repository(emptyState(), logPath()).logPrgStateExec();
        assertEquals(2, Files.readString(dir.resolve("log.txt")).split("ExeStack:", -1).length - 1);
    }

    @Test
    void unwritableLogFileThrowsARepositoryException() {
        String bad = dir.resolve("no-such-folder").resolve("log.txt").toString();
        IRepository repo = new Repository(emptyState(), bad);
        var ex = assertThrows(RepositoryException.class, repo::logPrgStateExec);
        assertTrue(ex.getMessage().startsWith("cannot write the log file"));
        assertInstanceOf(MyException.class, ex);
    }

    @Test
    void aDirectoryIsNotAValidLogFile() {
        IRepository repo = new Repository(emptyState(), dir.toString());
        assertThrows(RepositoryException.class, repo::logPrgStateExec);
    }

    @Test
    void theLogFileIsClosedAfterEveryCall() throws IOException, MyException {
        IRepository repo = new Repository(emptyState(), logPath());
        repo.logPrgStateExec();
        Files.delete(dir.resolve("log.txt")); // fails on Windows if the file were still open
        assertFalse(Files.exists(dir.resolve("log.txt")));
    }
}
