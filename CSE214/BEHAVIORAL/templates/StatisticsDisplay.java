package templates;

import java.util.ArrayList;
import java.util.List;

// ==========================================
// 1. THE INTERFACES
// ==========================================
 interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers();
}

 interface Observer {
    // The state values the Observers get from the Subject
    void update(float temp, float humidity, float pressure);
}

 interface DisplayElement {
    // A secondary interface just to force all displays to have a render method
    void display();
}

// ==========================================
// 2. THE CONCRETE SUBJECT (The Publisher)
// ==========================================
 class WeatherData implements Subject {
    // We use an ArrayList to hold the observers
    private List<Observer> observers;
    private float temperature;
    private float humidity;
    private float pressure;

    public WeatherData() {
        // Create the list in the constructor
        observers = new ArrayList<>();
    }

    @Override
    public void registerObserver(Observer o) {
        // When an observer registers, we just add it to the end of the list
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        // When an observer wants to un-register, we just take it off the list
        int i = observers.indexOf(o);
        if (i >= 0) {
            observers.remove(i);
        }
    }

    @Override
    public void notifyObservers() {
        // This is the fun part! We loop through the list and tell everyone.
        // Because they all implement Observer, we KNOW they all have an update() method.
        for (Observer observer : observers) {
            observer.update(temperature, humidity, pressure);
        }
    }

    // This is the trigger method called by the physical hardware
    public void measurementsChanged() {
        notifyObservers(); // We notify the observers when we get updated measurements
    }

    // A helper method to simulate the hardware pushing new data
    public void setMeasurements(float temperature, float humidity, float pressure) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        measurementsChanged();
    }
}

// ==========================================
// 3. THE CONCRETE OBSERVER (The Subscriber)
// ==========================================
 class CurrentConditionsDisplay implements Observer, DisplayElement {
    private float temperature;
    private float humidity;
    private Subject weatherData;

    // The constructor is passed the weatherData object (the Subject)
    // and we use it to automatically register the display as an observer.
    public CurrentConditionsDisplay(Subject weatherData) {
        this.weatherData = weatherData;
        weatherData.registerObserver(this); 
    }

    @Override
    public void update(float temp, float humidity, float pressure) {
        // When update is called, we save the temp and humidity and call display
        this.temperature = temp;
        this.humidity = humidity;
        display();
    }

    @Override
    public void display() {
        // Just prints out the most recent temp and humidity
        System.out.println("Current conditions: " + temperature + 
                           "F degrees and " + humidity + "% humidity");
    }
}
 class StatisticsDisplay implements Observer, DisplayElement {
    private float maxTemp = 0.0f;
    private float minTemp = 200;
    private float tempSum = 0.0f;
    private int numReadings;

    public StatisticsDisplay(Subject weatherData) {
        weatherData.registerObserver(this);
    }

    @Override
    public void update(float temp, float humidity, float pressure) {
        tempSum += temp;
        numReadings++;

        if (temp > maxTemp) {
            maxTemp = temp;
        }
        if (temp < minTemp) {
            minTemp = temp;
        }
        display();
    }

    @Override
    public void display() {
        System.out.println("Avg/Max/Min temperature = " + (tempSum / numReadings)
            + "/" + maxTemp + "/" + minTemp);
    }
}

 class ForecastDisplay implements Observer, DisplayElement {
    private float currentPressure = 29.92f;  
    private float lastPressure;

    public ForecastDisplay(Subject weatherData) {
        weatherData.registerObserver(this);
    }

    @Override
    public void update(float temp, float humidity, float pressure) {
        lastPressure = currentPressure;
        currentPressure = pressure;
        display();
    }

    @Override
    public void display() {
        System.out.print("Forecast: ");
        if (currentPressure > lastPressure) {
            System.out.println("Improving weather on the way!");
        } else if (currentPressure == lastPressure) {
            System.out.println("More of the same");
        } else if (currentPressure < lastPressure) {
            System.out.println("Watch out for cooler, rainy weather");
        }
    }
}