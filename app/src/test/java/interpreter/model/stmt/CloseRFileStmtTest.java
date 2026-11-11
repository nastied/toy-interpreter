package interpreter.model.stmt;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.IExp;
import interpreter.model.exp.VarExp;
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

class CloseRFileStmtTest {
    @TempDir
    Path dir;

    private PrgState state;
    private IExp file;
    private StringValue fileName;

    @BeforeEach
    void setUp() throws IOException {
        Path f = dir.resolve("numbers.in");
        Files.writeString(f, "7\n");
        fileName = new StringValue(f.toString());
        file = str(f.toString());
        state = emptyState();
    }

    @AfterEach
    void closeFiles() throws IOException {
        closeOpenFiles(state);
    }

    @Test
    void removesTheFileFromTheFileTable() throws MyException {
        new OpenRFileStmt(file).execute(state);
        new CloseRFileStmt(file).execute(state);
        assertFalse(state.getFileTable().isDefined(fileName));
        assertTrue(state.getFileTable().toMap().isEmpty());
    }

    @Test
    void closesTheReader() throws MyException {
        new OpenRFileStmt(file).execute(state);
        BufferedReader reader = state.getFileTable().lookup(fileName);
        new CloseRFileStmt(file).execute(state);
        assertThrows(IOException.class, reader::readLine); // "Stream closed"
    }

    @Test
    void onlyTheClosedFileIsRemoved() throws Exception {
        Path other = dir.resolve("other.in");
        Files.writeString(other, "1\n");
        new OpenRFileStmt(file).execute(state);
        new OpenRFileStmt(str(other.toString())).execute(state);
        new CloseRFileStmt(file).execute(state);
        assertEquals(1, state.getFileTable().toMap().size());
        assertTrue(state.getFileTable().isDefined(new StringValue(other.toString())));
    }

    @Test
    void aClosedFileCanBeOpenedAgain() throws MyException {
        new OpenRFileStmt(file).execute(state);
        new CloseRFileStmt(file).execute(state);
        new OpenRFileStmt(file).execute(state);
        assertTrue(state.getFileTable().isDefined(fileName));
    }

    @Test
    void closingAFileThatIsNotOpenThrows() {
        var ex = assertThrows(StatementException.class, () -> new CloseRFileStmt(file).execute(state));
        assertTrue(ex.getMessage().contains("is not open"));
    }

    @Test
    void closingTheSameFileTwiceThrows() throws MyException {
        new OpenRFileStmt(file).execute(state);
        new CloseRFileStmt(file).execute(state);
        assertThrows(StatementException.class, () -> new CloseRFileStmt(file).execute(state));
    }

    @Test
    void nonStringExpressionThrows() {
        var ex = assertThrows(StatementException.class, () -> new CloseRFileStmt(num(1)).execute(state));
        assertEquals("closeRFile: 1 is not a string", ex.getMessage());
    }

    @Test
    void errorOfTheExpressionIsPropagated() {
        assertThrows(ExpressionException.class, () -> new CloseRFileStmt(new VarExp("undefined")).execute(state));
    }

    @Test
    void toStringShowsTheCall() {
        assertEquals("closeRFile(varf)", new CloseRFileStmt(new VarExp("varf")).toString());
    }
}
