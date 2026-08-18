package CSE214.STRUCTURAL.practise;

interface alert {
    void showtype();

    double cost();
}

class securityalert implements alert {

    @Override
    public void showtype() {
        System.out.println("secyrity alert");

    }

    @Override
    public double cost() {
        return 0.5;
    }

}

class MaintenanceAlert implements alert {
    @Override
    public void showtype() {
        System.out.println("madoubleainance alert");

    }

    @Override
    public double cost() {
        return 0;
    }

}

abstract class channel {

    abstract double cost();
}

class PushChannel extends channel {

    alert type;

    public PushChannel(alert type) {
        this.type = type;
    }

    double cost() {
        return 1 + type.cost();
    }
}

class VoiceChannel extends channel {
    alert type;

    public VoiceChannel(alert type) {
        this.type = type;
    }

    double cost() {
        return 4 + type.cost();
    }
}

abstract class decoratorr extends channel {
    channel ch;

    public decoratorr(channel ch) {
        this.ch = ch;
    }

}

class PriortyFlag extends decoratorr {

    public PriortyFlag(channel ch) {
        super(ch);
    }

    @Override
    double cost() {
        return ch.cost() + 5;
    }

}

class DeliveryConfirm extends decoratorr {

    public DeliveryConfirm(channel ch) {
        super(ch);
    }

    @Override
    double cost() {
        return ch.cost() + 5;
    }

}

public class p6 {
    public static void main(String[] args) {
        channel ch = new PushChannel(new MaintenanceAlert());
        System.out.println(ch.cost());
        channel ch2 = new DeliveryConfirm(new PriortyFlag(new VoiceChannel(new securityalert())));
        System.out.println(ch2.cost());

    }
}