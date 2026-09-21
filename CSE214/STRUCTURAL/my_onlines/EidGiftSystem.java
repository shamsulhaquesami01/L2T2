package CSE214.STRUCTURAL.my_onlines;

import java.util.ArrayList;
import java.util.List;

// ==========================================
// 1. BRIDGE PATTERN: Packaging Implementation
// ==========================================
interface PackagingStyle {
    double getPackagingCost();
    String getPresentation();
}

class StandardBox implements PackagingStyle {
    @Override
    public double getPackagingCost() { return 0.0; }
    @Override
    public String getPresentation() { return "Standard Gift Box"; }
}

class PremiumBox implements PackagingStyle {
    @Override
    public double getPackagingCost() { return 15.0; }
    @Override
    public String getPresentation() { return "Premium wrapping with decorative ribbon"; }
}

class EcoFriendlyBox implements PackagingStyle {
    @Override
    public double getPackagingCost() { return 8.0; }
    @Override
    public String getPresentation() { return "Eco-friendly recyclable materials"; }
}

// ==========================================
// 2. COMPOSITE PATTERN: Component & Leaves
// ==========================================
interface GiftComponent {
    double getPrice();
    void showDetails(int indent);
}

class IndividualItem implements GiftComponent {
    private String name;
    private double price;

    public IndividualItem(String name, double price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public double getPrice() { return price; }

    @Override
    public void showDetails(int indent) {
        System.out.println(" ".repeat(indent) + "- Item: " + name + " ($" + price + ")");
    }
}

// ==========================================
// 3. ABSTRACTION & COMPOSITE MERGE
// ==========================================
abstract class GiftPackage implements GiftComponent {
    // The Bridge to the Implementation layer
    protected PackagingStyle packaging;
    // The Composite container
    protected List<GiftComponent> items = new ArrayList<>();

    public GiftPackage(PackagingStyle packaging) {
        this.packaging = packaging;
    }

    public void add(GiftComponent component) {
        items.add(component);
    }

    public void remove(GiftComponent component) {
        items.remove(component);
    }

    @Override
    public double getPrice() {
        double total = 0;
        for (GiftComponent item : items) {
            total += item.getPrice();
        }
        return total + packaging.getPackagingCost(); // Delegate extra cost to the bridge
    }
}

// --- Refined Abstractions ---

class CompanyPackage extends GiftPackage {
    private String packageName;

    // Company packages default to the StandardBox (No extra cost)
    public CompanyPackage(String packageName) {
        super(new StandardBox());
        this.packageName = packageName;
    }

    @Override
    public void showDetails(int indent) {
        System.out.println(" ".repeat(indent) + "[*] Company Package: " + packageName);
        System.out.println(" ".repeat(indent + 4) + "Packaging: " + packaging.getPresentation());
        for (GiftComponent item : items) {
            item.showDetails(indent + 4);
        }
    }
}

abstract class UserCraftedPackage extends GiftPackage {
    protected String packageName;
    protected String creatorName;

    // User-crafted packages allow the user to select the packaging style
    public UserCraftedPackage(String packageName, String creatorName, PackagingStyle style) {
        super(style);
        this.packageName = packageName;
        this.creatorName = creatorName;
    }
}

class PersonalGiftPackage extends UserCraftedPackage {
    public PersonalGiftPackage(String packageName, String creatorName, PackagingStyle style) {
        super(packageName, creatorName, style);
    }

    @Override
    public void showDetails(int indent) {
        System.out.println(" ".repeat(indent) + "[*] Personal Package: '" + packageName + "' by " + creatorName);
        System.out.println(" ".repeat(indent + 4) + "Packaging: " + packaging.getPresentation() + " (+$" + packaging.getPackagingCost() + ")");
        for (GiftComponent item : items) {
            item.showDetails(indent + 4);
        }
    }
}

class CorporateGiftPackage extends UserCraftedPackage {
    public CorporateGiftPackage(String packageName, String creatorName, PackagingStyle style) {
        super(packageName, creatorName, style);
    }

    @Override
    public void showDetails(int indent) {
        System.out.println(" ".repeat(indent) + "[*] Corporate Package: '" + packageName + "' by " + creatorName);
        System.out.println(" ".repeat(indent + 4) + "Packaging: " + packaging.getPresentation() + " (+$" + packaging.getPackagingCost() + ")");
        for (GiftComponent item : items) {
            item.showDetails(indent + 4);
        }
    }
}

// ==========================================
// 4. MAIN / CLIENT EXECUTION
// ==========================================
public class EidGiftSystem {
    public static void main(String[] args) {
        // 1. Create basic inventory items
        GiftComponent chocolates = new IndividualItem("Swiss Chocolates", 20.0);
        GiftComponent mug = new IndividualItem("Coffee Mug", 10.0);
        GiftComponent perfume = new IndividualItem("Luxury Perfume", 50.0);
        GiftComponent flowers = new IndividualItem("Bouquet of Roses", 30.0);

        // 2. Create a Pre-defined Company Package
        CompanyPackage companyCombo = new CompanyPackage("Eid Sweet Treat");
        companyCombo.add(chocolates);
        companyCombo.add(mug);

        // 3. User creates a Personal Gift Package (Using Premium Packaging)
        PersonalGiftPackage myPackage = new PersonalGiftPackage(
                "Eid Special for Mom",
                "Sami",
                new PremiumBox()
        );
        
        // Sami adds 2 individual items AND an existing company package to his custom package
        myPackage.add(perfume);
        myPackage.add(flowers);
        myPackage.add(companyCombo);

        // 4. Display the recursive hierarchy and total price
        System.out.println("============== EID GIFT REPOSITORY ==============\n");
        myPackage.showDetails(0);
        System.out.println("\n-------------------------------------------------");
        System.out.println("Total Price calculation: $" + myPackage.getPrice());
        System.out.println("=================================================");
    }
}