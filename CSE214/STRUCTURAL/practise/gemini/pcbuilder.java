package CSE214.STRUCTURAL.practise.gemini;

import java.util.ArrayList;
import java.util.List;

interface component {
    void showDetails(int indentLevel);
    int getcost();
}

// ==========================================
// 2. Leaves (End Nodes)
// ==========================================
class ram implements component {
    private String name;
    public ram(String name) { this.name = name; }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + "- ram: " + name);
    }

    @Override
    public int getcost() { return 10000; }
}

class gpu implements component {
    private String name;
    public gpu(String name) { this.name = name; }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + "- gpu: " + name);
    }

    @Override
    public int getcost() { return 6000; }
}

 class Pack implements component {
    protected List<component> children = new ArrayList<>();
    protected packagingtype type;

    public Pack(packagingtype type) {this.type= type; };

    public void add(component component) { children.add(component); }
    public void remove(component component) { children.remove(component); }

     public void showDetails(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) );
        for (component child : children) {
            child.showDetails(indentLevel + 4);
        }
        type.title();
    }
    public int getcost() {
        int count = 0;
        for (component child : children) {
            count += child.getcost();
        }
        return count+type.getcharge();
    }
}


interface packagingtype{
     int getcharge();
     void title();
}
class standard implements packagingtype{

    @Override
    public int getcharge() {
        return 0;
    }
    @Override
    public void title(){
        System.out.println("Standard Package Type");
    }
    
}
class secure implements packagingtype{

    @Override
    public int getcharge() {
        return 500;
    }
      @Override
    public void title(){
        System.out.println("Secured Package Type; 500 Charged");
    }
    
}
class RGB implements packagingtype{

    @Override
    public int getcharge() {
        return 1500;
    }
      @Override
    public void title(){
        System.out.println("RGB Package Type; 1500 charged");
    }
}

abstract class Decorator implements component {
    protected component wrappee;
    public Decorator(component source) {
        this.wrappee = source;
    }
}


class ExpressAssembly extends Decorator {
    public ExpressAssembly(component source) { super(source); }

    @Override
    public int getcost() {
        return wrappee.getcost()+2000;
    }

    @Override
    public void showDetails(int indentLevel) {
        System.out.println("Express Assembly: 2000 charged");
        wrappee.showDetails(indentLevel);
        
    }
    
}

public class pcbuilder{
    public static void main(String[] args) {
        component ram = new ram("corsair");
        component gpu = new gpu("Nvidia rtx 5070");
        Pack pk = new Pack(new standard());
        pk.add(ram);
        pk.add(gpu);
        component ck = new ExpressAssembly(pk);
        ck.showDetails(1);
        System.out.println(ck.getcost());
        pk= new Pack(new RGB());
        pk.add(ram);
        pk.add(gpu);
        ck = new ExpressAssembly(pk);
        ck.showDetails(1);
        System.out.println(ck.getcost());
    }
}