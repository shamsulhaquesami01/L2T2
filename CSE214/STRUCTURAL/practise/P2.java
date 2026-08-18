package CSE214.STRUCTURAL.practise;

import java.util.ArrayList;
import java.util.List;

interface item{
    void show();
    int size();
}
class file implements item{
    String title;
    int size;
    public file(String title,int size) {
        this.title = title;
        this.size=size;
    }

    @Override
    public void show() {
        System.out.println(this.title);
    }

    @Override
    public int size() {
        return this.size;
    }
    
    
}
class folder implements item{
    String title;
    List<item> lst;
    public folder(String title) {
        this.title = title;
        this.lst = new ArrayList<>();
    }
    @Override
    public void show() {
        for( item i : lst){
            i.show();
        }
    }
    
    @Override
    public int size() {
        int tot =0;
        for(item i:lst){
            tot += i.size();
     
        }
        return tot;
    }
    public void add(item i){
        lst.add(i);
    }
    public void remove(item i){
        lst.remove(i);
    }
}


public class P2 {
    public static void main(String[] args) {
        item a = new file("A.txt", 120);
        folder root =new folder("Root");
        folder docs = new folder("docs");
        item b = new file("B.txt", 300);
        folder sub = new folder("Sub");
        item c= new file("C.txt", 50);
        root.add(a);
        root.add(docs);
        docs.add(b);
        docs.add(sub);
        sub.add(c);

        root.show();
        System.out.println(root.size());

        
    }
}
