package template;
// --- SINGLETON ---
public class LoggerSingleton {
    // 1. Private static instance
    private static LoggerSingleton instance;
    
    // Example state
    private String logData = "";

    // 2. Private constructor prevents instantiation via 'new'
    private LoggerSingleton() {
        System.out.println("Singleton Instance Created.");
    }

    // 3. Public static method that guarantees single instance creation
    public static LoggerSingleton getInstance() {
        if (instance == null) {
            instance = new LoggerSingleton();
        }
        return instance;
    }

    // 4. Core Business Logic
    public void writeLog(String message) {
        logData += message + "\n";
        System.out.println("Logged: " + message);
    }
    
    public void showLogs() {
        System.out.println("--- All Logs ---\n" + logData);
    }
}

// 5. Client Code
class SingletonDemo {
    public static void main(String[] args) {
        LoggerSingleton moduleA = LoggerSingleton.getInstance();
        moduleA.writeLog("Module A performed a transaction.");
        
        LoggerSingleton moduleB = LoggerSingleton.getInstance();
        moduleB.writeLog("Module B performed a withdrawal.");
        
        // Demonstrate they are the exact same instance in memory
        System.out.println("Are both instances the same? " + (moduleA == moduleB));
        moduleB.showLogs();
    }
}