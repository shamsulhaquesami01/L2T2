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

class ConcreteSubject implements Subject {
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

class ObserverA implements Observer {
    public void update(String message) {
        System.out.println("ObserverA received: " + message);
    }
}

class ObserverB implements Observer {
    public void update(String message) {
        System.out.println("ObserverB received: " + message);
    }
}

class ObserverC implements Observer {
    public void update(String message) {
        System.out.println("ObserverC received: " + message);
    }
}

public class ObserverTemplate {
    public static void main(String[] args) {
        ConcreteSubject subject = new ConcreteSubject();

        Observer a = new ObserverA();
        Observer b = new ObserverB();
        Observer c = new ObserverC();

        subject.registerObserver(a);
        subject.registerObserver(b);
        subject.registerObserver(c);

        subject.setMessage("First update");

        System.out.println("\nRemoving ObserverB...\n");
        subject.removeObserver(b);

        subject.setMessage("Second update");
    }
}
