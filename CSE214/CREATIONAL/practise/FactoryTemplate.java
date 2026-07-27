package practise;
import java.util.Scanner;



/* ===========================================================================
 * PRODUCT INTERFACE - what every created object has in common.
 * ======================================================================== */
interface PaymentMethod {
    void pay(double amount);
}

/* ---- CONCRETE PRODUCTS ---------------------------------------------------
 * Keep as many as the question needs, delete the rest, copy one to add more.
 * ------------------------------------------------------------------------ */
class Bkash implements PaymentMethod {
    public void pay(double amount) { System.out.println("   Bkash   : paying "+amount); }
}

class Nagad implements PaymentMethod {
    public void pay(double amount) { System.out.println("   Nagad    : paying "+amount); }
}

class CreditCard implements PaymentMethod {
    public void pay(double amount) { System.out.println("   CreditCard: paying "+amount); }
}

/* ===========================================================================
 * WAY 1 : SIMPLE FACTORY
 * One place in the whole program that knows the concrete class names.
 * ======================================================================== */
class PaymentMethodFactory {

    public static PaymentMethod create(String type) {
        if (type == null) {
            throw new IllegalArgumentException("PaymentMethod type must not be null");
        }
        switch (type.toLowerCase()) {
            case "road":
            case "bkash":    return new Bkash();
            case "sea":
            case "nagad":     return new Nagad();
            case "air":
            case "creditcard": return new CreditCard();

            default:
                throw new IllegalArgumentException("Unknown PaymentMethod type: " + type);
        }
    }
}

public class FactoryTemplate {

    public static void main(String[] args) {

        System.out.println("===== WAY 1 : SIMPLE FACTORY (client passes a String) =====");
        Scanner sc = new Scanner(System.in);
        String msg = sc.nextLine();
        PaymentMethod t1 = PaymentMethodFactory.create(msg);
        t1.pay(500);

        System.out.println();

    }
}

