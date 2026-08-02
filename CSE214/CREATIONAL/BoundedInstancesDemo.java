import java.util.ArrayList;
import java.util.List;

abstract class Base {
    protected Base() {} // Prevent direct instantiation
}

class Derived1 extends Base {
    private static final int MAX_INSTANCES = 5;
    private static final List<Derived1> instances = new ArrayList<>();

    private Derived1() {}

    public static synchronized Derived1 getInstance() {
        if (instances.size() < MAX_INSTANCES) {
            Derived1 newInstance = new Derived1();
            instances.add(newInstance);
            return newInstance;
        }
        // Return the first instance if limit is reached, creating a cycle
        return instances.get(0); 
    }
}

class Derived2 extends Base {
    private static final int MAX_INSTANCES = 10;
    private static final List<Derived2> instances = new ArrayList<>();

    private Derived2() {}

    public static synchronized Derived2 getInstance() {
        if (instances.size() < MAX_INSTANCES) {
            Derived2 newInstance = new Derived2();
            instances.add(newInstance);
            return newInstance;
        }
        return instances.get(0);
    }
}

public class BoundedInstancesDemo {
    public static void main(String[] args) {
        Derived1[] d1Array = new Derived1[7];
        Derived2[] d2Array = new Derived2[12];

        for (int i = 0; i < 7; i++) {
            d1Array[i] = Derived1.getInstance();
            System.out.println("D1 Instance " + i + ": " + d1Array[i].hashCode());
        }
        // Instances 5 and 6 will have the exact same hashcode as instance 0
    }
}