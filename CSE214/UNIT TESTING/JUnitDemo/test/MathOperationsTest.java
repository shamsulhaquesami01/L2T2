import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MathOperationsTest {

    private MathOperations math;

    @Before
    public void setUp() {
        System.out.println("MathOperationsTest: @Before");

        math = new MathOperations();
    }

    @Test
    public void testAdd() {
        System.out.println("Running testAdd");

        int actual = math.add(2, 3);

        assertEquals(5, actual);
    }

    @Test
    public void testSubtract() {
        System.out.println("Running testSubtract");

        int actual = math.subtract(10, 4);

        assertEquals(6, actual);
    }

    @Test
    public void testMultiply() {
        System.out.println("Running testMultiply");

        int actual = math.multiply(4, 5);

        assertEquals(20, actual);
    }

    @After
    public void tearDown() {
        System.out.println("MathOperationsTest: @After");
        System.out.println();

        math = null;
    }
}