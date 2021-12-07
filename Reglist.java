import java.util.ArrayList;

public class Reglist {
    private static Reglist instance;
    private ArrayList<Register> list;
    private Reglist(){
        this.list=new ArrayList<>();
    }
    public static Reglist getInstance(){
        if(instance==null){
            instance=new Reglist();
        }
        return instance;
    }
    public void add(Register reg){
        this.list.add(reg);
    }
    public Register getreg(String name){
        for(int i=0;i<this.list.size();i++){
            if(name.equals(this.list.get(i).getName())){
                return this.list.get(i);
            }
        }
        return null;
    }
}
