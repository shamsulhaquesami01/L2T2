package CSE214.STRUCTURAL.templates;
// ==========================================
// 1. Component & Concrete Component
// ==========================================
interface DataSource {
    void writeData(String data);
    String readData();
}

class FileDataSource implements DataSource {
    private String filename;
    private String dataBuffer = ""; // Simulating a file

    public FileDataSource(String filename) {
        this.filename = filename;
    }

    @Override
    public void writeData(String data) {
        System.out.println("Writing to [" + filename + "]: " + data);
        this.dataBuffer = data;
    }

    @Override
    public String readData() {
        System.out.println("Reading from [" + filename + "]");
        return dataBuffer;
    }
}

// ==========================================
// 2. Base Decorator
// ==========================================
abstract class DataSourceDecorator implements DataSource {
    protected DataSource wrappee;

    public DataSourceDecorator(DataSource source) {
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
class EncryptionDecorator extends DataSourceDecorator {
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

class CompressionDecorator extends DataSourceDecorator {
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

class LoggingDecorator extends DataSourceDecorator {
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