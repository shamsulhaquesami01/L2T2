package other_sec.B2_Factory;

public class FactoryMethodDemo {
    public static void main(String[] args) {
        // The client only interacts with the abstract Creator
        Creator creator = new Creatorpdf(); 
        creator.doWork(); 
        creator = new Creatorword(); 
        creator.doWork(); 
    }
}