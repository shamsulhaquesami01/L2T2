package previous.A1;

import java.util.Scanner;

public class Application {
    public static void main(String[] args) {
        // Create a Train transport
         Scanner sc = new Scanner(System.in);

         String msg =sc.next();

         Factory fac = new Factory();
         Transport trans = fac.createTransport(msg);
         trans.deliver();
         

    }

}
