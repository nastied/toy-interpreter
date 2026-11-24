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
        assertEquals("ExeStack: {nop}\nSymTable: {v -> 2}\nOut: {1}\nFileTable: {}\nHeap: {}", state.toString());
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
        assertTrue(state.toString().contains("FileTable: {\"test.in\" -> reader}"));
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
        state.getHeap().allocate(new IntValue(20));
        state.getHeap().allocate(new interpreter.model.value.RefValue(1, new interpreter.model.type.IntType()));
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
                Heap:
                1 --> 20
                2 --> (1, int)
                """, state.toLogString());
    }

    @Test
    void toLogStringOfAnEmptyStateHasEmptySections() throws MyException {
        PrgState state = PrgState.of(new NopStmt());
        state.getExeStack().pop();
        assertEquals("ExeStack:\nSymTable:\nOut:\nFileTable:\nHeap:\n", state.toLogString());
    }

    @Test
    void ofCreatesAnEmptyHeap() {
        PrgState state = PrgState.of(program);
        assertNotNull(state.getHeap());
        assertTrue(state.getHeap().getContent().isEmpty());
    }

    @Test
    void everyStateHasItsOwnHeap() {
        PrgState a = PrgState.of(program);
        PrgState b = PrgState.of(program);
        a.getHeap().allocate(new IntValue(1));
        assertTrue(b.getHeap().getContent().isEmpty());
    }

    @Test
    void setHeapReplacesTheHeap() {
        PrgState state = PrgState.of(program);
        var other = new interpreter.model.adt.MyHeap();
        state.setHeap(other);
        assertSame(other, state.getHeap());
    }

    @Test
    void toStringShowsTheHeapInTheLabNotation() {
        PrgState state = PrgState.of(new NopStmt());
        state.getHeap().allocate(new IntValue(20));
        assertTrue(state.toString().endsWith("Heap: {1 -> 20}"));
    }

    @Test
    void toLogStringListsTheHeapSortedByNumericAddress() {
        PrgState state = PrgState.of(new NopStmt());
        state.getHeap().setContent(java.util.Map.of(10, new IntValue(10), 2, new IntValue(2), 1, new IntValue(1)));
        String log = state.toLogString();
        assertTrue(log.endsWith("Heap:\n1 --> 1\n2 --> 2\n10 --> 10\n")); // 2 comes before 10
    }

    @Test
    void theHeapSectionComesAfterTheFileTableSection() {
        String log = PrgState.of(new NopStmt()).toLogString();
        assertTrue(log.indexOf("FileTable:") < log.indexOf("Heap:"));
    }
}
