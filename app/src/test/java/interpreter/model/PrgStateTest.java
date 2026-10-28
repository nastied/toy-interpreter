package interpreter.model;

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
        return new PrgState(new MyStack<>(), new MyDictionary<>(), new MyList<>(), program);
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
        PrgState state = new PrgState(new MyStack<>(), new MyDictionary<>(), new MyList<>(), new NopStmt());
        state.getSymTable().put("v", new IntValue(2));
        state.getOut().add(new IntValue(1));
        assertEquals("ExeStack: {nop}\nSymTable: {v -> 2}\nOut: {1}", state.toString());
    }
}
