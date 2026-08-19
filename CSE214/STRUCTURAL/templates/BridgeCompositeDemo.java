package CSE214.STRUCTURAL.templates;

import java.util.ArrayList;
import java.util.List;

// ==========================================
// BRIDGE + COMPOSITE
// A tree (Composite) where every branch node ALSO carries a mandatory,
// independently-varying second attribute (Bridge). One class plays
// BOTH roles at the same time -- that's the whole trick.
// ==========================================

// 1. Composite side: uniform tree component
interface PackageNode {
    void showDetails(int indentLevel);
    double getPrice();
}

// 2. Leaf
class GiftItem implements PackageNode {
    private String name;
    private double price;

    public GiftItem(String name, double price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + "- Item: " + name + " ($" + price + ")");
    }

    @Override
    public double getPrice() {
        return price;
    }
}

// 3. Bridge side: Implementor
interface PackagingStyle {
    double getExtraCost();
    String getLabel();
}

class StandardBox implements PackagingStyle {
    public double getExtraCost() { return 0; }
    public String getLabel() { return "Standard Box"; }
}

class PremiumBox implements PackagingStyle {
    public double getExtraCost() { return 15; }
    public String getLabel() { return "Premium Box"; }
}

class EcoBox implements PackagingStyle {
    public double getExtraCost() { return 8; }
    public String getLabel() { return "Eco Box"; }
}

// 4. GiftPackage = Composite branch node AND Bridge Abstraction, at once.
//    It `implements PackageNode` (so it can hold children like a normal
//    composite) AND it holds a `PackagingStyle` field (the bridge).
abstract class GiftPackage implements PackageNode {
    protected String name;
    protected PackagingStyle style;                             // <- the bridge
    protected List<PackageNode> children = new ArrayList<>();   // <- the composite part

    public GiftPackage(String name, PackagingStyle style) {
        this.name = name;
        this.style = style;
    }

    public void add(PackageNode child) { children.add(child); }
    public void remove(PackageNode child) { children.remove(child); }

    @Override
    public double getPrice() {
        double total = style.getExtraCost();   // NOTE: double accumulator, not int!
        for (PackageNode child : children) {
            total += child.getPrice();
        }
        return total;
    }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + kindLabel() + ": " + name + " [" + style.getLabel() + "]");
        for (PackageNode child : children) {
            child.showDetails(indentLevel + 4);
        }
    }

    protected abstract String kindLabel();   // RefinedAbstraction hook
}

// 5. RefinedAbstractions -- kind varies completely independently of style
class PersonalGiftPackage extends GiftPackage {
    public PersonalGiftPackage(String name, PackagingStyle style) { super(name, style); }
    protected String kindLabel() { return "Personal Package"; }
}

class CorporateGiftPackage extends GiftPackage {
    public CorporateGiftPackage(String name, PackagingStyle style) { super(name, style); }
    protected String kindLabel() { return "Corporate Package"; }
}

// 6. Demo
public class BridgeCompositeDemo {
    public static void main(String[] args) {
        GiftPackage pack = new PersonalGiftPackage("Birthday Combo", new PremiumBox());
        pack.add(new GiftItem("Mug", 12.50));
        pack.add(new GiftItem("Chocolate", 8.25));

        // a package can even contain ANOTHER package -- still just a PackageNode
        GiftPackage sub = new CorporateGiftPackage("Team Add-on", new EcoBox());
        sub.add(new GiftItem("Notebook", 5.00));
        pack.add(sub);

        pack.showDetails(0);
        System.out.printf("Total: $%.2f%n", pack.getPrice());
        // Expected: Premium(15) + Mug(12.50) + Chocolate(8.25) + [EcoBox(8) + Notebook(5.00)]
        //         = 15 + 20.75 + 13.00 = $48.75
    }
}
