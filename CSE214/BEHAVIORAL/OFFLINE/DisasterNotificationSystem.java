package OFFLINE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

class AlertData{
    private String title;
    private String category;
    private String location;
    private String securityLevel;
    private String safetyInstructions;
    public AlertData(String title,
     String category,
     String location,
     String securityLevel,
     String safetyInstructions){
            this.category=category;
            this.title=title;
            this.location=location;
            this.securityLevel=securityLevel;
            this.safetyInstructions=safetyInstructions;
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
    public String getSecurityLevel() {
        return securityLevel;
    }
    public String getSafetyInstructions() {
        return safetyInstructions;
    }
    
}



interface AlertSystem{
    void subscribe(Observer o, String category);
    void unsubscribe(Observer o, String category);
    void publishAlert(AlertData data);
}

class BDAlertSystem implements AlertSystem{
    Map<String, List<Observer> > categorySubs = new HashMap<>();

    @Override
   public void publishAlert(AlertData data) {
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
        List<Observer> lst = categorySubs.get(category);
        if(lst==null){
     
             List<Observer> newList = new ArrayList<>();
            newList.add(o);
            categorySubs.put(category, newList);
        }
        else{
                  lst.add(o);
        }
    }

    @Override
    public void unsubscribe(Observer o, String category) {
        List<Observer> lst = categorySubs.get(category);
        if(lst == null) return;
        lst.remove(o);
        
    }

    
}

interface Observer{
    void update(AlertData data);
}
class Citizen implements Observer{
    private String name;
    private List<AlertData> receivedAlerts;
     
    public Citizen(String name) {
        this.name = name;
        this.receivedAlerts = new ArrayList<>();
    }
    @Override
    public void update(AlertData data) {
        System.out.println("Citizen :"+name+" recived "+data.getCategory()+" alert: "+data.getTitle());
        receivedAlerts.add(data);
    }
    public void displaynotifications(){
        System.out.println("Every Notificartion recieved: ");
        int i =1;
        for( AlertData x:receivedAlerts){
            System.out.println(i+" .");
            System.out.println("Title: "+x.getTitle());
             System.out.println("Location: "+x.getLocation());
              System.out.println("level: "+x.getSecurityLevel());
              System.out.println("Instructions: "+x.getSafetyInstructions());
              System.out.println("Category: "+x.getCategory());
              i++;
        }
    }
}

public class DisasterNotificationSystem{
    public static void main(String[] args) {

        System.out.println("--- SYSTEM INITIALIZATION ---");
        // 1. Create the Subject (The Central Alert System)
        AlertSystem bdAlert = new BDAlertSystem();

        // 2. Register citizens (Create Observers)
        Citizen citizen1 = new Citizen("Rahim");
        Citizen citizen2 = new Citizen("Karim");
        Citizen citizen3 = new Citizen("Fatema");

        System.out.println("\n--- SETTING UP SUBSCRIPTIONS ---");
        // 3. Subscribe citizens to different categories
        bdAlert.subscribe(citizen1, "Earthquake");
        bdAlert.subscribe(citizen1, "Flood");
        
        bdAlert.subscribe(citizen2, "Fire");
        
        bdAlert.subscribe(citizen3, "Flood");
        bdAlert.subscribe(citizen3, "Fire");

        System.out.println("\n--- PUBLISHING INITIAL ALERTS ---");
        // 4. Publish at least one of each: Earthquake, Flood, and Fire
        
        AlertData earthquake1 = new AlertData(
            "Magnitude 6.5 Earthquake", "Earthquake", "Sylhet", "High", "Drop, Cover, and Hold on."
        );
        bdAlert.publishAlert(earthquake1);
        System.out.println();

        AlertData flood1 = new AlertData(
            "Flash Flood Warning", "Flood", "Sunamganj", "Severe", "Move to higher ground immediately."
        );
        bdAlert.publishAlert(flood1);
        System.out.println();

        AlertData fire1 = new AlertData(
            "Industrial Fire", "Fire", "Gazipur", "Critical", "Evacuate the area. Do not use elevators."
        );
        bdAlert.publishAlert(fire1);
        System.out.println();

        System.out.println("--- SUBSCRIPTION UPDATES ---");
        // 5. Show subscription updates or unsubscriptions
        System.out.println("Rahim unsubscribes from Flood alerts.");
        bdAlert.unsubscribe(citizen1, "Flood");

        System.out.println("Karim subscribes to Earthquake alerts.");
        bdAlert.subscribe(citizen2, "Earthquake");

        System.out.println("\n--- PUBLISHING ALERTS AFTER UPDATES ---");
        // 6. Verify updates by publishing new alerts
        
        AlertData flood2 = new AlertData(
            "River Overflow", "Flood", "Kurigram", "Moderate", "Avoid driving through flooded roads."
        );
        // Rahim should NOT receive this anymore. Only Fatema should.
        bdAlert.publishAlert(flood2);
        System.out.println();

        AlertData earthquake2 = new AlertData(
            "Aftershock Warning", "Earthquake", "Sylhet", "Moderate", "Stay away from damaged buildings."
        );
        // Karim SHOULD receive this now, along with Rahim.
        bdAlert.publishAlert(earthquake2);
        System.out.println();

        System.out.println("--- CITIZEN NOTIFICATION HISTORIES ---");
        // 7. Display the notifications received by each citizen
        citizen1.displaynotifications();
        System.out.println();
        citizen2.displaynotifications();
        System.out.println();
        citizen3.displaynotifications();
    }
}