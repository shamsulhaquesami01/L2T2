public class Eco extends GiftBox{


    Eco(GiftPackage pack){
        super("Eco Friendly Box", pack);
    }

    @Override
    public int getPrice(){
        return super.getPrice() + 8;
    }
}
