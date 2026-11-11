package interpreter;

import interpreter.exceptions.ADTException;
import interpreter.model.PrgState;
import interpreter.model.exp.IExp;
import interpreter.model.exp.ValueExp;
import interpreter.model.stmt.NopStmt;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import interpreter.model.value.StringValue;

import java.io.BufferedReader;
import java.io.IOException;

/** Small helpers shared by the tests. */
public final class TestSupport {
    private TestSupport() {
    }

    /** A program state whose execution stack, symbol table, output and file table are all empty. */
    public static PrgState emptyState() {
        PrgState state = PrgState.of(new NopStmt());
        try {
            state.getExeStack().pop(); // remove the program pushed by the factory
        } catch (ADTException e) {
            throw new IllegalStateException(e);
        }
        return state;
    }

    public static IExp num(int n) {
        return new ValueExp(new IntValue(n));
    }

    public static IExp bool(boolean b) {
        return new ValueExp(new BoolValue(b));
    }

    public static IExp str(String s) {
        return new ValueExp(new StringValue(s));
    }

    /** Closes the files a test left open, so that @TempDir can delete them (needed on Windows). */
    public static void closeOpenFiles(PrgState state) throws IOException {
        for (BufferedReader reader : state.getFileTable().toMap().values()) reader.close();
    }
}
