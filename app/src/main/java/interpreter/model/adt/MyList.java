package interpreter.model.adt;

import interpreter.exceptions.ADTException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyList<T> implements IList<T> {
    private final List<T> list = new ArrayList<>();

    @Override
    public void add(T v) {
        list.add(v);
    }

    @Override
    public T get(int index) throws ADTException {
        if (index < 0 || index >= list.size())
            throw new ADTException("index " + index + " is out of bounds for a list of size " + list.size());
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    /** As in the lab notation: {1, 2}. */
    @Override
    public String toString() {
        return list.stream().map(String::valueOf).collect(Collectors.joining(", ", "{", "}"));
    }
}
