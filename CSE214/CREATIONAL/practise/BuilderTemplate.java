package practise;



public class BuilderTemplate {

    public static void main(String[] args) {

        Director director = new Director();

        System.out.println("===== Model 1, built by the director =====");
        ComputerBuilder builder1 = new ComputerBuilder();
        director.OfficePC(builder1);
        Computer relaxation = builder1.getResult();   // result comes FROM THE BUILDER
        System.out.println(relaxation);

        System.out.println();
        System.out.println("===== Model 2, SAME construction process =====");
        ComputerBuilder builder2 = new ComputerBuilder();
        director.GamingPC(builder2);
        System.out.println(builder2.getResult());

    }
}

/* ===========================================================================
 * 1. THE PRODUCT - a plain object made of several parts.
 *    Strings are fine; the question usually says so explicitly.
 * ======================================================================== */
class Computer {

    private String Processor;
    private String Ram;
    private String Memory;

    public void setProcessor(String Processor)     { this.Processor = Processor; }
    public void setRam(String Ram)       { this.Ram = Ram; }
    public void setMemory(String Memory) { this.Memory = Memory; }

    @Override
    public String toString() {
        return "Computer {"
             + "\n   Processor   : " + Processor
             + "\n   Ram    : " + Ram
             + "\n   Memory : " + Memory
             + "\n}";
    }
}

/* ===========================================================================
 * 2. THE BUILDER INTERFACE - one method per construction STEP.
 *    getResult() is deliberately NOT here: different builders return
 *    different types (a package vs a text brochure).
 * ======================================================================== */
interface Builder {
    void reset();
    void setProcessor(String Processor);
    void setRam(String Ram);
    void setMemory(String Memory);
}

/* ===========================================================================
 * 3. CONCRETE BUILDER #1 - assembles the real product.
 * ======================================================================== */
class ComputerBuilder implements Builder {

    private Computer pkg;

    public ComputerBuilder() { reset(); }

    public void reset() { pkg = new Computer(); }

    public void setProcessor(String Processor)     { pkg.setProcessor(Processor); }
    public void setRam(String Ram)       { pkg.setRam(Ram); }
    public void setMemory(String Memory) { pkg.setMemory(Memory); }

    // Result is fetched from the BUILDER, not from the director.
    public Computer getResult() {
        Computer result = pkg;
        reset();                    // ready to build the next one
        return result;
    }
}


class Director {

    public void OfficePC(Builder builder) {
        builder.reset();
        builder.setProcessor("Core i3");
        builder.setRam("12GB DDR5");
        builder.setMemory("512GB SSD");
    }

    public void GamingPC(Builder builder) {
        builder.reset();
        builder.setProcessor("Core i9");
        builder.setRam("32 GB DDR5");
        builder.setMemory("2TB NVMe");
    }

    // Copy-paste for a third model if the question asks for one.
}