package previous_online;

interface Mediator {
    void notify(Component sender, String event);
}

abstract class Component {
    protected Mediator mediator;
    public Component(Mediator mediator) { this.mediator = mediator; }
    public void setMediator(Mediator mediator) { this.mediator = mediator; }
}

class LightSensor extends Component {
    public LightSensor(Mediator mediator) { super(mediator); }
    public void detect() { 
        System.out.println("Sensor Detected High Brightness");
        mediator.notify(this, "detect"); }
}

class Blinds extends Component {
    private boolean opened = true;
    public Blinds(Mediator mediator) { super(mediator); }
    public void close() {
        opened = false;
        System.out.println("Blinds Closed");
        mediator.notify(this, "closed");
    }
    public boolean isopened() { return opened; }
}

class Ac extends Component {
    public Ac(Mediator mediator) { super(mediator); }
    public void turnon(){
        System.out.println("AC turned on");
    }
    
}

// Concrete Mediator — the ONLY class that knows how everything fits together
class AuthenticationDialog implements Mediator {
    private final LightSensor lightsensor;
    private final Blinds blind;
    private final Ac ac;

    public AuthenticationDialog() {
        lightsensor = new LightSensor(this);
        blind = new Blinds(this);
        ac = new Ac(this);
    }

    public void notify(Component sender, String event) {
        if (sender == blind && event.equals("closed")) {
            ac.turnon();
        }  
         else if (sender == lightsensor && event.equals("detect")) {
             blind.close();
        }
    }

    public LightSensor getOkLightSensor()          { return lightsensor; }
    public Blinds getblind(){ return blind; }
    public Ac getac() { return ac; }
}

public class B2 {
public static void main(String[] args) {
    AuthenticationDialog au = new AuthenticationDialog();
    LightSensor l = au.getOkLightSensor();
    Blinds b = au.getblind();
    Ac a = au.getac();

    l.detect();
}
}
