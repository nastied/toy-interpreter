package interpreter.view;

import interpreter.controller.Controller;
import interpreter.exceptions.MyException;

import java.io.PrintStream;

/** Runs one example program completely; an example runs only once. */
public class RunExampleCommand extends Command {
    private final Controller controller;
    private final PrintStream out;
    private boolean executed = false;

    public RunExampleCommand(String key, String description, Controller controller) {
        this(key, description, controller, System.out);
    }

    public RunExampleCommand(String key, String description, Controller controller, PrintStream out) {
        super(key, description);
        this.controller = controller;
        this.out = out;
    }

    @Override
    public void execute() {
        if (executed) {
            out.println("This example has already been executed.");
            return;
        }
        executed = true;
        try {
            controller.allStep();
        } catch (MyException e) { // errors the controller cannot solve
            out.println("Error: " + e.getMessage());
        }
    }
}
