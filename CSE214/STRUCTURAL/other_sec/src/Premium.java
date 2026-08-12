public class Premium extends GiftBox{

    Premium(GiftPackage pack){
        super("Premium Box", pack);
    }

    @Override
    public int getPrice(){
        return super.getPrice() + 15;
    }
}
