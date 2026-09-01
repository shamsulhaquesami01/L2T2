package other_section;

import java.util.ArrayList;
import java.util.List;

interface policy {
    void execute(List<task> lst);
}
class task
{
    int id;
    int start;
    int end;
    String prior;
    int time;

    public task(int id,int start,int end,String prior){
        this.id=id;
        this.start=start;
        this.end=end;
        this.prior=prior;
        this.time=end-start;
    }

    public int getId() {
        return id;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getPrior() {
        return prior;
    }

    public int getTime() {
        return time;
    }
    
}
// ===== Concrete strategies: executeing =====
class FCFS implements policy {
    public void execute(List<task> lst) { 
        task early=lst.get(0);
        for(task t:lst){
            if(t.getStart()<early.getStart()){
                early=t;
            }
        }
        lst.remove(early);

        System.out.println(early.getId());
        System.out.println("FCFS");
    }
}

class priority implements policy {
    private int getPriorityRank(String p) {
        if ("HIGH".equalsIgnoreCase(p)) return 3;
        if ("MEDIUM".equalsIgnoreCase(p)) return 2;
        if ("LOW".equalsIgnoreCase(p)) return 1;
        return 0;
    }
    public void execute(List<task> lst) { 
        task early=lst.get(0);

        for (task t : lst) {
            int currentRank = getPriorityRank(t.getPrior());
            int bestRank = getPriorityRank(early.getPrior());

            if (currentRank > bestRank) {
                early = t;
            } else if (currentRank == bestRank) {
                if (t.getStart() < early.getStart()) {
                    early = t;
                }
            }
        }
        System.out.println(early.getId());
        System.out.println("priority schedule");
        lst.remove(early);

    }
}

class sjf implements policy {
    public void execute(List<task> lst) { 
        task early=lst.get(0);
        int less=1000000;
        for(task t:lst){
            if(t.getTime()<less){
                less=t.getTime();
                early=t;
            }else if (t.getTime() == early.getTime()) {
                if (t.getStart() < early.getStart()) {
                    early = t;
                }
            }
        }
        System.out.println(early.getId());
        System.out.println("SJF");
        lst.remove(early);
     }
}


// ===== Context: composes behaviors instead of inheriting/overriding them =====
 class Scheduler {
    protected policy policy;
    List<task> lst = new ArrayList<>();

    public Scheduler(policy p){
        this.policy=p;
    }


    public void setpolicy(policy fb)     { this.policy = fb; }
    public void addTask(task t){
        lst.add(t);
    }
    public void executeNextTask(){
        int ispriorty=0;
        for(task t:lst){
            if(t.getPrior().equals("HIGH")){
                this.setpolicy(new priority());
                ispriorty=1;
                break;
            }
        }
        int count =0;
         for(task t:lst){
         
            if(t.getTime()<=3){
                count++;
            }
        }
       policy activePolicy;
        if (ispriorty==1) {
            activePolicy = new priority();
        } else if (count >= 3) { 
            activePolicy = new sjf();
        } else {
            activePolicy = this.policy;
        }

        activePolicy.execute(lst);
    }
    public void executeAll(){
        while(!lst.isEmpty()){
            this.executeNextTask();
        }
    }


}
public class b2_today {
    public static void main(String[] args) {
        task t1= new task(1, 0, 8, "MEDIUM");
        task t2= new task(2, 1, 4, "LOW");
        task t3= new task(3, 2, 4, "MEDIUM");
        task t4= new task(4, 3, 4, "LOW");
        task t5= new task(5, 4, 9, "HIGH");

        Scheduler sc = new Scheduler(new FCFS());
        sc.addTask(t1);
        sc.addTask(t3);
        sc.addTask(t4);
        sc.addTask(t5);
        sc.addTask(t2);
        sc.executeAll();
    }
}
