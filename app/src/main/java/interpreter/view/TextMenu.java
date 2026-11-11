package interpreter.view;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/** A text menu built from commands: adding an option means adding a command, the menu does not change. */
public class TextMenu {
    private final Map<String, Command> commands = new LinkedHashMap<>(); // keeps the order of addCommand
    private final Scanner scanner;
    private final PrintStream out;

    public TextMenu() {
        this(System.in, System.out);
    }

    public TextMenu(InputStream in, PrintStream out) {
        this.scanner = new Scanner(in);
        this.out = out;
    }

    public void addCommand(Command c) {
        commands.put(c.getKey(), c);
    }

    private void printMenu() {
        for (Command c : commands.values())
            out.printf("%4s : %s%n", c.getKey(), c.getDescription());
    }

    /** Shows the menu until an ExitCommand ends the program or the input ends. */
    public void show() {
        while (true) {
            printMenu();
            out.print("Input the option: ");
            out.flush();
            if (!scanner.hasNextLine()) return; // end of input: stop instead of throwing
            Command c = commands.get(scanner.nextLine().strip());
            if (c == null) {
                out.println("Invalid option");
                continue;
            }
            c.execute();
        }
    }
}
