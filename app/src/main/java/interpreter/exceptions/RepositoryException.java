package interpreter.exceptions;

/** Error of the repository, e.g. the log file cannot be written. */
public class RepositoryException extends MyException {
    private static final long serialVersionUID = 1L;

    public RepositoryException(String message) {
        super(message);
    }
}
