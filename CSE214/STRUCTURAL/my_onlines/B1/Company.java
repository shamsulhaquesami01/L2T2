package CSE214.STRUCTURAL.my_onlines.B1;
import java.util.ArrayList;
import java.util.List;

public class Company {

    String name = "Company A";

    List<Gift> items = new ArrayList<Gift>();

    public void add(Gift item){
        items.add(item);
    }

    public void remove(Gift item){
        items.remove(item);
    }

    public void show(){
        for (Gift i : items){
            i.show();
        }
    }
}
