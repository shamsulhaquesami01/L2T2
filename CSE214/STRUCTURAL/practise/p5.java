package CSE214.STRUCTURAL.practise;

import java.util.ArrayList;
import java.util.List;

interface furniture{
    void showtitle();
    int getprice();
}
class chair implements furniture{
    String name;
    int price;
    public chair(String name, int price) {
        this.name = name;
        this.price = price;
    }
    @Override
    public void showtitle() {
        System.out.println(this.name);
        
    }
    @Override
    public int getprice() {
        return this.price;
    }
    
    
}
class lamp implements furniture{
    String name;
    int price;
    public lamp(String name, int price) {
        this.name = name;
        this.price = price;
    }
    @Override
    public void showtitle() {
        System.out.println(this.name);
        
    }
    @Override
    public int getprice() {
        return this.price;
    }
    
    
}

interface finishing{
    int cost();
    String show();
}
class OakFinish implements finishing{

    @Override
    public int cost() {
       return 25;
    }

    @Override
    public String show() {
        // TODO Auto-generated method stub
        return "Oak finishing done";
    }
    
    
}
class WalnutFinish implements finishing{

    @Override
    public int cost() {
       return 60;
    }
        @Override
    public String show() {
        // TODO Auto-generated method stub
        return "walnut finishing done";
    }
    
}
class PaintedFinish implements finishing{

    @Override
    public int cost() {
       return 15;
    }
        @Override
    public String show() {
        // TODO Auto-generated method stub
        return "Painted finishing done";
    }
    
}
abstract class furniturset implements furniture{
        List<furniture> lst;
         finishing finish;
        public furniturset( finishing finish) {
            this.lst= new ArrayList<>();
            this.finish=finish;
        }
        public void add(furniture f){
            lst.add(f);
        }
        public void remove(furniture f){
            lst.remove(f);
        }
}
class LivingRoomSet extends furniturset{

    public LivingRoomSet(finishing f) {
        super(f);
    }

    @Override
    public int getprice() {
        int tot=0;
        for(furniture f : lst){
            tot += f.getprice();
        }
        return tot+finish.cost();
    }

    @Override
    public void showtitle() {
       System.out.println("This is a Livingroom set");
       for(furniture f : lst){
            f.showtitle();
        }
       System.out.println(finish.show());
    }
    
}

class OfficeSet extends furniturset{

    public OfficeSet( finishing f) {
        super(f);
    }
    @Override
    public int getprice() {
        int tot=0;
        for(furniture f : lst){
            tot += f.getprice();
        }
        return tot+finish.cost();
    }

    @Override
    public void showtitle() {
       System.out.println("This is a OfficeSet set");
       for(furniture f : lst){
            f.showtitle();
        }
        System.out.println(finish.show());
    }
    
}

public class p5 {
    public static void main(String[] args) {
        LivingRoomSet set1 = new LivingRoomSet(new WalnutFinish());
        set1.add(new chair("Tall chair", 80));
        set1.add(new lamp("Oxford lamp", 40));
        set1.showtitle();
        System.out.println(set1.getprice());
    }
}