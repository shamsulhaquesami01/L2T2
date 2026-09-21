package CSE214.STRUCTURAL.my_onlines.B1;
public class GiftItem implements Gift{

    String name;
    int price;
    public GiftItem(String name , int price){
        this.name = name;
        this.price = price;
    }
    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public void show(){
        System.out.println(name + ":" + price);
    }
}
