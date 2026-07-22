/* ============================================================================
 * CSE 214 - CREATIONAL PATTERNS - TEMPLATE 1 of 4 :  S I N G L E T O N
 * ----------------------------------------------------------------------------
 * RUN IT:  java SingletonTemplate.java        (JDK 11+, no compile step needed)
 * FILE RULE: the file name MUST match the public class name.
 *            If your lab uses "Main", rename BOTH to Main / Main.java.
 * ============================================================================
 *
 * USE THIS TEMPLATE WHEN THE QUESTION SAYS:
 *   "only one instance"        "single shared X"       "global access point"
 *   "must not be created directly"                     "same instance"
 *   "not more than one"        "strictly forbidden to have multiple copies"
 *   "load once and keep in memory"                     "prevent duplicates"
 *
 * THE 3 PARTS A GRADER LOOKS FOR (nothing else earns marks):
 *   1. private static <Type> instance;          <- static field holds the object
 *   2. private <Type>() { ... }                 <- PRIVATE constructor
 *   3. public static <Type> getInstance() {...} <- lazy creation + return
 *
 * 60-SECOND ADAPTATION MAP:
 *   Logger        -> AuditLogger / TransactionLogger / whatever they named it
 *   log(String)   -> the method they asked for (recordActivity, writeEntry, ...)
 *   ModuleA/B     -> StudentLogin / QuestionManagement / ResultProcessing / etc.
 *   AppConfig     -> GameConfig / SettingsManager  (delete if not needed)
 *
 * WHAT TO DELETE BEFORE SUBMITTING: anything you did not use. Extra unused
 * classes never lose marks, but a clean file is faster for the grader to read.
 * ========================================================================== */
package templates_new;
public class SingletonTemplate {

    public static void main(String[] args) {

        System.out.println("========== PART 1 : SHARED LOGGER ==========");

        // Two INDEPENDENT parts of the application ask for the logger.
        // Neither of them uses `new` - they cannot, the constructor is private.
        ModuleA moduleA = new ModuleA();
        moduleA.doWork();

        ModuleB moduleB = new ModuleB();
        moduleB.doWork();

        // ---- PROOF that both modules received the SAME object --------------
        Logger l1 = Logger.getInstance();
        Logger l2 = Logger.getInstance();
        System.out.println();
        System.out.println("l1 hashCode : " + l1.hashCode());
        System.out.println("l2 hashCode : " + l2.hashCode());
        System.out.println("l1 == l2    : " + (l1 == l2));          // must be true
        System.out.println("Entries in the single shared log: " + l1.getCount());

        // Logger l3 = new Logger();  // <-- WILL NOT COMPILE. That is the point.

        System.out.println();
        System.out.println("========== PART 2 : SHARED CONFIGURATION ==========");

        // Same pattern, but the singleton now HOLDS STATE that is expensive to
        // load. Use this variant for GameConfig / SettingsManager questions.
        new GraphicsEngine().start();
        new AudioEngine().start();

        AppConfig c1 = AppConfig.getInstance();
        AppConfig c2 = AppConfig.getInstance();
        System.out.println("c1 == c2 : " + (c1 == c2));             // must be true

        // One module changes a setting -> every other module sees it instantly,
        // because there is only one object in memory.
        c1.setDifficulty("Hard");
        System.out.println("Read back from the other reference: " + c2.getDifficulty());
    }
}

/* ===========================================================================
 * THE SINGLETON  (lazy initialisation - the default answer for this course)
 * ======================================================================== */
class Logger {

    // 1. The one and only instance, held in a STATIC field.
    private static Logger instance;

    // Any normal state the object needs.
    private int count = 0;

    // 2. PRIVATE constructor -> no other class can ever write `new Logger()`.
    private Logger() {
        System.out.println("[Logger] >> instance created (prints exactly ONCE)");
    }

    // 3. The global access point. Creates the object the first time it is
    //    requested, returns the existing one on every later call.
    public static Logger getInstance() {
        if (instance == null) {          // first call only
            instance = new Logger();
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
        Logger.getInstance().log("ModuleA: user logged in");
    }
}

class ModuleB {
    public void doWork() {
        Logger.getInstance().log("ModuleB: transaction completed");
    }
}

/* ===========================================================================
 * SINGLETON THAT CARRIES STATE  (GameConfig / SettingsManager flavour)
 * Delete this whole block if the question is only about a logger.
 * ======================================================================== */
class AppConfig {

    private static AppConfig instance;

    private String resolution = "1920x1080";
    private int    audioVolume = 70;
    private String difficulty  = "Normal";

    private AppConfig() {
        // Pretend this is the expensive disk read the question talks about.
        System.out.println("[AppConfig] >> loading settings from disk (ONCE)");
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public String getResolution()            { return resolution; }
    public int    getAudioVolume()           { return audioVolume; }
    public String getDifficulty()            { return difficulty; }
    public void   setDifficulty(String d)    { this.difficulty = d; }

    public void show() {
        System.out.println("   resolution=" + resolution
                + ", volume=" + audioVolume
                + ", difficulty=" + difficulty);
    }
}

class GraphicsEngine {
    public void start() {
        System.out.println("[Graphics] reading shared config:");
        AppConfig.getInstance().show();
    }
}

class AudioEngine {
    public void start() {
        System.out.println("[Audio] reading shared config:");
        AppConfig.getInstance().show();
    }
}

/* ===========================================================================
 * OPTIONAL VARIANTS - copy the body you need over Logger's body.
 * Only bother if the question explicitly mentions THREADS or SYNCHRONISATION.
 * (Questions that say "thread synchronization is NOT required" want the plain
 *  lazy version above - do not waste your 20 minutes here.)
 * ======================================================================== */

/* (a) EAGER - created at class-load time. Simplest thread-safe version. */
class EagerLogger {
    private static final EagerLogger INSTANCE = new EagerLogger();
    private EagerLogger() { }
    public static EagerLogger getInstance() { return INSTANCE; }
    public void log(String m) { System.out.println("[EAGER] " + m); }
}

/* (b) DOUBLE-CHECKED LOCKING - lazy AND thread-safe. `volatile` is required. */
class ThreadSafeLogger {
    private static volatile ThreadSafeLogger instance;
    private ThreadSafeLogger() { }
    public static ThreadSafeLogger getInstance() {
        if (instance == null) {                      // no lock on the fast path
            synchronized (ThreadSafeLogger.class) {
                if (instance == null) {              // check again inside lock
                    instance = new ThreadSafeLogger();
                }
            }
        }
        return instance;
    }
    public void log(String m) { System.out.println("[SAFE] " + m); }
}

/* (c) ENUM - shortest bullet-proof singleton in Java. Use if you want to show
 *     off; some graders expect the classic form, so mention both. */
enum EnumLogger {
    INSTANCE;
    private int count = 0;
    public void log(String m) { System.out.println("[ENUM " + (++count) + "] " + m); }
}
