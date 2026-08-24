package OFFLINE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

class AlertData {
    private String title;
    private String category;
    private String location;
    private String severityLevel;
    private String safetyInstructions;

    public AlertData(String title,
            String category,
            String location,
            String severityLevel,
            String safetyInstructions) {
        this.category = category;
        this.title = title;
        this.location = location;
        this.severityLevel = severityLevel;
        this.safetyInstructions = safetyInstructions;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getLocation() {
        return location;
    }

    public String getseverityLevel() {
        return severityLevel;
    }

    public String getSafetyInstructions() {
        return safetyInstructions;
    }

}

interface AlertSystem {
    void registerCitizen(Observer observer);

    void subscribe(Observer observer, String category);

    void unsubscribe(Observer observer, String category);

    void publishAlert(AlertData data);
}

class BDAlertSystem implements AlertSystem {
    private final Map<String, List<Observer>> categorySubs = new HashMap<>();
    private final List<Observer> registeredCitizens = new ArrayList<>();

    public void registerCitizen(Observer observer) {
        if (!registeredCitizens.contains(observer)) {
            registeredCitizens.add(observer);
        }
    }

    private boolean isValidCategory(String category) {
        return "Earthquake".equals(category)
                || "Flood".equals(category)
                || "Fire".equals(category);
    }

    @Override
    public void publishAlert(AlertData data) {
        if (data == null) {
            throw new IllegalArgumentException("Alert data cannot be null.");
        }

        if (!isValidCategory(data.getCategory())) {
            throw new IllegalArgumentException(
                    "Invalid disaster category: " + data.getCategory());
        }
        String category = data.getCategory();
        List<Observer> lst = categorySubs.get(category);

        if (lst != null) {
            for (Observer o : lst) {
                o.update(data);
            }
        }
    }

    @Override
    public void subscribe(Observer o, String category) {
        if (!isValidCategory(category)) {
            throw new IllegalArgumentException("Invalid disaster category: " + category);
        }
        if (!registeredCitizens.contains(o)) {
            throw new IllegalArgumentException("Citizen must be registered before subscribing.");
        }

        List<Observer> lst = categorySubs.get(category);

        if (lst == null) {
            lst = new ArrayList<>();
            categorySubs.put(category, lst);
        }

        if (!lst.contains(o)) {
            lst.add(o);
        }
    }

    @Override
    public void unsubscribe(Observer o, String category) {
        List<Observer> lst = categorySubs.get(category);

        if (lst == null) {
            return;
        }

        lst.remove(o);

        if (lst.isEmpty()) {
            categorySubs.remove(category);
        }
    }

}

interface Observer {
    void update(AlertData data);
}

class Citizen implements Observer {
    private final String name;
    private final List<AlertData> receivedAlerts;

    public Citizen(String name) {
        this.name = name;
        this.receivedAlerts = new ArrayList<>();
    }

    @Override
    public void update(AlertData data) {
        System.out.println("Citizen :" + name + " recived " + data.getCategory() + " alert: " + data.getTitle());
        receivedAlerts.add(data);
    }

    public void displayNotifications() {
        System.out.println("Every Notificartion recieved: ");
        int i = 1;
        for (AlertData x : receivedAlerts) {
            System.out.println(i + " .");
            System.out.println("Title: " + x.getTitle());
            System.out.println("Location: " + x.getLocation());
            System.out.println("level: " + x.getseverityLevel());
            System.out.println("Instructions: " + x.getSafetyInstructions());
            System.out.println("Category: " + x.getCategory());
            i++;
        }
    }
}

public class DisasterNotificationSystem {
    public static void main(String[] args) {

        System.out.println("--- SYSTEM INITIALIZATION ---");
        // Subject (The Central Alert System)
        AlertSystem bdAlert = new BDAlertSystem();

        // Creating and register citizens who are observcers
        Citizen citizen1 = new Citizen("Rahim");
        Citizen citizen2 = new Citizen("Karim");
        Citizen citizen3 = new Citizen("Fatema");

        bdAlert.registerCitizen(citizen1);
        bdAlert.registerCitizen(citizen2);
         bdAlert.registerCitizen(citizen3);

        System.out.println("\n--- SETTING UP SUBSCRIPTIONS ---");
        // Subscribing different citizens to different categories
        bdAlert.subscribe(citizen1, "Earthquake");
        bdAlert.subscribe(citizen1, "Flood");

        bdAlert.subscribe(citizen2, "Fire");

        bdAlert.subscribe(citizen3, "Flood");
        bdAlert.subscribe(citizen3, "Fire");

        System.out.println("\n--- PUBLISHING INITIAL ALERTS ---");
        // Publishing at least one of each: Earthquake, Flood, and Fire

        AlertData earthquake1 = new AlertData(
                "Magnitude 6.5 Earthquake", "Earthquake", "Sylhet", "High", "Drop, Cover, and Hold on.");
        bdAlert.publishAlert(earthquake1);
        System.out.println();

        AlertData flood1 = new AlertData(
                "Flash Flood Warning", "Flood", "Sunamganj", "Severe", "Move to higher ground immediately.");
        bdAlert.publishAlert(flood1);
        System.out.println();

        AlertData fire1 = new AlertData(
                "Industrial Fire", "Fire", "Gazipur", "Critical", "Evacuate the area. Do not use elevators.");
        bdAlert.publishAlert(fire1);
        System.out.println();

        System.out.println("--- SUBSCRIPTION UPDATES ---");

        // Showing subscription updates or unsubscriptions
        System.out.println("Rahim unsubscribes from Flood alerts.");
        bdAlert.unsubscribe(citizen1, "Flood");

        System.out.println("Karim subscribes to Earthquake alerts.");
        bdAlert.subscribe(citizen2, "Earthquake");

        System.out.println("\n--- PUBLISHING ALERTS AFTER UPDATES ---");
        // Verifying updates by publishing new alerts

        AlertData flood2 = new AlertData(
                "River Overflow", "Flood", "Kurigram", "Moderate", "Avoid driving through flooded roads.");

        bdAlert.publishAlert(flood2);
        System.out.println();

        AlertData earthquake2 = new AlertData(
                "Aftershock Warning", "Earthquake", "Sylhet", "Moderate", "Stay away from damaged buildings.");

        bdAlert.publishAlert(earthquake2);
        System.out.println();

        System.out.println("--- CITIZEN NOTIFICATION HISTORIES ---");
        // Displaying the notifications received by each citizen
        citizen1.displayNotifications();
        System.out.println();
        citizen2.displayNotifications();
        System.out.println();
        citizen3.displayNotifications();
    }
}