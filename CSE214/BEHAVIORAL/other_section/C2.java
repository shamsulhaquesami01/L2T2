package other_section;

import java.util.*;

// ===================== Part 1: the slide's StockData example =====================

interface Subjectt {
    void registerObserverr(Observerr o);
    void removeObserverr(Observerr o);
    void notifyObserverrs();
}

interface Observerr {
    void update(float price, float data_point);
}

interface DisplayElement {
    void display();
}

class StockData implements Subjectt {
    private final List<Observerr> observerrs = new ArrayList<>();
    private float price, data_point;
    public void registerObserverr(Observerr o) { observerrs.add(o); }
    public void removeObserverr(Observerr o)   { observerrs.remove(o); }

    public void notifyObserverrs() {
        for (Observerr o : observerrs) {
            o.update(price, data_point);
        }
    }

    // called whenever new readings arrive from the weather station hardware
    public void stockpricechanged() { notifyObserverrs(); }

    public void setstockprice(float price, float data_point) {
        this.price = price;
        this.data_point = data_point;
        stockpricechanged();
    }
}

class TickerTape implements Observerr, DisplayElement {
    private float price;

    public void update(float price, float data_point) {
        this.price = price;
        display();
    }

    public void display() {
        System.out.println("New Price: " + price );
    }
}

class Graph implements Observerr, DisplayElement {
private float data;

    public void update(float price, float data_point) {
        this.data=data_point;
        display();
    }

    public void display() {
       System.out.println("new data point:"+data);
    }
}

class BuySellBot implements Observerr, DisplayElement {
    private float price;

    public void update(float price, float data_point) {
        this.price=price;
        if(price>10000)
        display();
    }

    public void display() {
        System.out.println("The new price "+price+" triggered automated trade");       
    }
}


public class C2 {
    public static void main(String[] args) {
         StockData st = new StockData();
    TickerTape t = new TickerTape();
    Graph g = new Graph();
    BuySellBot b = new BuySellBot();
    
    st.registerObserverr(t);
    st.registerObserverr(b);
    st.registerObserverr(g);

    st.setstockprice(20000, 100);

    st.removeObserverr(b);

    System.out.println();

    st.setstockprice(30000, 50);
    }
}
