import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        ArrayList<Integer> l1= new ArrayList<Integer>();
        l1.add(1);
        l1.add(2);
        ArrayList<Integer> l2=new ArrayList<Integer>(l1);
        l2.add(3);
        System.out.println(l1);
        System.out.println(l2);
    }
}
