package interpreter.model.exp;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.model.adt.IDictionary;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IValue;

/** Rule E4: and / or on two booleans (both operands are always evaluated, left first). */
public record LogicExp(LogicOp op, IExp left, IExp right) implements IExp {
    @Override
    public IValue eval(IDictionary<String, IValue> symTable) throws MyException {
        if (!(left.eval(symTable) instanceof BoolValue(boolean b1)))
            throw new ExpressionException("first operand is not a boolean");
        if (!(right.eval(symTable) instanceof BoolValue(boolean b2)))
            throw new ExpressionException("second operand is not a boolean");
        return switch (op) {
            case AND -> new BoolValue(b1 && b2);
            case OR -> new BoolValue(b1 || b2);
        };
    }

    @Override
    public String toString() {
        return "(" + left + " " + op + " " + right + ")";
    }
}
