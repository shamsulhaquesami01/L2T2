package practise;
public class SingletonTemplate {

    public static void main(String[] args) {

        System.out.println("========== PART 1 : SHARED ResultCache ==========");

        // Two INDEPENDENT parts of the application ask for the ResultCache.
        // Neither of them uses `new` - they cannot, the constructor is private.
        ModuleA moduleA = new ModuleA();
        moduleA.doWork();

        ModuleB moduleB = new ModuleB();
        moduleB.doWork();

        // ---- PROOF that both modules received the SAME object --------------
        ResultCache l1 = ResultCache.getInstance();
        ResultCache l2 = ResultCache.getInstance();
        System.out.println();
        System.out.println("l1 hashCode : " + l1.hashCode());
        System.out.println("l2 hashCode : " + l2.hashCode());
        System.out.println("l1 == l2    : " + (l1 == l2));          // must be true
        System.out.println("Entries in the single shared log: " + l1.getCount());

        // ResultCache l3 = new ResultCache();  // <-- WILL NOT COMPILE. That is the point.

    }
}

/* ===========================================================================
 * THE SINGLETON  (lazy initialisation - the default answer for this course)
 * ======================================================================== */
class ResultCache {

    // 1. The one and only instance, held in a STATIC field.
    private static ResultCache instance;

    // Any normal state the object needs.
    private int count = 0;

    // 2. PRIVATE constructor -> no other class can ever write `new ResultCache()`.
    private ResultCache() {
        System.out.println("[ResultCache] >> instance created (prints exactly ONCE)");
    }

    // 3. The global access point. Creates the object the first time it is
    //    requested, returns the existing one on every later call.
    public static ResultCache getInstance() {
        if (instance == null) {          // first call only
            instance = new ResultCache();
        }
        return instance;                 // every later call lands here
    }

    // ---- normal business methods ------------------------------------------
    public void log(String message) {
        count++;
        System.out.println("[LOG " + count + "] " + message);
    }

    public int getCount() {
        return count;
    }
}

/* ---- Two independent "clients" / "modules" -------------------------------
 * The question almost always asks you to DEMONSTRATE access from two places.
 * Rename these to StudentLogin / QuestionManagement / ResultProcessing,
 * or DepositService / WithdrawalService, etc.
 * ------------------------------------------------------------------------ */
class ModuleA {
    public void doWork() {
        ResultCache.getInstance().log("ModuleA: user logged in");
    }
}

class ModuleB {
    public void doWork() {
        ResultCache.getInstance().log("ModuleB: transaction completed");
    }
}
