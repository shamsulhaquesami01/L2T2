package other_sec.B2_Factory;

class PDF implements Report {
    @Override
    public void open() {
        System.out.println("PDF opened");
    }
        public void generate() {
        System.out.println("pdf genrated");
    }

}
