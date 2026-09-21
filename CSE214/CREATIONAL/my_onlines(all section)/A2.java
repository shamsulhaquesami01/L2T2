

class TravelPlan {

    private String Transport;
    private String Residence;
    private String activity;

    public void setTransport(String Transport)     { this.Transport = Transport; }
    public void setResidence(String Residence)       { this.Residence = Residence; }
    public void setActivity(String activity) { this.activity = activity; }

    @Override
    public String toString() {
        return "TravelPlan {"
             + "\n   Transport   : " + Transport
             + "\n   Residence    : " + Residence
             + "\n   Activity : " + activity
             + "\n}";
    }
}


interface Builder {
    void reset();
    void setTransport(String Transport);
    void setResidence(String Residence);
    void setActivity(String activity);
}


class TravelPlanBuilder implements Builder {

    private TravelPlan pkg;

    public TravelPlanBuilder() { reset(); }

    public void reset() { pkg = new TravelPlan(); }

    public void setTransport(String Transport)     { pkg.setTransport(Transport); }
    public void setResidence(String Residence)       { pkg.setResidence(Residence); }
    public void setActivity(String activity) { pkg.setActivity(activity); }

   
    public TravelPlan getPlan() {
        TravelPlan result = pkg;
        reset();                 
        return result;
    }
}


class Director {

    public void constructLuxuryPlan(Builder builder) {
        builder.reset();
        builder.setTransport("Business Class Flight");
        builder.setResidence("Luxury Hotel");
        builder.setActivity("Pivate City Tour");
    }

    public void constructBudgetPlan(Builder builder) {
        builder.reset();
        builder.setTransport("Economy Bus");
        builder.setResidence("Hostel");
        builder.setActivity("Group Walikng tour");
    }

}



public class A2 {

    public static void main(String[] args) {

        Director director = new Director();

        TravelPlanBuilder builder1 = new TravelPlanBuilder();
        director.constructLuxuryPlan(builder1);
        TravelPlan luxury = builder1.getPlan();   
        System.out.println(luxury);

        System.out.println();

        TravelPlanBuilder builder2 = new TravelPlanBuilder();
        director.constructBudgetPlan(builder2);
        System.out.println(builder2.getPlan());

    }
}

