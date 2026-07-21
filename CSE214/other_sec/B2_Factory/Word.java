package other_sec.B2_Factory;

class Word implements Report {
    @Override
     public void open() {
        System.out.println("Word opened");
    }
        public void generate() {
        System.out.println("word genrated");
    }

}