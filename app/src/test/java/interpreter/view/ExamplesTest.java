package interpreter.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExamplesTest {
    @Test
    void thereAreFiveExamplesInOrder() {
        assertEquals(5, Examples.all().size());
        assertSame(Examples.EX1, Examples.all().get(0));
        assertSame(Examples.EX2, Examples.all().get(1));
        assertSame(Examples.EX3, Examples.all().get(2));
        assertSame(Examples.EX4, Examples.all().get(3));
        assertSame(Examples.EX5, Examples.all().get(4));
    }

    // the strings below are the menu lines of the Lab 5 document (section 2.7)

    @Test
    void example1IsDisplayedInInfixForm() {
        assertEquals("(int v; (v = 2; print(v)))", Examples.EX1.toString());
    }

    @Test
    void example2IsDisplayedInInfixForm() {
        assertEquals("(int a; (int b; (a = (2 + (3 * 5)); (b = (a + 1); print(b)))))", Examples.EX2.toString());
    }

    @Test
    void example3ShowsTheIfStatementWithParentheses() {
        assertEquals("(bool a; (int v; (a = true; ((if (a) then (v = 2) else (v = 3)); print(v)))))",
                Examples.EX3.toString());
    }

    @Test
    void example4IsDisplayedInInfixForm() {
        assertEquals("(string varf; (varf = \"test.in\"; (openRFile(varf); (int varc; "
                        + "(readFile(varf, varc); (print(varc); (readFile(varf, varc); "
                        + "(print(varc); closeRFile(varf))" + ")".repeat(7),
                Examples.EX4.toString());
    }

    @Test
    void example5IsDisplayedInInfixForm() {
        assertEquals("(int a; (int b; (a = 5; (b = 7; (if ((a < b)) then (print(a)) else (print(b)))))))",
                Examples.EX5.toString());
    }

    @Test
    void example4readsTestInFromTheWorkingDirectory() {
        assertEquals(Examples.fileExample("test.in"), Examples.EX4);
    }

    @Test
    void theFileExampleTakesTheFileNameAsAParameter() {
        assertTrue(Examples.fileExample("data.txt").toString().contains("\"data.txt\""));
        assertNotEquals(Examples.fileExample("a.in"), Examples.fileExample("b.in"));
    }
}
