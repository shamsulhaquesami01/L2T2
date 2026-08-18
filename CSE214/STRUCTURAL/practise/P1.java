package CSE214.STRUCTURAL.practise;

interface ride{
    double cost(double distance);
}

class travel implements ride{

    @Override
    public double cost(double distance) {
       return 2 + distance*1.5;
    }
    
}
abstract class decorator implements ride{
    protected ride r;

    public decorator(ride r) {
        this.r = r;
    }
    

}
class PetFee extends decorator{

    public PetFee(ride r) {
        super(r);
    }

    @Override
    public double cost(double distance) {
       return r.cost(distance)+3;
    }

    
    
}
class Luggage extends decorator{

    public Luggage(ride r) {
        super(r);
    }

    @Override
    public double cost(double distance) {
       return r.cost(distance)+2;
    }
    
}
class Priority extends decorator{

    public Priority(ride r) {
        super(r);
    }

    @Override
    public double cost(double distance) {
       return r.cost(distance)+5;
    }
}

public class P1 {

    public static void main(String[] args) {
        ride r1 = new travel();
        System.out.println(r1.cost(4));

        ride r2 = new PetFee(new Luggage(new Luggage(new travel())));
        System.out.println(r2.cost(6));

        ride r3 = new Priority(new PetFee(new travel()));
        System.out.println(r3.cost(10));
        
    }
}