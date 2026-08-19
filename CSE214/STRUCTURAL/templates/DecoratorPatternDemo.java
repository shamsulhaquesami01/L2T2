package CSE214.STRUCTURAL.templates;

interface BaseProduct {
    void showDetails(int indentLevel);

    double getPrice();
}

class BaseProduct1 implements BaseProduct {
    private String name;
    private double price;

    public BaseProduct1(String name, double price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + "- BaseProduct1: " + name);
    }

    @Override
    public double getPrice() {
        return this.price;
    }
}

class BaseProduct2 implements BaseProduct {
    private String name;
    private double price;

    public BaseProduct2(String name, double price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + "- BaseProduct2: " + name);
    }

    @Override
    public double getPrice() {
        return this.price;
    }
}

// FIX: field/parameter renamed from "BaseProduct" (same as the type name)
// to "wrapped". The old version compiled fine -- Java allows a field/param
// to share a name with a type, since types and variables live in separate
// namespaces -- but it's a real readability risk when you're renaming fast
// under exam pressure. Every other template uses a plain name like
// `wrapped`/`component` for exactly this reason.
abstract class BaseDecorator implements BaseProduct {
    protected BaseProduct wrapped;

    public BaseDecorator(BaseProduct wrapped) {
        this.wrapped = wrapped;
    }
}

class Mocha extends BaseDecorator {
    public Mocha(BaseProduct wrapped) {
        super(wrapped);
    }

    @Override
    public void showDetails(int indentLevel) {
        wrapped.showDetails(indentLevel);
        System.out.println(" ".repeat(indentLevel) + "  + Mocha");   // FIX: now respects indentLevel
    }

    @Override
    public double getPrice() {
        return .20 + wrapped.getPrice();
    }
}

class Whip extends BaseDecorator {
    public Whip(BaseProduct wrapped) {
        super(wrapped);
    }

    @Override
    public void showDetails(int indentLevel) {
        wrapped.showDetails(indentLevel);
        System.out.println(" ".repeat(indentLevel) + "  + Whip");
    }

    @Override
    public double getPrice() {
        return .10 + wrapped.getPrice();
    }
}

public class DecoratorPatternDemo {
    public static void main(String[] args) {
        BaseProduct espresso = new BaseProduct1("Espresso", 100);
        espresso.showDetails(1);
        System.out.printf("$%.2f%n", espresso.getPrice());

        BaseProduct darkRoast = new BaseProduct2("DarkRoast", 200);
        darkRoast = new Mocha(darkRoast);
        darkRoast = new Mocha(darkRoast);
        darkRoast = new Whip(darkRoast);
        darkRoast.showDetails(1);
        System.out.printf("$%.2f%n", darkRoast.getPrice());
        // 200 + .20 + .20 + .10 = $200.50
    }
}

