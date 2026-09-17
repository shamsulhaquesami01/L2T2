import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CalculatorTest {

    private Calculator calculator;

    @Before
    public void setUp() {
        System.out.println("CalculatorTest: @Before");

        calculator = new Calculator();
    }

    @Test
    public void testEvenNumber() {
        System.out.println("Running testEvenNumber");

        boolean actual = calculator.isEven(8);

        assertTrue(actual);
    }

    @Test
    public void testOddNumber() {
        System.out.println("Running testOddNumber");

        boolean actual = calculator.isEven(7);

        assertFalse(actual);
    }

    @Test
    public void testSquare() {
        System.out.println("Running testSquare");

        int actual = calculator.square(5);

        assertEquals(35, actual);
    }

    @After
    public void tearDown() {
        System.out.println("CalculatorTest: @After");
        System.out.println();

        calculator = null;
    }
}