package interpreter.repository;

import interpreter.model.PrgState;
import org.junit.jupiter.api.Test;

import static interpreter.TestSupport.emptyState;
import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {
    @Test
    void getCrtPrgReturnsTheStateGivenToTheConstructor() {
        PrgState state = emptyState();
        IRepository repo = new Repository(state);
        assertSame(state, repo.getCrtPrg());
    }

    @Test
    void getCrtPrgReturnsTheSameStateEveryTime() {
        IRepository repo = new Repository(emptyState());
        assertSame(repo.getCrtPrg(), repo.getCrtPrg());
    }
}
