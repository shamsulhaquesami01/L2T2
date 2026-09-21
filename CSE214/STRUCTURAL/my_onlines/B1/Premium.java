package CSE214.STRUCTURAL.my_onlines.B1;
public class Premium extends GiftBox{

    Premium(GiftPackage pack){
        super("Premium Box", pack);
    }

    @Override
    public int getPrice(){
        return super.getPrice() + 15;
    }
}
