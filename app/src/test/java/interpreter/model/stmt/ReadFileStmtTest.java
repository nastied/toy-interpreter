package interpreter.model.stmt;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.IExp;
import interpreter.model.exp.VarExp;
import interpreter.model.type.BoolType;
import interpreter.model.type.IntType;
import interpreter.model.type.StringType;
import interpreter.model.value.IntValue;
import interpreter.model.value.StringValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static interpreter.TestSupport.closeOpenFiles;
import static interpreter.TestSupport.emptyState;
import static interpreter.TestSupport.num;
import static interpreter.TestSupport.str;
import static org.junit.jupiter.api.Assertions.*;

class ReadFileStmtTest {
    @TempDir
    Path dir;

    private PrgState state;
    private IExp file; // the expression "<dir>/numbers.in"
    private StringValue fileName;

    @BeforeEach
    void setUp() throws MyException {
        state = emptyState();
        fileName = new StringValue(dir.resolve("numbers.in").toString());
        file = str(fileName.val());
        new VarDeclStmt("x", new IntType()).execute(state);
    }

    @AfterEach
    void closeFiles() throws IOException {
        closeOpenFiles(state);
    }

    /** Writes the content of numbers.in and opens the file in the state. */
    private void openWith(String content) throws IOException, MyException {
        Files.writeString(dir.resolve("numbers.in"), content);
        new OpenRFileStmt(file).execute(state);
    }

    private IntValue x() throws MyException {
        return (IntValue) state.getSymTable().lookup("x");
    }

    @Test
    void readGivesTheNextNumberThenZeroAtTheEnd() throws Exception {
        openWith("7\n");
        new ReadFileStmt(file, "x").execute(state);
        assertEquals(new IntValue(7), x());
        new ReadFileStmt(file, "x").execute(state);
        assertEquals(new IntValue(0), x());
    }

    @Test
    void readsSeveralNumbersInOrder() throws Exception {
        openWith("15\n50\n3\n");
        var read = new ReadFileStmt(file, "x");
        read.execute(state);
        assertEquals(new IntValue(15), x());
        read.execute(state);
        assertEquals(new IntValue(50), x());
        read.execute(state);
        assertEquals(new IntValue(3), x());
        read.execute(state);
        assertEquals(new IntValue(0), x());
    }

    @Test
    void anEmptyFileGivesZero() throws Exception {
        openWith("");
        new AssignStmt("x", num(99)).execute(state);
        new ReadFileStmt(file, "x").execute(state);
        assertEquals(new IntValue(0), x());
    }

    @Test
    void zeroAtTheEndOfTheFileCanBeReadRepeatedly() throws Exception {
        openWith("");
        var read = new ReadFileStmt(file, "x");
        read.execute(state);
        read.execute(state);
        assertEquals(new IntValue(0), x());
    }

    @Test
    void spacesAroundTheNumberAreIgnored() throws Exception {
        openWith("  42 \n");
        new ReadFileStmt(file, "x").execute(state);
        assertEquals(new IntValue(42), x());
    }

    @Test
    void windowsLineEndingsAreAccepted() throws Exception {
        openWith("15\r\n50\r\n");
        var read = new ReadFileStmt(file, "x");
        read.execute(state);
        assertEquals(new IntValue(15), x());
        read.execute(state);
        assertEquals(new IntValue(50), x());
    }

    @Test
    void theFileNameCanComeFromAVariable() throws Exception {
        openWith("5\n");
        new VarDeclStmt("f", new StringType()).execute(state);
        new AssignStmt("f", file).execute(state);
        new ReadFileStmt(new VarExp("f"), "x").execute(state);
        assertEquals(new IntValue(5), x());
    }

    @Test
    void undeclaredVariableThrows() throws Exception {
        openWith("1\n");
        var ex = assertThrows(StatementException.class, () -> new ReadFileStmt(file, "y").execute(state));
        assertEquals("readFile: variable y was not declared", ex.getMessage());
    }

    @Test
    void booleanVariableThrows() throws Exception {
        openWith("1\n");
        new VarDeclStmt("b", new BoolType()).execute(state);
        var ex = assertThrows(StatementException.class, () -> new ReadFileStmt(file, "b").execute(state));
        assertEquals("readFile: variable b is not of type int", ex.getMessage());
    }

    @Test
    void stringVariableThrows() throws Exception {
        openWith("1\n");
        new VarDeclStmt("s", new StringType()).execute(state);
        assertThrows(StatementException.class, () -> new ReadFileStmt(file, "s").execute(state));
    }

    @Test
    void nonStringFileExpressionThrows() {
        var ex = assertThrows(StatementException.class, () -> new ReadFileStmt(num(1), "x").execute(state));
        assertEquals("readFile: 1 is not a string", ex.getMessage());
    }

    @Test
    void fileThatIsNotOpenThrows() {
        var ex = assertThrows(StatementException.class, () -> new ReadFileStmt(file, "x").execute(state));
        assertTrue(ex.getMessage().contains("is not open"));
    }

    @Test
    void nonIntegerLineThrowsAndLeavesTheVariableUnchanged() throws Exception {
        openWith("abc\n");
        new AssignStmt("x", num(3)).execute(state);
        var ex = assertThrows(StatementException.class, () -> new ReadFileStmt(file, "x").execute(state));
        assertTrue(ex.getMessage().contains("is not an integer"));
        assertEquals(new IntValue(3), x());
    }

    @Test
    void decimalNumberIsNotAnInteger() throws Exception {
        openWith("1.5\n");
        assertThrows(StatementException.class, () -> new ReadFileStmt(file, "x").execute(state));
    }

    @Test
    void emptyLineIsNotAnInteger() throws Exception {
        openWith("\n7\n");
        assertThrows(StatementException.class, () -> new ReadFileStmt(file, "x").execute(state));
    }

    @Test
    void numberTooLargeForAnIntThrowsAStatementException() throws Exception {
        openWith("99999999999\n");
        assertThrows(StatementException.class, () -> new ReadFileStmt(file, "x").execute(state));
    }

    @Test
    void ioErrorOfTheReaderIsReportedAsAStatementException() throws Exception {
        openWith("1\n");
        state.getFileTable().lookup(fileName).close(); // the next read fails with "Stream closed"
        var ex = assertThrows(StatementException.class, () -> new ReadFileStmt(file, "x").execute(state));
        assertTrue(ex.getMessage().startsWith("readFile: cannot read from"));
    }

    @Test
    void errorOfTheFileExpressionIsPropagated() {
        assertThrows(ExpressionException.class, () -> new ReadFileStmt(new VarExp("undefined"), "x").execute(state));
    }

    @Test
    void toStringShowsTheCall() {
        assertEquals("readFile(varf, varc)", new ReadFileStmt(new VarExp("varf"), "varc").toString());
    }
}
