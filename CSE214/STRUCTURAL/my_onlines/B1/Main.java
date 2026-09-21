package CSE214.STRUCTURAL.my_onlines.B1;
import java.util.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

       Company a = new Company();
       GiftItem item1 = new GiftItem("Mug", 10);
       GiftItem item2 = new GiftItem("Books", 50);
       GiftItem item3 = new GiftItem("Chocolates", 100);

       CompanyPackage c1 = new CompanyPackage("Company 1");
       c1.add(item1);
       c1.add(item2);

       PersonalPackage p1 = new PersonalPackage("Personal 1");
       p1.add(item3);
       a.add(p1);

       GiftBox prem = new Premium(p1);
       System.out.println(prem.getPrice());

       prem.show();





    }
}