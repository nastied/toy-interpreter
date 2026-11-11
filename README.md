# Toy-language interpreter (Assignments A2 + A3)

MVC interpreter for a small toy language: `int` / `bool` / `string`, `+ - * /`, `and` / `or`,
`< <= == != > >=`, variables, `print`, `if`, `nop`, sequencing and the file statements
`openRFile`, `readFile`, `closeRFile`. Java 25, Gradle (wrapper), JUnit 5.

## Build, test, run

```
./gradlew build     # compiles + runs ALL tests (A2 + A3)   (Windows: gradlew.bat build)
./gradlew test      # tests only; HTML report: app/build/reports/tests/test/index.html
./gradlew run       # command menu: 1-5 = run an example (once each), 0 = exit
```

`./gradlew run` starts in the project root (`workingDir = rootDir` in `app/build.gradle.kts`), so
`test.in` (the input of Ex4: the numbers 15 and 50) is found there and the log files
`log1.txt` ... `log5.txt` are written there. They are in `.gitignore`; the log is opened in append
mode, so **delete the log files before a demonstration**.

If you run `Main` from the IDE instead, set the run configuration's working directory to the project
root, otherwise `openRFile` reports that `test.in` does not exist.

## Layout (`app/src/main/java/interpreter`)

| Package | Content |
|---|---|
| `model.type`, `model.value` | `IType`/`IntType`/`BoolType`/`StringType`, `IValue`/`IntValue`/`BoolValue`/`StringValue` (records) |
| `model.exp` | `IExp`, `ValueExp`, `VarExp`, `ArithExp`+`ArithOp`, `LogicExp`+`LogicOp`, `RelationalExp`+`RelOp` (rules E1-E5) |
| `model.stmt` | `IStmt`, `CompStmt`, `VarDeclStmt`, `AssignStmt`, `PrintStmt`, `IfStmt`, `NopStmt`, `OpenRFileStmt`, `ReadFileStmt`, `CloseRFileStmt` (rules S1-S10) |
| `model.adt` | `IStack`/`MyStack`, `IDictionary`/`MyDictionary`, `IList`/`MyList` (with `remove`, `toMap`, `toList`) |
| `model` | `PrgState`: ExeStack, SymTable, Out, FileTable; `PrgState.of(program)`; `toLogString()` |
| `repository` | `IRepository`, `Repository` (`logPrgStateExec` appends the state to the log file) |
| `controller` | `Controller` (`oneStep`, `allStep` logs and displays every state; rule S7) |
| `view` | `Command`, `ExitCommand`, `RunExampleCommand`, `TextMenu`, `Examples` (Ex1-Ex5, `fileExample(name)`) |
| `exceptions` | `MyException` <- `ADTException`, `ExpressionException`, `StatementException`, `RepositoryException` |

Tests mirror the packages under `app/src/test/java/interpreter`. File tests use `@TempDir`, so they never
touch files of the project.

## Git workflow

Commit after every small step that compiles and passes `./gradlew test`. A3 in a sensible order:

1. `Add StringType and StringValue` (+ tests)
2. `Add RelOp and RelationalExp` (+ tests)
3. `Show compound statements and if in infix form with parentheses`
4. `Add FileTable: remove/toMap/toList in the ADTs, PrgState.of`
5. `Add openRFile, readFile and closeRFile` (+ tests with @TempDir)
6. `Add Ex4 and Ex5 and test.in`
7. `Replace the menu by commands: Command, ExitCommand, RunExampleCommand, TextMenu`
8. `Log every program state: IRepository.logPrgStateExec, RepositoryException`

When everything is green: `git tag A3 && git push --tags`.
