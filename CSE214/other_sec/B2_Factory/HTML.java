package other_sec.B2_Factory;

class HTML implements Report {
    @Override
      public void open() {
        System.out.println("Html opened");
    }
      public void generate() {
        System.out.println("Html genrated");
    }

}