package interpreter.exceptions;

/** Error while evaluating an expression: division by zero, wrong operand type, undefined variable. */
public class ExpressionException extends MyException {
    private static final long serialVersionUID = 1L;

    public ExpressionException(String message) {
        super(message);
    }
}
