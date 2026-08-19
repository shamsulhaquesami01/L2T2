package CSE214.STRUCTURAL.templates;

import java.util.ArrayList;
import java.util.List;

// ==========================================
// 1. Component
// ==========================================
interface Component {
    void showDetails(int indentLevel);

    double getPrice();
}

// if composite can have only leaves
interface prodcuts extends Component {
    void showDetails(int indentLevel);

    double getPrice();
}

// make prodcuts implement this interface
class Product1 implements Component {
    private String name;
    private double price;

    public Product1(String name, double price) {
        this.name = name;
        this.price = price;

    }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + "- Product1: " + name);
    }

    @Override
    public double getPrice() {
        return this.price;
    }
}

class Product2 implements Component {
    private String name;
    private double price;

    public Product2(String name, double price) {
        this.name = name;
        this.price = price;

    }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + "- Product2: " + name);
    }

    @Override
    public double getPrice() {
        return this.price;
    }
}

// 3. Composites (Container Nodes)

abstract class CompositeNode implements Component {
    protected String name;
    // write products interface here instead of the whole component
    protected List<Component> children = new ArrayList<>();

    public CompositeNode(String name) {
        this.name = name;
    }

    public void add(Component component) {
        children.add(component);
    }

    public void remove(Component component) {
        children.remove(component);
    }

    @Override
    public double getPrice() {
        double count = 0;
        for (Component child : children) {
            count += child.getPrice();
        }
        return count;
    }
}

class Composite1 extends CompositeNode {
    public Composite1(String name) {
        super(name);
    }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + "+ Composite1: " + name);
        for (Component child : children) {
            child.showDetails(indentLevel + 4);
        }
    }
}

class Composite2 extends CompositeNode {
    public Composite2(String name) {
        super(name);
    }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + "[*] Regional Branch: " + name);
        for (Component child : children) {
            child.showDetails(indentLevel + 4);
        }
    }
}

// 4. Main / Client

public class CompositeDemo {
    public static void main(String[] args) {
        // Create Leaves
        Product1 p1 = new Product1("Sami", 100);
        Product1 p2 = new Product1("Arif", 60);
        Product2 p3 = new Product2("Kamal", 70);

        // Create Tier 1 Composites
        Composite1 lst1 = new Composite1("Technology");
        lst1.add(p1);
        lst1.add(p2);

        Composite1 lst2 = new Composite1("UI/UX Design");
        lst2.add(p3);

        // Create Tier 2 Composite
        Composite2 lst3 = new Composite2("Dhaka HQ");
        lst3.add(lst1);
        lst3.add(lst2);

        // Execute uniform operations across the tree
        System.out.println("--- Organizational Chart ---");
        lst3.showDetails(0);

        System.out.println("\n Total cost:  " + lst3.getPrice());
        System.out.println("Total cost " + lst1.getPrice());
    }
}
