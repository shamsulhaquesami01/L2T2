package other_section;

interface State {
    void traveldistance(float km);

    void activateLux(int hours);

    void promote();

    void demote();

    void setMood(String m);
}

// ===== Context: holds current state, delegates every call to it =====
class Subscription {
    private final State Common;
    private final State Plus;
    private final State Lux;

    private State state;
    private float distance;

    public Subscription(String mode) {
        Common = new Common(this);
        Plus = new Plus(this);
        Lux = new Lux(this);
        this.distance = 10.0f;

        if(mode.equals("Common")) this.state=this.getCommon();
         if(mode.equals("Plus")) this.state=this.getPlus();
          if(mode.equals("Lux")) this.state=this.getLux();
    }

    // every public action just delegates — NO if/else on state here
    public void traveldistance(float km) {
        state.traveldistance(km);
    }

    public void activateLux(int hours) {
        state.activateLux(hours);
    }

    public void promote() {
        state.promote();
    }
    public void demote(){
        state.demote();
    }
    public void setMood(String m){
        state.setMood(m);
    }
    void setState(State state) {
        this.state = state;
    }

    float getdistance() {
        return distance;
    }

    State getCommon() {
        return Common;
    }

    State getPlus() {
        return Plus;
    }

    State getLux() {
        return Lux;
    }
}

// ===== Concrete states =====
class Common implements State {
    private final Subscription machine;

    public Common(Subscription machine) {
        this.machine = machine;
    }

    public void traveldistance(float km) {
        if (km<10)
            System.out.println("Stable");
        else
            System.out.println("Unstable; bring him back in coverage");
    }

    public void activateLux(int hours) {
        System.out.println("Switching to LUX");
        machine.setState(machine.getLux());
        try {
            Thread.sleep(hours);
             machine.setState(machine.getCommon());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void promote() {
        System.out.println("Taking Common to Plus");
        machine.setState(machine.getPlus());
    }

    public void demote() {
        System.out.println("No Change");
    }
   

    public void setMood(String m) {
        System.out.println("Mood control Unavailable");
    }
}

class Plus implements State {
    private final Subscription machine;

    public Plus(Subscription machine) {
        this.machine = machine;
    }

    public void traveldistance(float km) {
        if (km < 50)
            System.out.println("Stable");
        else
            System.out.println("Unstable; bring him back in coverage");
    }

    public void activateLux(int hours) {
        System.out.println("Switching to LUX");
        machine.setState(machine.getLux());
        try {
            Thread.sleep(hours);
             machine.setState(machine.getPlus());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void promote() {
        System.out.println("Taking PLus to Lux");
        machine.setState(machine.getLux());
    }

    public void demote() {
        System.out.println("Taking PLus to Common");
        machine.setState(machine.getCommon());
    }

    public void setMood(String m) {
        System.out.println("Mood Control unavailavble");
    }
}

class Lux implements State {
    private final Subscription machine;

    public Lux(Subscription machine) {
        this.machine = machine;
    }

    public void traveldistance(float km) {
        if (km < 50)
            System.out.println("Stable");
        else
            System.out.println("Unstable; bring him back in coverage");
    }

    public void activateLux(int hours) {
        System.out.println("Already in LUX");
    }

    public void promote() {
        System.out.println("NO CHANGE");
    }

    public void demote() {
        System.out.println("Taking Lux to Plus");
        machine.setState(machine.getPlus());
    }

    public void setMood(String m) {
        System.out.println("Current Mood: " + m);
    }
}

public class A1_online {
    public static void main(String[] args) {
        Subscription sc = new Subscription("Common");
        sc.traveldistance(5.5f);
        sc.traveldistance(15f);
        sc.activateLux(5000);
        sc.setMood("Happy");
        sc.promote();
        sc.setMood("Sad");
        sc.activateLux(1000);
        sc.promote();
        sc.traveldistance(20.6f);
        sc.setMood("Happy");
        }

}
