package CSE214.STRUCTURAL.other_sec;

import java.util.ArrayList;
import java.util.List;

interface OrderItem {
    int getPrice();

    void print(String msg);
}
interface fooditems extends OrderItem {
    int getPrice();

    void print(String msg);
}
class Food implements fooditems {
    String name;
    int price;

    public Food(String name, int price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public void print(String msg) {
        System.out.println("Food :" + name + "( $" + price + " )");

    }

}

class SetMenu implements fooditems{
     List<Food> children = new ArrayList<>();
     String name;
     public SetMenu(String name) {
        this.name = name;
    }
     
    public void addFood(Food component) {
        children.add(component);
    }

    public void removeFood(Food component) {
        children.remove(component);
    }

    @Override
    public int getPrice() {
       int total = 0;
        for (Food child : children) {
            total += child.getPrice();
        }
        return total-(total/10);
    }

    @Override
    public void print(String msg) {
      System.out.println(" SetMenu :  "+ name);
        for (Food child : children) {
            child.print("");
        }
        
    }

    
}


interface groceryitems extends OrderItem {
    int getPrice();

    void print(String msg);
}

class Grocery implements groceryitems {
    String name;
    int price;

    public Grocery(String name, int price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public void print(String msg) {
        System.out.println("Grocery :" + name + "( $" + price + " )");

    }

}

class GroceryPackage implements groceryitems {
    String name;
    List<groceryitems> children = new ArrayList<>();

    public GroceryPackage(String name) {
        this.name = name;
    }
   public void add(groceryitems component) {
        children.add(component);
    }

    public void remove(groceryitems component) {
        children.remove(component);
    }

    @Override
    public int getPrice() {
        int total = 0;
        for (groceryitems child : children) {
            total += child.getPrice();
        }
        return total;
    }

    @Override
    public void print(String msg) {
        System.out.println(" Package :  "+ name);
        for (groceryitems child : children) {
            child.print("");
        }
    }

}


class Order {
    private List<OrderItem> items = new ArrayList<>();

    public void add(OrderItem item) {
        items.add(item);
    }

    public double getTotalPrice() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getPrice();
        }
        return total;
    }

    public void printReceipt() {
        System.out.println("========== RECEIPT ==========");
        for (OrderItem item : items) {
            item.print("");
        }
        System.out.println("-----------------------------");
        System.out.printf("Total Bill: £%.2f%n", getTotalPrice());
    }
}

public class B2_Restaurant {
    public static void main(String[] args) {
        // Foods
        Food burger = new Food("Burger", 8);
        Food pizza = new Food("Pizza", 10);
        Food fries = new Food("French Fries", 3);
        // Set Menu
        SetMenu lunch = new SetMenu("Lunch Combo");
        lunch.addFood(burger);
        lunch.addFood(fries);
        // Grocery Items
        Grocery rice = new Grocery("Rice", 20);
        Grocery oil = new Grocery("Cooking Oil", 12);
        Grocery eggs = new Grocery("Eggs", 6);
        Grocery sugar = new Grocery("Sugar", 5);
        // Small Package
        GroceryPackage breakfastPack = new GroceryPackage("Breakfast Pack");
        breakfastPack.add(eggs);
        breakfastPack.add(sugar);
        // Large Package (contains another package)
        GroceryPackage monthlyPack = new GroceryPackage("Monthly Essentials");
        monthlyPack.add(rice);
        monthlyPack.add(oil);
        monthlyPack.add(breakfastPack);
        // Customer Order
        Order order = new Order();
        order.add(pizza);
        order.add(lunch);
        order.add(rice);
        order.add(monthlyPack);
        order.printReceipt();
    }
}
