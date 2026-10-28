package interpreter.model.stmt;

/** Helper for toString: a compound statement nested inside another statement is shown in parentheses. */
final class StmtFormat {
    private StmtFormat() {
    }

    static String nested(IStmt stmt) {
        return stmt instanceof CompStmt ? "(" + stmt + ")" : String.valueOf(stmt);
    }
}
