package other_section;

// OBSERVER PATTERN - EXAM TEMPLATE
// Subject/Publisher keeps a dynamic list of observers/subscribers.

import java.util.ArrayList;
import java.util.List;

interface Observer {
    void update(String message);
}

interface Subject {
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}

class RavenBoard implements Subject {
    private final List<Observer> observers = new ArrayList<>();
    private String message;

    public void registerObserver(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        // Copy prevents problems if an observer changes registration during update.
        for (Observer observer : new ArrayList<>(observers)) {
            observer.update(message);
        }
    }

    // Call this whenever the subject's important state changes.
    public void setMessage(String message) {
        this.message = message;
        notifyObservers();
    }
}

class Commander implements Observer {
    @Override
    public void update(String message) {
        if (message.contains("Enemy") || message.contains("Ships")) {
            System.out.println("[Commander] Prepare the defenses and sound the war horns!");
        } else {
            System.out.println("[Commander] Noted: \"" + message + "\"");
        }
    }
}

// Concrete Observer: Scouts
class Scouts implements Observer {
    @Override
    public void update(String message) {
        if (message.contains("Enemy") || message.contains("Ships")) {
            System.out.println("[Scouts] Dispatch riders!");
        } else {
            System.out.println("[Scouts] Monitoring borders.");
        }
    }
}

// Concrete Observer: Supply Team
class SupplyTeam implements Observer {
    @Override
    public void update(String message) {
        if (message.contains("supplies")) {
            System.out.println("[Supply Team] Update inventory!");
        } else {
            System.out.println("[Supply Team] Rationing resources accordingly.");
        }
    }
}

public class A2 {
    public static void main(String[] args) {
        RavenBoard subject = new RavenBoard();

        Observer a = new Commander();
        Observer b = new Scouts();
        Observer c = new SupplyTeam();

        subject.registerObserver(a);
        subject.registerObserver(b);
        subject.registerObserver(c);

        subject.setMessage("Enemy spotted near the river");

        System.out.println("\nRemoving Scouts...\n");
        subject.removeObserver(b);
   
        subject.setMessage("Winter supplies running low");

        subject.registerObserver(b);
        
        System.out.println("\n Again adding Scouts...\n");

        subject.setMessage("Ship seen in the east");
    }
}
