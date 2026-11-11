package interpreter.model.stmt;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.VarExp;
import interpreter.model.type.StringType;
import interpreter.model.value.StringValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static interpreter.TestSupport.closeOpenFiles;
import static interpreter.TestSupport.emptyState;
import static interpreter.TestSupport.num;
import static interpreter.TestSupport.str;
import static org.junit.jupiter.api.Assertions.*;

class OpenRFileStmtTest {
    @TempDir
    Path dir; // a new empty folder for every test

    private PrgState state;
    private StringValue fileName;
    private IStmt open;

    @BeforeEach
    void setUp() throws IOException {
        Path f = dir.resolve("numbers.in");
        Files.writeString(f, "7\n");
        fileName = new StringValue(f.toString());
        open = new OpenRFileStmt(str(f.toString()));
        state = emptyState();
    }

    @AfterEach
    void closeFiles() throws IOException {
        closeOpenFiles(state);
    }

    @Test
    void addsTheFileToTheFileTable() throws MyException {
        open.execute(state);
        assertTrue(state.getFileTable().isDefined(fileName));
        assertEquals(1, state.getFileTable().toMap().size());
    }

    @Test
    void theReaderInTheFileTableReadsTheFile() throws Exception {
        open.execute(state);
        BufferedReader reader = state.getFileTable().lookup(fileName);
        assertEquals("7", reader.readLine());
    }

    @Test
    void theFileStaysOpenAfterTheStatement() throws Exception {
        open.execute(state);
        BufferedReader reader = state.getFileTable().lookup(fileName);
        assertTrue(reader.ready()); // would throw "Stream closed" if the reader had been closed
    }

    @Test
    void theFileNameCanComeFromAVariable() throws MyException {
        new VarDeclStmt("f", new StringType()).execute(state);
        new AssignStmt("f", str(fileName.val())).execute(state);
        new OpenRFileStmt(new VarExp("f")).execute(state);
        assertTrue(state.getFileTable().isDefined(fileName));
    }

    @Test
    void nonStringExpressionThrows() {
        var ex = assertThrows(StatementException.class, () -> new OpenRFileStmt(num(1)).execute(state));
        assertEquals("openRFile: 1 is not a string", ex.getMessage());
        assertTrue(state.getFileTable().toMap().isEmpty());
    }

    @Test
    void openingTheSameFileTwiceThrows() throws MyException {
        open.execute(state);
        var ex = assertThrows(StatementException.class, () -> open.execute(state));
        assertTrue(ex.getMessage().contains("is already open"));
        assertEquals(1, state.getFileTable().toMap().size());
    }

    @Test
    void missingFileThrowsAndAddsNothing() {
        IStmt missing = new OpenRFileStmt(str(dir.resolve("missing.in").toString()));
        var ex = assertThrows(StatementException.class, () -> missing.execute(state));
        assertTrue(ex.getMessage().startsWith("openRFile: cannot open"));
        assertTrue(state.getFileTable().toMap().isEmpty());
    }

    @Test
    void aDirectoryCannotBeOpenedAsAFile() {
        IStmt directory = new OpenRFileStmt(str(dir.toString()));
        assertThrows(StatementException.class, () -> directory.execute(state));
        assertTrue(state.getFileTable().toMap().isEmpty());
    }

    @Test
    void errorOfTheExpressionIsPropagated() {
        assertThrows(ExpressionException.class, () -> new OpenRFileStmt(new VarExp("undefined")).execute(state));
    }

    @Test
    void twoDifferentFilesCanBeOpenAtTheSameTime() throws Exception {
        Path other = dir.resolve("other.in");
        Files.writeString(other, "1\n");
        open.execute(state);
        new OpenRFileStmt(str(other.toString())).execute(state);
        assertEquals(2, state.getFileTable().toMap().size());
    }

    @Test
    void toStringShowsTheCall() {
        assertEquals("openRFile(varf)", new OpenRFileStmt(new VarExp("varf")).toString());
    }
}
