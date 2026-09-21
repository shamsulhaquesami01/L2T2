package CSE214.STRUCTURAL.my_onlines.B1;
public class Eco extends GiftBox{


    Eco(GiftPackage pack){
        super("Eco Friendly Box", pack);
    }

    @Override
    public int getPrice(){
        return super.getPrice() + 8;
    }
}
