package CSE214.STRUCTURAL.temp_extra;

// ==========================================
// 1. Implementation (Platform Layer)
// ==========================================
interface RenderingEngine {
    void renderCircle(float radius);
    void renderSquare(float sideLength);
}

// ==========================================
// 2. Concrete Implementations
// ==========================================
class OpenGLRenderer implements RenderingEngine {
    @Override
    public void renderCircle(float radius) {
        System.out.println("OpenGL rendering circle with radius " + radius);
    }
    @Override
    public void renderSquare(float sideLength) {
        System.out.println("OpenGL rendering square with side " + sideLength);
    }
}

class DirectXRenderer implements RenderingEngine {
    @Override
    public void renderCircle(float radius) {
        System.out.println("DirectX rendering circle with radius " + radius);
    }
    @Override
    public void renderSquare(float sideLength) {
        System.out.println("DirectX rendering square with side " + sideLength);
    }
}

class VulkanRenderer implements RenderingEngine {
    @Override
    public void renderCircle(float radius) {
        System.out.println("Vulkan rendering circle with radius " + radius);
    }
    @Override
    public void renderSquare(float sideLength) {
        System.out.println("Vulkan rendering square with side " + sideLength);
    }
}

// ==========================================
// 3. Abstraction (Control Layer)
// ==========================================
abstract class GraphicShape {
    protected RenderingEngine engine; // The Bridge

    public GraphicShape(RenderingEngine engine) {
        this.engine = engine;
    }

    public abstract void draw();
    public abstract void scale(float scaleFactor);
}

// ==========================================
// 4. Refined Abstractions
// ==========================================
class CircleShape extends GraphicShape {
    private float radius;

    public CircleShape(RenderingEngine engine, float radius) {
        super(engine);
        this.radius = radius;
    }

    @Override
    public void draw() {
        System.out.print("Circle Control Logic: ");
        engine.renderCircle(radius);
    }

    @Override
    public void scale(float scaleFactor) {
        radius *= scaleFactor;
        System.out.println("Circle scaled by " + scaleFactor);
    }
}

class SquareShape extends GraphicShape {
    private float sideLength;

    public SquareShape(RenderingEngine engine, float sideLength) {
        super(engine);
        this.sideLength = sideLength;
    }

    @Override
    public void draw() {
        System.out.print("Square Control Logic: ");
        engine.renderSquare(sideLength);
    }

    @Override
    public void scale(float scaleFactor) {
        sideLength *= scaleFactor;
        System.out.println("Square scaled by " + scaleFactor);
    }
}

// ==========================================
// 5. Main / Client
// ==========================================
public class BridgeDemo {
    public static void main(String[] args) {
        // Instantiate different engines (Implementations)
        RenderingEngine opengl = new OpenGLRenderer();
        RenderingEngine directx = new DirectXRenderer();
        RenderingEngine vulkan = new VulkanRenderer();

        // Instantiate shapes (Abstractions) linked via the bridge
        GraphicShape circle1 = new CircleShape(opengl, 5.0f);
        GraphicShape circle2 = new CircleShape(vulkan, 10.0f);
        GraphicShape square1 = new SquareShape(directx, 4.0f);

        System.out.println("--- Rendering Shapes across different platforms ---");
        circle1.draw();
        circle2.draw();
        square1.draw();

        System.out.println("\n--- Modifying Abstractions ---");
        circle1.scale(2.0f);
        circle1.draw();
    }
}
