package interpreter.exceptions;

/** Invalid ADT operation: pop from an empty stack, lookup of a missing key, index out of bounds ... */
public class ADTException extends MyException {
    private static final long serialVersionUID = 1L;

    public ADTException(String message) {
        super(message);
    }
}
