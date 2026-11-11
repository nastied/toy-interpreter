package interpreter.model;

import java.io.StringReader;
import java.io.BufferedReader;
import interpreter.model.stmt.CompStmt;
import interpreter.model.value.StringValue;
import interpreter.model.value.BoolValue;
import interpreter.exceptions.MyException;
import interpreter.model.adt.IStack;
import interpreter.model.adt.MyDictionary;
import interpreter.model.adt.MyList;
import interpreter.model.adt.MyStack;
import interpreter.model.stmt.IStmt;
import interpreter.model.stmt.NopStmt;
import interpreter.model.stmt.PrintStmt;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.num;
import static org.junit.jupiter.api.Assertions.*;

class PrgStateTest {
    private final IStmt program = new PrintStmt(num(1));

    private PrgState newState() {
        return PrgState.of(program);
    }

    @Test
    void theConstructorPushesTheProgramOnTheExecutionStack() throws MyException {
        PrgState state = newState();
        assertSame(program, state.getExeStack().pop());
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void symTableAndOutAreInitiallyEmpty() {
        PrgState state = newState();
        assertEquals("{}", state.getSymTable().toString());
        assertEquals(0, state.getOut().size());
    }

    @Test
    void keepsTheOriginalProgram() throws MyException {
        PrgState state = newState();
        state.getExeStack().pop();
        assertSame(program, state.getOriginalProgram());
    }

    @Test
    void settersReplaceTheStructures() throws MyException {
        PrgState state = newState();
        IStack<IStmt> otherStack = new MyStack<>();
        state.setExeStack(otherStack);
        state.getOut().add(new IntValue(1));
        state.setOut(new MyList<>());
        state.setSymTable(new MyDictionary<>());
        assertSame(otherStack, state.getExeStack());
        assertEquals(0, state.getOut().size());
    }

    @Test
    void toStringShowsTheThreeStructures() throws MyException {
        PrgState state = PrgState.of(new NopStmt());
        state.getSymTable().put("v", new IntValue(2));
        state.getOut().add(new IntValue(1));
        assertEquals("ExeStack: {nop}\nSymTable: {v -> 2}\nOut: {1}\nFileTable: {}", state.toString());
    }

    @Test
    void ofCreatesEmptyStructuresAndPushesTheProgram() throws MyException {
        PrgState state = PrgState.of(program);
        assertSame(program, state.getExeStack().pop());
        assertTrue(state.getExeStack().isEmpty());
        assertEquals(0, state.getOut().size());
        assertTrue(state.getSymTable().toMap().isEmpty());
        assertTrue(state.getFileTable().toMap().isEmpty());
    }

    @Test
    void ofCreatesIndependentStates() {
        PrgState a = PrgState.of(program);
        PrgState b = PrgState.of(program);
        a.getOut().add(new IntValue(1));
        assertEquals(0, b.getOut().size());
    }

    @Test
    void setFileTableReplacesTheFileTable() {
        PrgState state = PrgState.of(program);
        var other = new MyDictionary<StringValue, BufferedReader>();
        state.setFileTable(other);
        assertSame(other, state.getFileTable());
    }

    @Test
    void toStringShowsTheOpenFilesInTheLabNotation() {
        PrgState state = PrgState.of(new NopStmt());
        state.getFileTable().put(new StringValue("test.in"), new BufferedReader(new StringReader("")));
        assertTrue(state.toString().endsWith("FileTable: {\"test.in\" -> reader}"));
    }

    @Test
    void toLogStringHasTheFourSectionsWithOneElementPerLine() throws MyException {
        PrgState state = PrgState.of(new CompStmt(new NopStmt(), new PrintStmt(num(1))));
        state.getExeStack().pop();
        state.getExeStack().push(new PrintStmt(num(1)));
        state.getExeStack().push(new NopStmt());
        state.getSymTable().put("v", new IntValue(2));
        state.getSymTable().put("w", new StringValue("hi"));
        state.getOut().add(new IntValue(1));
        state.getOut().add(new BoolValue(true));
        state.getFileTable().put(new StringValue("test.in"), new BufferedReader(new StringReader("")));
        assertEquals("""
                ExeStack:
                nop
                print(1)
                SymTable:
                v --> 2
                w --> "hi"
                Out:
                1
                true
                FileTable:
                test.in
                """, state.toLogString());
    }

    @Test
    void toLogStringOfAnEmptyStateHasEmptySections() throws MyException {
        PrgState state = PrgState.of(new NopStmt());
        state.getExeStack().pop();
        assertEquals("ExeStack:\nSymTable:\nOut:\nFileTable:\n", state.toLogString());
    }
}
