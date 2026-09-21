package CSE214.STRUCTURAL.my_onlines.B1;
abstract class GiftBox {

    protected GiftPackage pack;
    protected String name;
    GiftBox(String name, GiftPackage pack){
        this.name = name;
        this.pack = pack;
    }

    public int getPrice(){
        return pack.getPrice();
    }

    public void show(){
        pack.show();
    }
}
