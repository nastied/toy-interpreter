package interpreter.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExamplesTest {
    @Test
    void thereAreThreeExamples() {
        assertEquals(3, Examples.all().size());
    }

    @Test
    void examplesAreListedInOrder() {
        assertSame(Examples.EX1, Examples.all().get(0));
        assertSame(Examples.EX2, Examples.all().get(1));
        assertSame(Examples.EX3, Examples.all().get(2));
    }

    @Test
    void example1IsDisplayedAsInTheLabDocument() {
        assertEquals("int v; (v = 2; print(v))", Examples.EX1.toString());
    }

    @Test
    void example3ShowsTheIfStatementInParentheses() {
        assertEquals("bool a; (int v; (a = true; ((if a then v = 2 else v = 3); print(v))))",
                Examples.EX3.toString());
    }
}
