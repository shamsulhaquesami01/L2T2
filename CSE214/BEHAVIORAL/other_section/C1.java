package other_section;

import java.util.Scanner;

// ===== Abstract class holding the template method =====
abstract class Hospital {
    private static int count = 0;

    public final void visitpatient(String name) {
        checkin(name);
        record_vitals();
        assessment();
        treatment();
        discharge_sumnmary();
    }

    private void checkin(String name) {
        count++;
        System.out.println("patient name:" + name);
        System.out.println("serial number " + count);
    }

    protected abstract void assessment();

    protected abstract void treatment();

    // steps that MUST be supplied by each subclass
    private void record_vitals() {
        System.out.println("temperature :34 C");
        System.out.println("presure: 120/80 mmHg");
    }

    private void discharge_sumnmary() {
        System.out.println("patient discharged");
    }
}

// ===== Concrete classes: each overrides only the steps that differ =====
class GeneralDept extends Hospital {

    @Override
    void assessment() {
        System.out.println("Doctor performs normal diagnosis");

    }

    @Override
    void treatment() {
        System.out.println("Prescribe standard medicine");

    }

}

class Pediatrics extends Hospital {
    @Override
    void assessment() {
        System.out.println("Doctor checks symptoms by ensuring child comfort leve");

    }

    @Override
    void treatment() {
        System.out.println("Give child-safe medicine, friendly reassurance message");

    }
}

class Emergency extends Hospital {
    @Override
    void assessment() {
        System.out.println("Quick triage check (urgent/non-urgent)");

    }

    @Override
    void treatment() {
        System.out.println("Immediate emergency procedure");

    }
}

public class C1 {
    public static void main(String[] args) {
        Hospital d1 = new GeneralDept();
        d1.visitpatient("Sami");

        System.out.println();
        Hospital d2 = new Pediatrics();
        d2.visitpatient("Akif");
        System.out.println();
        Hospital d3 = new Emergency();
        d3.visitpatient("Rahim");

    }

}
