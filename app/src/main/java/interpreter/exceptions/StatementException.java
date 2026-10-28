package interpreter.exceptions;

/**
 * Error while executing a statement: empty execution stack, variable declared twice or not declared,
 * type mismatch in an assignment, non-boolean condition.
 */
public class StatementException extends MyException {
    private static final long serialVersionUID = 1L;

    public StatementException(String message) {
        super(message);
    }
}
