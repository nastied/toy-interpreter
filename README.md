# Toy-language interpreter (Assignment A2)

MVC interpreter for a small toy language: `int`/`bool`, `+ - * /`, `and`/`or`, variables,
`print`, `if`, `nop`, sequencing. Java 25, Gradle (wrapper), JUnit 5.

## Build, test, run

```
./gradlew build     # compiles + runs all tests   (Windows: gradlew.bat build)
./gradlew test      # tests only; HTML report: app/build/reports/tests/test/index.html
./gradlew run       # text menu: 1 = select a program, 2 = run it, 0 = exit
```

## Layout (`app/src/main/java/interpreter`)

| Package | Content |
|---|---|
| `model.type`, `model.value` | `IType`/`IntType`/`BoolType`, `IValue`/`IntValue`/`BoolValue` (records) |
| `model.exp` | `IExp`, `ValueExp`, `VarExp`, `ArithExp` + `ArithOp`, `LogicExp` + `LogicOp` (rules E1-E4) |
| `model.stmt` | `IStmt`, `CompStmt`, `VarDeclStmt`, `AssignStmt`, `PrintStmt`, `IfStmt`, `NopStmt` (rules S1-S6) |
| `model.adt` | `IStack`/`MyStack`, `IDictionary`/`MyDictionary`, `IList`/`MyList` |
| `model` | `PrgState` (ExeStack, SymTable, Out, original program) |
| `repository` | `IRepository`, `Repository` |
| `controller` | `Controller` (`oneStep`, `allStep`, display flag; rule S7) |
| `view` | `TextMenu`, `Examples` (Ex1-Ex3 as object trees) |
| `exceptions` | `MyException` <- `ADTException`, `ExpressionException`, `StatementException` |

Tests mirror the packages under `app/src/test/java/interpreter`.

## Git workflow (first time)

```
git init -b main
git add . && git commit -m "Initial Gradle project for the toy-language interpreter"
git remote add origin <URL of your repository>
git push -u origin main
# ... later, when everything is green:
git tag A2 && git push --tags
```
