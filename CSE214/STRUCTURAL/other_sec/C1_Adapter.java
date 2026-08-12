package CSE214.STRUCTURAL.other_sec;

class LegacyInternalXMLService {
 String getDataAsXML() {
 return "<user><id>101</id><name>Ishrat</name></user>";
 }
}

// Target interface that the 3rd-party client expects
interface ApiResponse {
    String getBody();
}

class XMLJSONAdapter implements ApiResponse{
    private LegacyInternalXMLService legacy;

    public XMLJSONAdapter(LegacyInternalXMLService legacy) {
        this.legacy = legacy;
    }

    @Override
    public String getBody() {
        // TODO Auto-generated method stub
        String xml= legacy.getDataAsXML();
        return convertXmltoJSon(xml);
    }
    private String convertXmltoJSon(String xml){
        System.out.println("Adapter: Converting XML to JSON...");
        // In a real application, you would use a library like Jackson or Gson here
        return "{ \"user\": { \"id\": 101, \"name\": \"Ishrat\" } }";
    }
}

abstract class decorator implements ApiResponse{
    protected ApiResponse api;

    public decorator(ApiResponse api) {
        this.api = api;
    }

    @Override
    public String getBody() {
       return api.getBody();
    }
    
}

class CompressionDecorator extends decorator{

    public CompressionDecorator(ApiResponse api) {
        super(api);
    }

    @Override
    public String getBody() {
       String data = api.getBody();
       return Compress(data);
    }
    private String Compress(String msg){
        return "Zip[ "+msg+" ]";
    }
    
}

class EncryptDecorator extends decorator{

    public EncryptDecorator(ApiResponse api) {
        super(api);
    }

    @Override
    public String getBody() {
       String data = api.getBody();
       return Encrypt(data);
    }
    private String Encrypt(String msg){
        return "Encrypted{ "+msg+" }";
    }
    
}
public class C1_Adapter {
    public static void main(String[] args) {

        LegacyInternalXMLService lg = new LegacyInternalXMLService();
        ApiResponse api = new XMLJSONAdapter(lg);
        System.out.println(api.getBody());


        ApiResponse api2 = new EncryptDecorator( 
            new CompressionDecorator(
                new XMLJSONAdapter(lg)
            )
        );
        System.out.println(api2.getBody());
    }
}
