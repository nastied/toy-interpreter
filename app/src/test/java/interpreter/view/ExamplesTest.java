package interpreter.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExamplesTest {
    @Test
    void thereAreTenExamplesInOrder() {
        assertEquals(10, Examples.all().size());
        assertSame(Examples.EX1, Examples.all().get(0));
        assertSame(Examples.EX2, Examples.all().get(1));
        assertSame(Examples.EX3, Examples.all().get(2));
        assertSame(Examples.EX4, Examples.all().get(3));
        assertSame(Examples.EX5, Examples.all().get(4));
        assertSame(Examples.EX6, Examples.all().get(5));
        assertSame(Examples.EX7, Examples.all().get(6));
        assertSame(Examples.EX8, Examples.all().get(7));
        assertSame(Examples.EX9, Examples.all().get(8));
        assertSame(Examples.EX10, Examples.all().get(9));
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

    @Test
    void example6IsDisplayedInInfixForm() {
        assertEquals("(Ref(int) v; (new(v, 20); (Ref(Ref(int)) a; (new(a, v); (print(v); print(a)"
                + ")".repeat(5), Examples.EX6.toString());
    }

    @Test
    void example7IsDisplayedInInfixForm() {
        assertEquals("(Ref(int) v; (new(v, 20); (Ref(Ref(int)) a; (new(a, v); "
                + "(print(rH(v)); print((rH(rH(a)) + 5))" + ")".repeat(5), Examples.EX7.toString());
    }

    @Test
    void example8IsDisplayedInInfixForm() {
        assertEquals("(Ref(int) v; (new(v, 20); (print(rH(v)); (wH(v, 30); print((rH(v) + 5))"
                + ")".repeat(4), Examples.EX8.toString());
    }

    @Test
    void example9IsDisplayedInInfixForm() {
        assertEquals("(Ref(int) v; (new(v, 20); (Ref(Ref(int)) a; (new(a, v); (new(v, 30); print(rH(rH(a)))"
                + ")".repeat(5), Examples.EX9.toString());
    }

    @Test
    void example10IsDisplayedInInfixForm() {
        assertEquals("(int v; (v = 4; ((while ((v > 0)) (print(v); v = (v - 1))); print(v))))",
                Examples.EX10.toString());
    }
}
