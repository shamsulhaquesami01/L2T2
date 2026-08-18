package CSE214.STRUCTURAL.practise.gemini;

import CSE214.STRUCTURAL.templates.AdapterDemo;

class LegacyInventorySystem {
    public String fetchStock() {
        return "ITEM : Rice |QTY :50| PRICE :70; ITEM :Oil |QTY :20|PRICE :160;";
    }
}

interface ModernAppResponse {
    String getFormattedResponse();
}
class ADAPTERR implements ModernAppResponse{
    LegacyInventorySystem adaptee ;

    public ADAPTERR(LegacyInventorySystem adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public String getFormattedResponse() {
        String old = adaptee.fetchStock();
        String neww = somemethod(old);
        return neww;
    }
    String somemethod(String s){
        return "json{"+s+"}";
    }
    
}
abstract class jsondeocrator implements ModernAppResponse{
    protected ModernAppResponse api;

    public jsondeocrator(ModernAppResponse api) {
        this.api = api;
    }
    
}
class compressed extends jsondeocrator{

    public compressed(ModernAppResponse api) {
        super(api);
    }

    @Override
    public String getFormattedResponse() {
        return "compressed*** "+api.getFormattedResponse()+ " ***";
    }
    
}
class encrypted extends jsondeocrator{

    public encrypted(ModernAppResponse api) {
        super(api);
    }

    @Override
    public String getFormattedResponse() {
        return "encrypteds @#%#^$&*&% "+api.getFormattedResponse()+ " %*&(&$%$%^Y";
    }
    
}
public class legacyAPI {
    public static void main(String[] args) {
        LegacyInventorySystem old = new LegacyInventorySystem();
        ModernAppResponse ad = new compressed(new encrypted(new ADAPTERR(old)));
        System.out.println(ad.getFormattedResponse());

    }

}
