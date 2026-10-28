package interpreter.exceptions;

/** Base class of all the errors raised by the interpreter (checked exception). */
public class MyException extends Exception {
    private static final long serialVersionUID = 1L;

    public MyException(String message) {
        super(message);
    }
}
