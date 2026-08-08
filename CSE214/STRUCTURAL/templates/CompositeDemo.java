package CSE214.STRUCTURAL.templates;
import java.util.ArrayList;
import java.util.List;

// ==========================================
// 1. Component
// ==========================================
interface OrgComponent {
    void showDetails(int indentLevel);
    int getEmployeeCount();
}

// ==========================================
// 2. Leaves (End Nodes)
// ==========================================
class Developer implements OrgComponent {
    private String name;
    public Developer(String name) { this.name = name; }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + "- Developer: " + name);
    }

    @Override
    public int getEmployeeCount() { return 1; }
}

class Designer implements OrgComponent {
    private String name;
    public Designer(String name) { this.name = name; }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + "- Designer: " + name);
    }

    @Override
    public int getEmployeeCount() { return 1; }
}

// ==========================================
// 3. Composites (Container Nodes)
// ==========================================
abstract class CompositeNode implements OrgComponent {
    protected String name;
    protected List<OrgComponent> children = new ArrayList<>();

    public CompositeNode(String name) { this.name = name; }

    public void add(OrgComponent component) { children.add(component); }
    public void remove(OrgComponent component) { children.remove(component); }

    @Override
    public int getEmployeeCount() {
        int count = 0;
        for (OrgComponent child : children) {
            count += child.getEmployeeCount();
        }
        return count;
    }
}

class Department extends CompositeNode {
    public Department(String name) { super(name); }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + "+ Department: " + name);
        for (OrgComponent child : children) {
            child.showDetails(indentLevel + 4);
        }
    }
}

class RegionalBranch extends CompositeNode {
    public RegionalBranch(String name) { super(name); }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + "[*] Regional Branch: " + name);
        for (OrgComponent child : children) {
            child.showDetails(indentLevel + 4);
        }
    }
}

// ==========================================
// 4. Main / Client
// ==========================================
public class CompositeDemo {
    public static void main(String[] args) {
        // Create Leaves
        Developer dev1 = new Developer("Sami");
        Developer dev2 = new Developer("Arif");
        Designer des1 = new Designer("Kamal");

        // Create Tier 1 Composites
        Department techDept = new Department("Technology");
        techDept.add(dev1);
        techDept.add(dev2);

        Department designDept = new Department("UI/UX Design");
        designDept.add(des1);

        // Create Tier 2 Composite
        RegionalBranch dhakaBranch = new RegionalBranch("Dhaka HQ");
        dhakaBranch.add(techDept);
        dhakaBranch.add(designDept);

        // Execute uniform operations across the tree
        System.out.println("--- Organizational Chart ---");
        dhakaBranch.showDetails(0);

        System.out.println("\nTotal Employees in Dhaka HQ: " + dhakaBranch.getEmployeeCount());
        System.out.println("Total Employees in Tech Dept only: " + techDept.getEmployeeCount());
    }
}
