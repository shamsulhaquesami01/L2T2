package CSE214.STRUCTURAL.templates;

// 1. The Implementation (Platform Layer)
 interface Color {
    String fill(); 
}

// 2. Concrete Implementations


 class Red implements Color {
    @Override
    public String fill() {
        return "Coloring it Red";
    }
}

 class Blue implements Color {
    @Override
    public String fill() {
        return "Coloring it Blue";
    }
}

// 3. The Abstraction (Control Layer)
 abstract class Shape {
    // THIS IS THE BRIDGE: 
    // Instead of inheriting color, the Shape holds a reference (composition) to the Color interface.
    protected Color color; 

    // The abstraction is linked to a specific implementation upon creation via the constructor.
    public Shape(Color color) {
        this.color = color;
    }
    
    // The high-level operation that will delegate work to the implementation.
    public abstract void draw(); 
}

// 4. Refined Abstractions

 class Circle extends Shape {
    
    public Circle(Color color) {
        super(color);
    }

    @Override
    public void draw() {
        // The refined abstraction delegates the color work across the bridge to the Color object.
        System.out.println("Drawing a Circle... " + color.fill());
    }
}

 class Square extends Shape {
    
    public Square(Color color) {
        super(color);
    }

    @Override
    public void draw() {
        System.out.println("Drawing a Square... " + color.fill());
    }
}


// 5. The Client Code

public class BridgePatternDemo {
    public static void main(String[] args) {
        // The client is responsible for linking the abstraction with the concrete implementation.
        
        // We create the refined abstractions (Shapes) and pass the implementations (Colors) across the bridge
        Shape redCircle = new Circle(new Red());
        Shape blueSquare = new Square(new Blue());

        // The client interacts only with the high-level abstraction.
        redCircle.draw();   // Output: Drawing a Circle... Coloring it Red
        blueSquare.draw();  // Output: Drawing a Square... Coloring it Blue
    }
}