package CSE214.template;

// --- BUILDER ---
import java.util.ArrayList;
import java.util.List;

// 1. The Complex Product
class ComplexProduct {
    private List<String> parts = new ArrayList<>();
    public void add(String part) { parts.add(part); }
    public void show() { System.out.println("Product Parts: " + String.join(", ", parts)); }
}

// 2. Builder Interface
interface Builder {
    void reset();
    void buildPartA(); // e.g., Flight / Frame
    void buildPartB(); // e.g., Hotel / Gear System
    void buildPartC(); // e.g., Activity / Tires
    ComplexProduct getResult();
}

// 3. Concrete Builder
class ConcreteBuilder implements Builder {
    private ComplexProduct product;

    public ConcreteBuilder() { this.reset(); }
    public void reset() { this.product = new ComplexProduct(); }

    @Override
    public void buildPartA() { product.add("Part A (e.g., Business Class Flight)"); }
    @Override
    public void buildPartB() { product.add("Part B (e.g., 5-Star Resort)"); }
    @Override
    public void buildPartC() { product.add("Part C (e.g., Spa Treatment)"); }

    @Override
    public ComplexProduct getResult() {
        ComplexProduct result = this.product;
        this.reset(); // Reset for the next build
        return result;
    }
}

// 4. Director (Ensures construction steps are called in order)
class Director {
    private Builder builder;

    public void setBuilder(Builder builder) {
        this.builder = builder;
    }

    public void constructPresetPackage() {
        builder.buildPartA();
        builder.buildPartB();
        builder.buildPartC();
    }
}

// 5. Client Code
public class BuilderDemo {
    public static void main(String[] args) {
        Director director = new Director();
        Builder builder = new ConcreteBuilder();
        director.setBuilder(builder);
        
        director.constructPresetPackage();
        ComplexProduct product = builder.getResult();
        product.show();
    }
}