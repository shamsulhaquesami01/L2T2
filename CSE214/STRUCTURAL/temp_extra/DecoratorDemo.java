package CSE214.STRUCTURAL.temp_extra;
// ==========================================
// 1. Component & Concrete Component
// ==========================================
interface DataSource {
    void writeData(String data);
    String readData();
}

class FileDataSource implements DataSource {
    private String name;
    private String dataBuffer = ""; // Simulating a file

    public FileDataSource(String name) {
        this.name = name;
    }

    @Override
    public void writeData(String data) {
        System.out.println("Writing to [" + name + "]: " + data);
        this.dataBuffer = data;
    }

    @Override
    public String readData() {
        System.out.println("Reading from [" + name + "]");
        return dataBuffer;
    }
}

// ==========================================
// 2. Base Decorator
// ==========================================
abstract class Decorator implements DataSource {
    protected DataSource wrappee;

    public Decorator(DataSource source) {
        this.wrappee = source;
    }

    @Override
    public void writeData(String data) {
        wrappee.writeData(data);
    }

    @Override
    public String readData() {
        return wrappee.readData();
    }
}

// ==========================================
// 3. Concrete Decorators
// ==========================================
class EncryptionDecorator extends Decorator {
    public EncryptionDecorator(DataSource source) { super(source); }

    @Override
    public void writeData(String data) {
        System.out.println("Encrypting data...");
        String encrypted = "###" + data + "###"; // Mock encryption
        super.writeData(encrypted);
    }

    @Override
    public String readData() {
        String data = super.readData();
        System.out.println("Decrypting data...");
        return data.replace("###", ""); // Mock decryption
    }
}

class CompressionDecorator extends Decorator {
    public CompressionDecorator(DataSource source) { super(source); }

    @Override
    public void writeData(String data) {
        System.out.println("Compressing data...");
        String compressed = "ZIP[" + data + "]"; // Mock compression
        super.writeData(compressed);
    }

    @Override
    public String readData() {
        String data = super.readData();
        System.out.println("Decompressing data...");
        return data.replace("ZIP[", "").replace("]", ""); // Mock decompression
    }
}

class LoggingDecorator extends Decorator {
    public LoggingDecorator(DataSource source) { super(source); }

    @Override
    public void writeData(String data) {
        System.out.println("[LOG]: Attempting to write payload length: " + data.length());
        super.writeData(data);
        System.out.println("[LOG]: Write operation successful.");
    }
}

// ==========================================
// 4. Main / Client
// ==========================================
public class DecoratorDemo {
    public static void main(String[] args) {
        String salaryRecords = "SalaryData: 50000";

        // Wrap the file inside Encryption, then Compression, then Logging
        DataSource secureFile = new LoggingDecorator(
                                    new CompressionDecorator(
                                        new EncryptionDecorator(
                                            new FileDataSource("finances.dat"))));

        System.out.println("--- Write Execution Flow ---");
        secureFile.writeData(salaryRecords);

        System.out.println("\n--- Read Execution Flow ---");
        String retrievedData = secureFile.readData();
        System.out.println("Final Output: " + retrievedData);
    }
}