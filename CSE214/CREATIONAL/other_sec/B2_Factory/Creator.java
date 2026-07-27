package other_sec.B2_Factory;

public abstract class Creator {
    // The factory method itself
    public abstract Report createReport();
    
    // Optional: Core business logic that uses the product
    public void doWork() {
        Report p = createReport();
        p.open();
        p.generate();
        System.out.println("completed");
    }
}

// 4. Concrete Creators
class Creatorpdf extends Creator {
    @Override
    public Report createReport() {
        return new PDF();
    }
}

class Creatorword extends Creator {
    @Override
    public Report createReport() {
        return new Word();
    }
}
class Creatorhtml extends Creator {
    @Override
    public Report createReport() {
        return new HTML();
    }
}