package interpreter;

import interpreter.view.Examples;
import interpreter.view.TextMenu;

public class Main {
    public static void main(String[] args) {
        new TextMenu(Examples.all(), System.in, System.out).run();
    }
}
