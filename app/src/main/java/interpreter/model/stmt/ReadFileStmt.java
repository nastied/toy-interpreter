package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.IExp;
import interpreter.model.value.IntValue;
import interpreter.model.value.StringValue;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Rule S9: readFile(Exp, Id) -> read a line with the reader of Eval(Exp); n = 0 at the end of the file,
 * otherwise n = the integer on the line; update Id -> n in the SymTable.
 */
public record ReadFileStmt(IExp exp, String varName) implements IStmt {
    @Override
    public PrgState execute(PrgState state) throws MyException {
        var symTable = state.getSymTable();
        if (!symTable.isDefined(varName))
            throw new StatementException("readFile: variable " + varName + " was not declared");
        if (!(symTable.lookup(varName) instanceof IntValue))
            throw new StatementException("readFile: variable " + varName + " is not of type int");
        if (!(exp.eval(symTable) instanceof StringValue fileName))
            throw new StatementException("readFile: " + exp + " is not a string");
        var fileTable = state.getFileTable();
        if (!fileTable.isDefined(fileName))
            throw new StatementException("readFile: " + fileName + " is not open");
        BufferedReader reader = fileTable.lookup(fileName);
        String line = null;
        try {
            line = reader.readLine();
            int n = (line == null) ? 0 : Integer.parseInt(line.strip());
            symTable.update(varName, new IntValue(n));
        } catch (IOException e) {
            throw new StatementException("readFile: cannot read from " + fileName + ": " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new StatementException("readFile: the line \"" + line + "\" of " + fileName
                    + " is not an integer");
        }
        return state;
    }

    @Override
    public String toString() {
        return "readFile(" + exp + ", " + varName + ")";
    }
}
