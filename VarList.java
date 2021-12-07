import java.util.ArrayList;

public class VarList {
    private static VarList instance;
    private ArrayList<Var> list;
    private VarList(){
        this.list=new ArrayList<>();
    }
    public static VarList getInstance(){
        if(instance==null){
            instance=new VarList();
        }
        return instance;
    }
    public void add(Var var){
        this.list.add(var);
    }
    public Var getVar(String a){
        for(int i=0;i<this.list.size();i++){
            if(a.equals(this.list.get(i).getName())){
                return this.list.get(i);
            }
        }
        return null;
    }
}
