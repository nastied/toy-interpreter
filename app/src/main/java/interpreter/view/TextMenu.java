package interpreter.view;

import interpreter.controller.Controller;
import interpreter.exceptions.MyException;
import interpreter.model.PrgState;
import interpreter.model.adt.MyDictionary;
import interpreter.model.adt.MyList;
import interpreter.model.adt.MyStack;
import interpreter.model.stmt.IStmt;
import interpreter.repository.Repository;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/** Text menu: select one of the programs, run the selected program completely. Errors never crash it. */
public class TextMenu {
    private final List<IStmt> programs;
    private final Scanner in;
    private final PrintStream out;
    private int selected = -1; // index into programs; -1 = nothing selected yet

    public TextMenu(List<IStmt> programs, InputStream input, PrintStream out) {
        this.programs = programs;
        this.in = new Scanner(input);
        this.out = out;
    }

    public void run() {
        while (true) {
            printMenu();
            String line = readLine();
            if (line == null) return; // end of input
            switch (line.trim()) {
                case "1" -> selectProgram();
                case "2" -> runSelectedProgram();
                case "0" -> {
                    out.println("Goodbye.");
                    return;
                }
                default -> out.println("Invalid option: \"" + line.trim() + "\".");
            }
        }
    }

    private void printMenu() {
        out.println();
        out.println("=== Toy-language interpreter ===");
        out.println("1. Select a program" + (selected >= 0 ? " (selected: " + (selected + 1) + ")" : ""));
        out.println("2. Run the selected program");
        out.println("0. Exit");
        out.print("Option: ");
        out.flush();
    }

    private void selectProgram() {
        out.println();
        for (int i = 0; i < programs.size(); i++)
            out.println((i + 1) + ". " + programs.get(i));
        out.print("Program number: ");
        out.flush();
        String line = readLine();
        if (line == null) return;
        try {
            int n = Integer.parseInt(line.trim());
            if (n < 1 || n > programs.size()) {
                out.println("There is no program number " + n + ".");
                return;
            }
            selected = n - 1;
            out.println("Selected program " + n + ".");
        } catch (NumberFormatException e) {
            out.println("Not a number: \"" + line.trim() + "\".");
        }
    }

    private void runSelectedProgram() {
        if (selected < 0) {
            out.println("No program selected. Choose option 1 first.");
            return;
        }
        PrgState state = new PrgState(new MyStack<>(), new MyDictionary<>(), new MyList<>(),
                programs.get(selected));
        Controller controller = new Controller(new Repository(state), true, out);
        out.println();
        try {
            controller.allStep();
            out.println("Execution finished. Out: " + state.getOut());
        } catch (MyException e) {
            out.println("Error: " + e.getMessage());
        }
    }

    /** Next input line, or null at the end of the input. */
    private String readLine() {
        return in.hasNextLine() ? in.nextLine() : null;
    }
}
