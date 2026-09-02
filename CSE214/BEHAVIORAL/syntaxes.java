
import java.util.*;

class Student {
    int age;
    String name;

    public Student(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

}

public class syntaxes {
    public static void main(String[] args) {
        HashMap<String, Integer> marks = new HashMap<>();
        marks.put("sami", 90);
        int xyz = marks.get("sami");
        if (marks.containsKey("sami"))
            ;
        for (Map.Entry<String, Integer> m : marks.entrySet()) {
            m.getKey();
            m.getValue();
        }

        Stack<String> st = new Stack<>();
        st.push("sami");
        st.pop();

        ArrayList<String> names = new ArrayList<>();
        names.add("Rahim");
        names.get(0);
        names.set(1, "Karim");
        names.remove(0);
        names.remove("Karim");
        names.size();
        names.isEmpty();
        names.contains("Sami");
        names.clear();

        //sorting ******

        names.sort(null);
        names.sort(Collections.reverseOrder());

        ArrayList<Student> lst = new ArrayList<>();
        lst.sort(Comparator.comparing(Student::getAge));

        int[] arr = { 5, 6, 87, 89, 123 };
        Arrays.sort(arr);
        ////********* */
        

        String s = "Sami";
        s.length();
        s.charAt(0);
        s.substring(1);
        s.contains("abc");
        s.startsWith("A");
        s.endsWith("!");
        s.toLowerCase();
        s.toUpperCase();
        s.trim();
        s.split(",");
        s.compareTo("other");

        if (s.equalsIgnoreCase("sami"))
            ;

        Scanner sc = new Scanner(System.in);
        double x = sc.nextDouble();
        int n = sc.nextInt();
        sc.nextLine(); // gap line break
        String line = sc.nextLine();

        Math.max(10, 20);
        Math.min(10, 20);
        Math.abs(x);
        Math.pow(10, 20);
        Math.sqrt(x);
        int i = Integer.parseInt("123");
        double dd = Double.parseDouble("3.14");
        String ss = String.valueOf(x);

        enum Priority {
            HIGH, MEDIUM, LOW
        }
        Priority p = Priority.HIGH;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        
            Thread.currentThread().interrupt();
        }
        
        
    }
}
