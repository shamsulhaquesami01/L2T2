package CSE214.STRUCTURAL.my_onlines.B1;
import java.util.ArrayList;
import java.util.List;

abstract class GiftPackage implements Gift{

    String name;
    List<GiftItem> items;

    public GiftPackage(String name){
        this.name = name;
        items = new ArrayList<GiftItem>();
    }

    public void add(GiftItem item){
        items.add(item);
    }

    public void remove(GiftItem item){
        items.remove(item);
    }

    @Override
    public int getPrice() {
       int total = 0;
       for(GiftItem i : items){
           total += i.getPrice();
       }
       return total;

    }

    @Override
    public void show(){
        for(GiftItem i : items){
            i.show();
        }
    }
}
