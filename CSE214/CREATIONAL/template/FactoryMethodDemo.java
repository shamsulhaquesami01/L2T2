package template;

// --- FACTORY METHOD ---
// 1. Common Product Interface
interface Product {
    void executeLogic(); // e.g., deliver() or notifyUser()
}

// 2. Concrete Products
class ConcreteProductA implements Product {
    @Override
    public void executeLogic() {
        System.out.println("Execution logic for ConcreteProductA (e.g., Truck delivery)");
    }
}

class ConcreteProductB implements Product {
    @Override
    public void executeLogic() {
        System.out.println("Execution logic for ConcreteProductB (e.g., Ship delivery)");
    }
}

// 3. Base Creator (The core of the Factory Method)
abstract class Creator {
    // The factory method itself
    public abstract Product createProduct();
    
    // Optional: Core business logic that uses the product
    public void doWork() {
        Product p = createProduct();
        p.executeLogic();
    }
}

// 4. Concrete Creators
class CreatorA extends Creator {
    @Override
    public Product createProduct() {
        return new ConcreteProductA();
    }
}

class CreatorB extends Creator {
    @Override
    public Product createProduct() {
        return new ConcreteProductB();
    }
}

// 5. Client Code (Main Method)
public class FactoryMethodDemo {
    public static void main(String[] args) {
        // The client only interacts with the abstract Creator
        Creator creator = new CreatorA(); // Swap to CreatorB based on string input if needed
        creator.doWork(); 
    }
}