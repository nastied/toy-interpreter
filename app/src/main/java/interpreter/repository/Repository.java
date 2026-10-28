package interpreter.repository;

import interpreter.model.PrgState;

import java.util.ArrayList;
import java.util.List;

public class Repository implements IRepository {
    // a list, because later the repository will hold the states of several threads
    private final List<PrgState> states = new ArrayList<>();

    public Repository(PrgState initialState) {
        states.add(initialState);
    }

    @Override
    public PrgState getCrtPrg() {
        return states.get(0);
    }
}
