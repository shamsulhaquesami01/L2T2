/*
 * OBSERVER PATTERN — Template based on the slide's WeatherData example
 * ----------------------------------------------------------------------
 * Definition (from slide): Define a one-to-many dependency between
 * objects so that when one object changes state, all its dependents are
 * notified and updated automatically. (Publisher == Subject,
 * Subscriber == Observer)
 *
 * Core shape (this is what to reuse in the exam):
 *   1. Subject interface: registerObserver, removeObserver, notifyObservers.
 *   2. Observer interface: update(...) — one method every listener implements.
 *   3. Concrete Subject keeps a LIST of observers and loops over it to
 *      notify them whenever its own state changes.
 *   4. Concrete Observers register themselves with the subject (usually in
 *      their own constructor) and react independently inside update().
 *   5. Observers can be added/removed at RUNTIME.
 *
 * A second, generic variant is included below (MessageBoard/Listener) for
 * problems phrased as "publisher posts a message, several unrelated
 * groups react differently, and they can subscribe/unsubscribe at
 * runtime" — e.g. a raven message-board or a stock-ticker/graph/bot setup
 * where the payload is a single value instead of 3 weather readings.
 */

import java.util.*;

// ===================== Part 1: the slide's WeatherData example =====================

interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers();
}

interface Observer {
    void update(float temperature, float humidity, float pressure);
}

interface DisplayElement {
    void display();
}

class WeatherData implements Subject {
    private final List<Observer> observers = new ArrayList<>();
    private float temperature, humidity, pressure;

    public void registerObserver(Observer o) { observers.add(o); }
    public void removeObserver(Observer o)   { observers.remove(o); }

    public void notifyObservers() {
        for (Observer o : observers) {
            o.update(temperature, humidity, pressure);
        }
    }

    // called whenever new readings arrive from the weather station hardware
    public void measurementsChanged() { notifyObservers(); }

    public void setMeasurements(float temperature, float humidity, float pressure) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        measurementsChanged();
    }
}

class CurrentConditionsDisplay implements Observer, DisplayElement {
    private float temperature, humidity;
    private final Subject weatherData;

    public CurrentConditionsDisplay(Subject weatherData) {
        this.weatherData = weatherData;
        weatherData.registerObserver(this);   // register itself with the Subject
    }

    public void update(float temperature, float humidity, float pressure) {
        this.temperature = temperature;
        this.humidity = humidity;
        display();
    }

    public void display() {
        System.out.println("Current conditions: " + temperature + "F degrees and " + humidity + "% humidity");
    }
}

class StatisticsDisplay implements Observer, DisplayElement {
    private final List<Float> temps = new ArrayList<>();
    public StatisticsDisplay(Subject weatherData) { weatherData.registerObserver(this); }

    public void update(float temperature, float humidity, float pressure) {
        temps.add(temperature);
        display();
    }

    public void display() {
        double avg = temps.stream().mapToDouble(Float::doubleValue).average().orElse(0);
        System.out.printf("Avg/Max/Min temperature = %.1f/%.1f/%.1f%n",
                avg, Collections.max(temps), Collections.min(temps));
    }
}

class ForecastDisplay implements Observer, DisplayElement {
    private float currentPressure = 29.92f, lastPressure;
    public ForecastDisplay(Subject weatherData) { weatherData.registerObserver(this); }

    public void update(float temperature, float humidity, float pressure) {
        lastPressure = currentPressure;
        currentPressure = pressure;
        display();
    }

    public void display() {
        System.out.print("Forecast: ");
        if (currentPressure > lastPressure)      System.out.println("Improving weather on the way!");
        else if (currentPressure == lastPressure) System.out.println("More of the same");
        else                                      System.out.println("Watch out for cooler, rainy weather");
    }
}

// ============ Part 2: generic variant — message-board / ticker style ============

interface Listener {
    void onMessage(String message);
}

class MessageBoard {
    private final List<Listener> listeners = new ArrayList<>();
    public void subscribe(Listener l)   { listeners.add(l); }
    public void unsubscribe(Listener l) { listeners.remove(l); }

    public void post(String message) {
        System.out.println("\n[Board] New message: \"" + message + "\"");
        for (Listener l : listeners) l.onMessage(message);
    }
}

class Scouts implements Listener {
    public void onMessage(String m) {
        if (m.toLowerCase().contains("enemy")) System.out.println("Scouts: Dispatch riders!");
    }
}

class SupplyTeam implements Listener {
    public void onMessage(String m) {
        if (m.toLowerCase().contains("supplies")) System.out.println("Supply Team: Update inventory!");
    }
}

class Commander implements Listener {
    public void onMessage(String m) { System.out.println("Commander: acknowledged - " + m); }
}

// ===== Demo =====
public class ObserverPatternTemplate {
    public static void main(String[] args) {
        System.out.println("=== Weather station (typed, multi-field push) ===");
        WeatherData weatherData = new WeatherData();
        new CurrentConditionsDisplay(weatherData);
        new StatisticsDisplay(weatherData);
        ForecastDisplay forecastDisplay = new ForecastDisplay(weatherData);

        weatherData.setMeasurements(80, 65, 30.4f);
        weatherData.setMeasurements(82, 70, 29.2f);

        weatherData.removeObserver(forecastDisplay);           // runtime unsubscribe
        System.out.println("-- forecast display unsubscribed --");
        weatherData.setMeasurements(78, 90, 29.2f);

        System.out.println("\n=== Message board (generic, single-value push) ===");
        MessageBoard board = new MessageBoard();
        Scouts scouts = new Scouts();
        SupplyTeam supply = new SupplyTeam();
        Commander commander = new Commander();

        board.subscribe(scouts);
        board.subscribe(supply);
        board.subscribe(commander);

        board.post("Enemy spotted near the river");
        board.post("Winter supplies running low");

        board.unsubscribe(scouts);                              // runtime unsubscribe
        System.out.println("-- Scouts left the board room --");
        board.post("Ships seen in the east");
    }
}
