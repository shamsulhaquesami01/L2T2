import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunner {

    public static void main(String[] args) {

        System.out.println("Starting JUnit tests...");
        System.out.println();

        Result result = JUnitCore.runClasses(AllTests.class);

        System.out.println();
        System.out.println("========== RESULTS ==========");

        for (Failure failure : result.getFailures()) {
            System.out.println("FAILED:");
            System.out.println(failure.toString());
        }

        System.out.println("Tests run: " + result.getRunCount());
        System.out.println("Failures: " + result.getFailureCount());
        System.out.println("Successful: " + result.wasSuccessful());
    }
}