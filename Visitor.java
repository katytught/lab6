import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class Visitor extends calcBaseVisitor<Void>{
    public String results="";
    public int Num=1;
    public boolean isconst=false;
    public boolean isglobal=false;
    public int blabel=0;
    public int clabel=0;
    public int T=0;
    public ArrayList<ArrayList> allconstlist = new ArrayList<ArrayList>();
    public ArrayList<ArrayList> alllist=new ArrayList<ArrayList>();
    public ArrayList<Var> global = new ArrayList<Var>();
    static Integer getnumber(String s){
        int res = 0;
        s = s.toLowerCase(Locale.ROOT);
        if (s.charAt(0)=='0'){
            if(s.length()==1){
                return 0;
            }
            if(s.charAt(1)=='x'||s.charAt(1)=='X'){
                int len = s.length();
                s = s.toLowerCase();
                for (int i=2;i<len;i++){
                    if(s.charAt(i)>='0'&&s.charAt(i)<='9'){
                        res=16*res+ (int) s.charAt(i)-48;
                    }
                    else if(s.charAt(i)>='a'&&s.charAt(i)<='f'){
                        res=16*res +10+ ((int) s.charAt(i)-'a');
                    }
                    else {
                        return res;
                    }
                }
                return res;
            }
            else {
                int len = s.length();
                for(int i=1;i<len;i++){
                    res=8*res+ (int) s.charAt(i)-48;
                }
                return res;
            }
        }
        else if(s.charAt(0)<'0'||s.charAt(0)>'9'){
            return null;
        }
        else {
            return Integer.valueOf(s);
        }
    }
    @Override public Void visitCompUnit(calcParser.CompUnitContext ctx) {
        for(int i=0;i<ctx.decl().size();i++){
            isglobal = true;
            visit(ctx.decl(i));
            isglobal = false;
        }
        visit(ctx.funcDef());
        return null;
    }
    @Override public Void visitFuncDef(calcParser.FuncDefContext ctx) {
        if(ctx.FuncType().getText().equals("int")){
            results+="define dso_local ";
        }
        if(ctx.Ident().getText().equals("main")){
            results+="i32 @main";
        }
        results+="()";
        results+="{\n";
        visit(ctx.block());
        results+="}";
        return null;
    }
    @Override public Void visitBlock(calcParser.BlockContext ctx) {
        ArrayList<Var> newlist = new ArrayList<Var>();
        ArrayList<Var> conlist = new ArrayList<Var>();
        alllist.add(newlist);
        allconstlist.add(conlist);
        for(int i=0;i<ctx.blockItem().size();i++){
            visit(ctx.blockItem(i));
        }
        alllist.remove(newlist);
        allconstlist.remove(conlist);
        return null;
    }
    @Override public Void visitStmt(calcParser.StmtContext ctx) {
        if(ctx.lval()!=null){
            String a=visitLval(ctx.lval());
            if(getnumber(a)!=null){
                System.exit(-1);
            }
            String s=visitExp(ctx.exp());
            VarList list=VarList.getInstance();
            if(alllist.size()>0){
                ArrayList<Var> tlist = alllist.get(alllist.size()-1);
                for(int i=0;i<tlist.size();i++){
                    if(tlist.get(i).getName().equals(ctx.lval().getText())&&tlist.get(i).isInit()&&tlist.get(i).isIsconst()){
                        System.exit(-1);
                    }
                }
            }
            else if(list.getVar(ctx.lval().getText()).isIsconst()&&list.getVar(ctx.lval().getText()).isInit()){
                System.exit(-1);
            }
            else{
                for(int i=0;i<global.size();i++){
                    if(global.get(i).getName().equals(ctx.lval().getText())&&global.get(i).isIsconst()){
                        System.exit(-1);
                    }
                }
            }
            if(alllist.size()>0){
                for(int i=alllist.size()-1;i>=0;i--){
//                    boolean bk=false;
                    ArrayList<Var> tlist = alllist.get(i);
                    for(int j=0;j<tlist.size();j++){
                        if(tlist.get(j).getName().equals(ctx.lval().getText())){
                            tlist.get(j).setInit(true);
                            results+="store i32 "+s+", i32* "+ tlist.get(j).getNum()+"\n";
                            return null;
//                            bk=true;
//                            break;
                        }
                    }
//                    if(bk){
//                        break;
//                    }
                }
                for(int i=0;i<global.size();i++){
                    if(global.get(i).getName().equals(ctx.lval().getText())){
                        global.get(i).setInit(true);
                        results+="store i32 "+s+", i32* "+ global.get(i).getNum()+"\n";
                        return null;
                    }
                }
            }
            else {
                list.getVar(ctx.lval().getText()).setInit(true);
                results+="store i32 "+s+", i32* "+ list.getVar(ctx.lval().getText()).getNum()+"\n";
            }
        }
        else if(ctx.block()!=null){
            visit(ctx.block());
        }
        else if(ctx.getText().startsWith("if")){
            if(ctx.stmt().size()==2){
                int Tleft=++T;
                int Tright=++T;
                int Tmid=++T;
                String s = visitCond(ctx.cond());
                if(getnumber(s)!=null){
                    results+="%"+Num+" = icmp ne i32 "+ s + ", 0"+ "\n";
                    Register reg = new Register();
                    reg.setName("%"+Num);
                    reg.setNum(Num);
                    reg.setType("i1");
                    Reglist.getInstance().add(reg);
                    Num++;
                }
                else if(Reglist.getInstance().getreg(s).getType().equals("i32")){
                results+="%"+Num+" = icmp ne "+Reglist.getInstance().getreg(s).getType() +s + ", 0"+ "\n";
                Register reg = new Register();
                reg.setName("%"+Num);
                reg.setNum(Num);
                reg.setType("i1");
                Reglist.getInstance().add(reg);
                Num++;
                }
                results+="br i1 %"+(Num-1)+", label %t"+Tleft+", label %t"+Tright+"\n";
                results+="t"+Tleft+":\n";
                visit(ctx.stmt(0));
                if(clabel!=-1&&blabel!=-1){
                    results+="br label %t"+Tmid+"\n";
                }
                results+="t"+Tright+":\n";
                visit(ctx.stmt(1));
                if(clabel!=-1&&blabel!=-1){
                    results+="br label %t"+Tmid+"\n";
                }
                results+="t"+Tmid+":\n";
            }
            else if(ctx.stmt().size()==1){
                int Tleft=++T;
                int Tright=++T;
                int Tmid=T;
                String s = visitCond(ctx.cond());
                if(getnumber(s)!=null){
                    results+="%"+Num+" = icmp ne i32 "+ s + ", 0"+ "\n";
                    Register reg = new Register();
                    reg.setName("%"+Num);
                    reg.setNum(Num);
                    reg.setType("i1");
                    Reglist.getInstance().add(reg);
                    Num++;
                }
                else if(Reglist.getInstance().getreg(s).getType().equals("i32")){
                    results+="%"+Num+" = icmp ne "+Reglist.getInstance().getreg(s).getType() +s + ", 0"+ "\n";
                    Register reg = new Register();
                    reg.setName("%"+Num);
                    reg.setNum(Num);
                    reg.setType("i1");
                    Reglist.getInstance().add(reg);
                    Num++;
                }
                results+="br i1 %"+(Num-1)+", label %t"+Tleft+", label %t"+Tmid+"\n";
                results+="t"+Tleft+":\n";
                visit(ctx.stmt(0));
                if(clabel!=-1&&blabel!=-1){
                    results+="br label %t"+Tmid+"\n";
                }
                results+="t"+Tmid+":\n";
            }
        }
        else if(ctx.getText().startsWith("while")){
            int Tleft=++T;
            int Tright=++T;
            int Tmid=++T;
            results+="br label %t"+Tleft+"\n";
            results+="t"+Tleft+":\n";
            String s = visitCond(ctx.cond());
            if(getnumber(s)!=null){
                results+="%"+Num+" = icmp ne i32 "+ s + ", 0"+ "\n";
                Register reg = new Register();
                reg.setName("%"+Num);
                reg.setNum(Num);
                reg.setType("i1");
                Reglist.getInstance().add(reg);
                Num++;
            }
            else if(Reglist.getInstance().getreg(s).getType().equals("i32")){
                results+="%"+Num+" = icmp ne "+Reglist.getInstance().getreg(s).getType() +s + ", 0"+ "\n";
                Register reg = new Register();
                reg.setName("%"+Num);
                reg.setNum(Num);
                reg.setType("i1");
                Reglist.getInstance().add(reg);
                Num++;
            }
            results+="br i1 %"+(Num-1)+", label %t"+Tright+", label %t"+Tmid+"\n";
            results+="t"+Tright+":\n";
            blabel=Tmid;
            clabel=Tleft;
            visit(ctx.stmt(0));
            blabel=0;
            clabel=0;
            results+="br label %t"+Tleft+"\n";
            results+="t"+Tmid+":\n";
        }
        else if(ctx.getText().startsWith("break")){
            results+="br label %t"+blabel+"\n";
            blabel=-1;
        }
        else if(ctx.getText().startsWith("continue")){
            results+="br label %t"+clabel+"\n";
            clabel=-1;
        }
        else if(ctx.getText().startsWith("return")){
            String s=visitExp(ctx.exp());
            results+="ret i32 "+s+"\n";
            Num++;
        }
        else {
            if(ctx.exp()!=null){
                visit(ctx.exp());
            }
        }
        return null;
    }

    @Override
    public String visitExp(calcParser.ExpContext ctx) {
        return visitAddexp(ctx.addexp());
    }

    @Override
    public String visitAddexp(calcParser.AddexpContext ctx) {
        switch (ctx.children.size()){
            case 1:
                return visitMulexp(ctx.mulexp());
            case 3:
                if(isglobal){
                    String left=visitAddexp(ctx.addexp());
                    String right=visitMulexp(ctx.mulexp());
                    int a=0;
                    int b=0;
                    for(int i=0;i<global.size();i++){
                        if(global.get(i).getName().equals(left)&&!global.get(i).isIsconst()){
                            System.exit(-1);
                        }
                        if(global.get(i).getName().equals(left)){
                            a=global.get(i).getValue();
                            break;
                        }
                        if(i==global.size()-1){
                            a=getnumber(left);
                        }
                    }
                    for(int i=0;i<global.size();i++){
                        if(global.get(i).getName().equals(right)&&!global.get(i).isIsconst()){
                            System.exit(-1);
                        }
                        if(global.get(i).getName().equals(right)){
                            b=global.get(i).getValue();
                            break;
                        }
                        if(i==global.size()-1){
                            b=getnumber(right);
                        }
                    }
                    if(ctx.Addfunc().getText().equals("+")){
                        return String.valueOf(a+b);
                    }else {
                        return String.valueOf(a-b);
                    }
                }
                String left=visitAddexp(ctx.addexp());
                String right=visitMulexp(ctx.mulexp());
                if(Objects.equals(ctx.Addfunc().getText(), "+")){
                    if(left.startsWith("%")&&Reglist.getInstance().getreg(left).getType().equals("i1")){
                        results+="%"+Num+" = "+"zext i1 %"+(Num-1)+" to i32\n";
                        Register reg = new Register();
                        left = "%"+Num;
                        reg.setName("%"+Num);
                        reg.setNum(Num);
                        reg.setType("i32");
                        Reglist.getInstance().add(reg);
                        Num++;
                    }
                    if(right.startsWith("%")&&Reglist.getInstance().getreg(right).getType().equals("i1")){
                        results+="%"+Num+" = "+"zext i1 %"+(Num-1)+" to i32\n";
                        Register reg = new Register();
                        right = "%"+Num;
                        reg.setName("%"+Num);
                        reg.setNum(Num);
                        reg.setType("i32");
                        Reglist.getInstance().add(reg);
                        Num++;
                    }
                    results+="%"+Num+" = add i32 "+left+","+right+"\n";
                    Register reg = new Register();
                    reg.setName("%"+Num);
                    reg.setNum(Num);
                    reg.setType("i32");
                    Reglist.getInstance().add(reg);
                    Num++;
                    return "%"+(Num-1);
                }
                else if(Objects.equals(ctx.Addfunc().getText(), "-")){
                    if(left.startsWith("%")&&Reglist.getInstance().getreg(left).getType().equals("i1")){
                        results+="%"+Num+" = "+"zext i1 %"+(Num-1)+" to i32\n";
                        Register reg = new Register();
                        left = "%"+Num;
                        reg.setName("%"+Num);
                        reg.setNum(Num);
                        reg.setType("i32");
                        Reglist.getInstance().add(reg);
                        Num++;
                    }
                    if(right.startsWith("%")&&Reglist.getInstance().getreg(right).getType().equals("i1")){
                        results+="%"+Num+" = "+"zext i1 %"+(Num-1)+" to i32\n";
                        Register reg = new Register();
                        right = "%"+Num;
                        reg.setName("%"+Num);
                        reg.setNum(Num);
                        reg.setType("i32");
                        Reglist.getInstance().add(reg);
                        Num++;
                    }
                    results+="%"+Num+" = sub i32 "+left+","+right+"\n";
                    Register reg = new Register();
                    reg.setName("%"+Num);
                    reg.setNum(Num);
                    reg.setType("i32");
                    Reglist.getInstance().add(reg);
                    Num++;
                    return "%"+(Num-1);
                }

                break;
        }
        return null;
    }

    @Override
    public String visitMulexp(calcParser.MulexpContext ctx) {
        switch (ctx.children.size()){
            case 1:
                return visitUnaryexp(ctx.unaryexp());
            case 3:
                if(isglobal){
                    String left=visitMulexp(ctx.mulexp());
                    String right=visitUnaryexp(ctx.unaryexp());
                    int a=0;
                    int b=0;
                    for(int i=0;i<global.size();i++){
                        if(global.get(i).getName().equals(left)&&!global.get(i).isIsconst()){
                            System.exit(-1);
                        }
                        if(global.get(i).getName().equals(left)){
                            a=global.get(i).getValue();
                            break;
                        }
                        if(i==global.size()-1){
                            a=getnumber(left);
                        }
                    }
                    for(int i=0;i<global.size();i++){
                        if(global.get(i).getName().equals(right)&&!global.get(i).isIsconst()){
                            System.exit(-1);
                        }
                        if(global.get(i).getName().equals(right)){
                            b=global.get(i).getValue();
                            break;
                        }
                        if(i==global.size()-1){
                            b=getnumber(right);
                        }
                    }
                    if(ctx.Mulfunc().getText().equals("*")){
                        return String.valueOf(a*b);
                    }else if(ctx.Mulfunc().getText().equals("/")){
                        return String.valueOf(a/b);
                    }else {
                        return String.valueOf(a%b);
                    }
                }
                String left=visitMulexp(ctx.mulexp());
                String right=visitUnaryexp(ctx.unaryexp());
                if(ctx.Mulfunc().getText().equals("*")){
                    if(left.startsWith("%")&&Reglist.getInstance().getreg(left).getType().equals("i1")){
                        results+="%"+Num+" = "+"zext i1 %"+(Num-1)+" to i32\n";
                        Register reg = new Register();
                        left = "%"+Num;
                        reg.setName("%"+Num);
                        reg.setNum(Num);
                        reg.setType("i32");
                        Reglist.getInstance().add(reg);
                        Num++;
                    }
                    if(right.startsWith("%")&&Reglist.getInstance().getreg(right).getType().equals("i1")){
                        results+="%"+Num+" = "+"zext i1 %"+(Num-1)+" to i32\n";
                        Register reg = new Register();
                        right = "%"+Num;
                        reg.setName("%"+Num);
                        reg.setNum(Num);
                        reg.setType("i32");
                        Reglist.getInstance().add(reg);
                        Num++;
                    }
                    results+="%"+Num+" = mul i32 "+left+","+right+"\n";
                    Register reg = new Register();
                    reg.setName("%"+Num);
                    reg.setNum(Num);
                    reg.setType("i32");
                    Reglist.getInstance().add(reg);
                    Num++;
                    return "%"+(Num-1);
                }
                else if(ctx.Mulfunc().getText().equals("/")) {
                    if(left.startsWith("%")&&Reglist.getInstance().getreg(left).getType().equals("i1")){
                        results+="%"+Num+" = "+"zext i1 %"+(Num-1)+" to i32\n";
                        Register reg = new Register();
                        left = "%"+Num;
                        reg.setName("%"+Num);
                        reg.setNum(Num);
                        reg.setType("i32");
                        Reglist.getInstance().add(reg);
                        Num++;
                    }
                    if(right.startsWith("%")&&Reglist.getInstance().getreg(right).getType().equals("i1")){
                        results+="%"+Num+" = "+"zext i1 %"+(Num-1)+" to i32\n";
                        Register reg = new Register();
                        right = "%"+Num;
                        reg.setName("%"+Num);
                        reg.setNum(Num);
                        reg.setType("i32");
                        Reglist.getInstance().add(reg);
                        Num++;
                    }
                    results+="%" + Num + " = sdiv i32 " + left + "," + right+"\n";
                    Register reg = new Register();
                    reg.setName("%"+Num);
                    reg.setNum(Num);
                    reg.setType("i32");
                    Reglist.getInstance().add(reg);
                    Num++;
                    return "%" + (Num - 1);
                }
                else if(ctx.Mulfunc().getText().equals("%")) {
                    if(left.startsWith("%")&&Reglist.getInstance().getreg(left).getType().equals("i1")){
                        results+="%"+Num+" = "+"zext i1 %"+(Num-1)+" to i32\n";
                        Register reg = new Register();
                        left = "%"+Num;
                        reg.setName("%"+Num);
                        reg.setNum(Num);
                        reg.setType("i32");
                        Reglist.getInstance().add(reg);
                        Num++;
                    }
                    if(right.startsWith("%")&&Reglist.getInstance().getreg(right).getType().equals("i1")){
                        results+="%"+Num+" = "+"zext i1 %"+(Num-1)+" to i32\n";
                        Register reg = new Register();
                        right = "%"+Num;
                        reg.setName("%"+Num);
                        reg.setNum(Num);
                        reg.setType("i32");
                        Reglist.getInstance().add(reg);
                        Num++;
                    }
                    results+="%" + Num + " = srem i32 " + left + "," + right+"\n";
                    Register reg = new Register();
                    reg.setName("%"+Num);
                    reg.setNum(Num);
                    reg.setType("i32");
                    Reglist.getInstance().add(reg);
                    Num++;
                    return "%" + (Num - 1);
                }
                break;
        }
        return null;
    }

    @Override
    public String visitUnaryexp(calcParser.UnaryexpContext ctx) {
        switch (ctx.children.size()){
            case 1:
                return visitPrimaryexp(ctx.primaryexp());
            case 2:
                String right=visitUnaryexp(ctx.unaryexp());
                if(ctx.Addfunc().getText().equals("+")){
                    if(Reglist.getInstance().getreg("%"+(Num-1)).getType().equals("i1")){
                        results+="%"+Num+" = "+"zext i1 %"+(Num-1)+" to i32\n";
                        Register reg = new Register();
                        right = "%"+Num;
                        reg.setName("%"+Num);
                        reg.setNum(Num);
                        reg.setType("i32");
                        Reglist.getInstance().add(reg);
                        Num++;
                    }
                    results+="%"+Num+" = add i32 0, "+right+"\n";
                    Register reg = new Register();
                    reg.setName("%"+Num);
                    reg.setNum(Num);
                    reg.setType("i32");
                    Reglist.getInstance().add(reg);
                    Num++;
                    return "%"+(Num-1);
                }
                else if(ctx.Addfunc().getText().equals("-")){
                    if(Reglist.getInstance().getreg("%"+(Num-1)).getType().equals("i1")){
                        results+="%"+Num+" = "+"zext i1 %"+(Num-1)+" to i32\n";
                        Register reg = new Register();
                        right = "%"+Num;
                        reg.setName("%"+Num);
                        reg.setNum(Num);
                        reg.setType("i32");
                        Reglist.getInstance().add(reg);
                        Num++;
                    }
                    results+="%"+Num+" = sub i32 0, "+right+"\n";
                    Register reg = new Register();
                    reg.setName("%"+Num);
                    reg.setNum(Num);
                    reg.setType("i32");
                    Reglist.getInstance().add(reg);
                    Num++;
                    return "%"+(Num-1);
                }
                else if(ctx.Addfunc().getText().equals("!")){
                    results+="%"+Num+" = icmp eq "+Reglist.getInstance().getreg("%"+(Num-1)).getType() +" %" + (Num-1) + ", 0"+ "\n";
                    Register reg = new Register();
                    reg.setName("%"+Num);
                    reg.setNum(Num);
                    reg.setType("i1");
                    Reglist.getInstance().add(reg);
                    Num++;
                    return "%"+(Num-1);
                }
            default:
                String s = ctx.Idigit().getText();
                if(s.equals("getint")){
                    results+="%"+Num+" = call i32 @getint()\n";
                    Register reg = new Register();
                    reg.setName("%"+Num);
                    reg.setNum(Num);
                    reg.setType("i32");
                    Reglist.getInstance().add(reg);
                    Num++;
                    return "%"+(Num-1);
                }
                else if(s.equals("putint")){
                    String tt=visitFuncrParams(ctx.funcrParams());
                    results+="call void @putint(i32 "+tt+")\n";
                    return null;
                }
                else if(s.equals("getch")){
                    results+="%"+Num+" = call i32 @getch()\n";
                    Register reg = new Register();
                    reg.setName("%"+Num);
                    reg.setNum(Num);
                    reg.setType("i32");
                    Reglist.getInstance().add(reg);
                    Num++;
                    return "%"+(Num-1);
                }
                else if(s.equals("putch")){
                    String tt=visitFuncrParams(ctx.funcrParams());
                    results+="call void @putch(i32 "+tt+")\n";
                    return null;
                }
        }
        return null;
    }

    @Override
    public String visitPrimaryexp(calcParser.PrimaryexpContext ctx) {
        switch (ctx.children.size()){
            case 1:
                if(ctx.Number()!=null){
                    String s = ctx.Number().getText();
                    int temp=getnumber(s);
                    return String.valueOf(temp);
                }
                else {
                    String a=visitLval(ctx.lval());
//                    Var var=VarList.getInstance().getVar(ctx.lval().getText());
                    return a;
                }
            case 3:
                return visitExp(ctx.exp());
        }
        return null;
    }

    @Override
    public Void visitDecl(calcParser.DeclContext ctx) {
        if(ctx.constDecl()!=null){
            isconst=true;
            visit(ctx.constDecl());
            isconst=false;
        }
        else {
            visit(ctx.varDecl());
        }
        return null;
    }

    @Override
    public Void visitConstDecl(calcParser.ConstDeclContext ctx) {
        for(int i=0;i<ctx.constDef().size();i++){
            visit(ctx.constDef(i));
        }
        return null;
    }

    @Override
    public Void visitConstDef(calcParser.ConstDefContext ctx) {
        if(isglobal){
            String ident=ctx.Idigit().getText();
            for(int i=0;i<global.size();i++){
                if(global.get(i).getName().equals(ident)){
                    System.exit(-1);
                }
            }
            Var var=new Var();
            var.setName(ident);
            var.setNum("@"+ident);
            var.setIsconst(true);
            global.add(var);
            String temp=visitConstInitVal(ctx.constInitVal());
            var.setValue(getnumber(temp));
            var.setInit(true);
            return null;
        }
        results+="%"+Num+" = alloca i32\n";
        String ident=ctx.Idigit().getText();
        if(alllist.size()>0){
            ArrayList<Var> tlist = alllist.get(alllist.size()-1);
            for(int i=0;i<tlist.size();i++){
                if(tlist.get(i).getName().equals(ident)){
                    System.exit(-1);
                }
            }
        }
        else if(VarList.getInstance().getVar(ident)!=null){
            System.exit(-1);
        }
        Var var=new Var();
        var.setName(ident);
        var.setNum("%"+Num);
        var.setInit(true);
        var.setIsconst(true);
        VarList list=VarList.getInstance();
        if(alllist.size()>0){
            alllist.get(alllist.size()-1).add(var);
        }
        else {
            list.add(var);
        }
        Register reg = new Register();
        reg.setName("%"+Num);
        reg.setNum(Num);
        reg.setType("i32");
        Reglist.getInstance().add(reg);
        Num++;
        String temp=visitConstInitVal(ctx.constInitVal());
        String loc= var.getNum();
        results+="store i32 "+temp+", i32* " +loc+"\n";
        allconstlist.get(allconstlist.size()-1).add(var);
        return null;
    }

    @Override
    public String visitConstInitVal(calcParser.ConstInitValContext ctx) {
        return visitConstExp(ctx.constExp());
    }

    @Override
    public String visitConstExp(calcParser.ConstExpContext ctx) {
        return visitAddexp(ctx.addexp());
    }

    @Override
    public Void visitVarDecl(calcParser.VarDeclContext ctx) {
        for(int i=0;i<ctx.varDef().size();i++){
            visit(ctx.varDef(i));
        }
        return null;
    }

    @Override
    public Void visitVarDef(calcParser.VarDefContext ctx) {
        switch (ctx.children.size()){
            case 1:
                if(isglobal){
                    String ident1=ctx.Idigit().getText();
                    for(int i=0;i<global.size();i++){
                        if(global.get(i).getName().equals(ident1)){
                            System.exit(-1);
                        }
                    }
                    Var var1=new Var();
                    var1.setName(ident1);
                    var1.setIsconst(false);
                    var1.setNum("@"+ident1);
                    var1.setValue(0);
                    global.add(var1);
                    var1.setInit(true);
                    results += "@"+ident1+" = dso_local global i32 " + var1.getValue()+"\n";
                    return null;
                }
                results+="%"+Num+" = alloca i32\n";
                String ident=ctx.Idigit().getText();
                if(alllist.size()>0){
                    ArrayList<Var> tlist = alllist.get(alllist.size()-1);
                    for(int i=0;i<tlist.size();i++){
                        if(tlist.get(i).getName().equals(ident)){
                            System.exit(-1);
                        }
                    }
                }
                else if(VarList.getInstance().getVar(ident)!=null){
                    System.exit(-1);
                }
                Var var=new Var();
                var.setName(ident);
                var.setNum("%"+Num);
                var.setIsconst(false);
                var.setInit(false);
                VarList list=VarList.getInstance();
                if(alllist.size()>0){
                    alllist.get(alllist.size()-1).add(var);
                }
                else {
                    list.add(var);
                }
                Register reg = new Register();
                reg.setName("%"+Num);
                reg.setNum(Num);
                reg.setType("i32");
                Reglist.getInstance().add(reg);
                Num++;
                break;
            case 3:
                if(isglobal){
                    String ident1=ctx.Idigit().getText();
                    for(int i=0;i<global.size();i++){
                        if(global.get(i).getName().equals(ident1)){
                            System.exit(-1);
                        }
                    }
                    Var var1=new Var();
                    var1.setName(ident1);
                    var1.setIsconst(false);
                    var1.setNum("@"+ident1);
                    var1.setValue(0);
                    global.add(var1);
                    String temp=visitInitVal(ctx.initVal());
                    var1.setValue(getnumber(temp));
                    var1.setInit(true);
                    results += "@"+ident1+" = dso_local global i32 " + getnumber(temp)+"\n";
                    return null;
                }
                results+="%"+Num+" = alloca i32\n";
                ident=ctx.Idigit().getText();
                if(alllist.size()>0){
                    ArrayList<Var> tlist = alllist.get(alllist.size()-1);
                    for(int i=0;i<tlist.size();i++){
                        if(tlist.get(i).getName().equals(ident)){
                            System.exit(-1);
                        }
                    }
                }
                else if(VarList.getInstance().getVar(ident)!=null){
                    System.exit(-1);
                }
                var=new Var();
                var.setName(ident);
                var.setNum("%"+Num);
                var.setInit(true);
                var.setIsconst(false);
                list=VarList.getInstance();
                if(alllist.size()>0){
                    alllist.get(alllist.size()-1).add(var);
                }
                else {
                    list.add(var);
                }
                Register reg2 = new Register();
                reg2.setName("%"+Num);
                reg2.setNum(Num);
                reg2.setType("i32");
                Reglist.getInstance().add(reg2);
                Num++;
                String temp=visitInitVal(ctx.initVal());
                String loc = var.getNum();
//                String loc=list.getVar(ctx.Idigit().getText()).getNum();
                results+="store i32 "+temp+", i32* " +loc+"\n";
                break;
        }
        return null;
    }

    @Override
    public String visitInitVal(calcParser.InitValContext ctx) {
        return visitExp(ctx.exp());
    }

    @Override
    public Void visitBlockItem(calcParser.BlockItemContext ctx) {
        if(ctx.decl()!=null){
            visit(ctx.decl());
        }
        else {
            visit(ctx.stmt());
        }
        return null;
    }

    @Override
    public String visitLval(calcParser.LvalContext ctx) {
        Var var=VarList.getInstance().getVar(ctx.getText());;
        for(int i=alllist.size()-1;i>=0;i--){
            ArrayList<Var> tlist = alllist.get(i);
            boolean bk=false;
            for(int j=0;j<tlist.size();j++){
                if(tlist.get(j).getName().equals(ctx.getText())){
                    var = tlist.get(j);
                    bk=true;
                    break;
                }
            }
            if(bk){
                break;
            }
        }
        if(var==null){
            for(int i=0;i<global.size();i++){
                if(global.get(i).getName().equals(ctx.getText())){
                    var = global.get(i);
                    if(isglobal&&!global.get(i).isIsconst()){
                        System.exit(-1);
                    }
                    if(global.get(i).isIsconst()){
                        return String.valueOf(var.getValue());
                    }
                    results+="%"+Num+" = load i32, i32* "+var.getNum()+"\n";
                    Register reg = new Register();
                    reg.setName("%"+Num);
                    reg.setNum(Num);
                    reg.setType("i32");
                    Reglist.getInstance().add(reg);
                    Num++;
                    return reg.getName();
                }
            }
        }
        if(!var.isIsconst()&&isconst){
            System.exit(-1);
        }
        if(var.isInit()){
            results+="%"+Num+" = load i32, i32* "+var.getNum()+"\n";
            Register reg = new Register();
            reg.setName("%"+Num);
            reg.setNum(Num);
            reg.setType("i32");
            Reglist.getInstance().add(reg);
            Num++;
        }
        return "%"+(Num-1);
    }

    @Override
    public String visitFuncrParams(calcParser.FuncrParamsContext ctx) {
        String s = "";
        for(int i=0;i<ctx.exp().size();i++){
            s+=visitExp(ctx.exp(i));
        }
        return s;
    }

    @Override
    public String visitCond(calcParser.CondContext ctx) {
        String s=visitLorexp(ctx.lorexp());
        return s;
    }

    @Override
    public String visitLorexp(calcParser.LorexpContext ctx) {
        switch (ctx.children.size()){
            case 1:
                String s =visitLandexp(ctx.landexp());
                return s;
            case 3:
                String s1 = visitLorexp(ctx.lorexp());
                String s2 = visitLandexp(ctx.landexp());
                results+="%"+Num+" = or i1 "+s1+","+s2+"\n";
                Register reg = new Register();
                reg.setName("%"+Num);
                reg.setNum(Num);
                reg.setType("i1");
                Reglist.getInstance().add(reg);
                Num++;
                return "%"+(Num-1);
        }
        return null;
    }

    @Override
    public String visitLandexp(calcParser.LandexpContext ctx) {
        switch (ctx.children.size()){
            case 1:
                String s = visitEqexp(ctx.eqexp());
                return s;
            case 3:
                String s1 =visitLandexp(ctx.landexp());
                String s2 =visitEqexp(ctx.eqexp());
                results+="%"+Num+" = and i1 "+s1+","+s2+"\n";
                Register reg = new Register();
                reg.setName("%"+Num);
                reg.setNum(Num);
                reg.setType("i1");
                Reglist.getInstance().add(reg);
                Num++;
                return "%"+(Num-1);
        }
        return null;
    }

    @Override
    public String visitEqexp(calcParser.EqexpContext ctx) {
        switch (ctx.children.size()){
            case 1:
                String s = visitRelexp(ctx.relexp());
                return s;
            case 3:
                String t1 = visitEqexp(ctx.eqexp());
                String t2 = visitRelexp(ctx.relexp());
                if(ctx.Judgefunc().getText().equals("==")){
                    results+="%"+Num+" = icmp eq i32 " + t1 + ", "+ t2 + "\n";
                }
                else {
                    results+="%"+Num+" = icmp ne i32 " + t1 + ", "+ t2 + "\n";
                }
                Register reg = new Register();
                reg.setName("%"+Num);
                reg.setNum(Num);
                reg.setType("i1");
                Reglist.getInstance().add(reg);
                Num++;
                return "%"+(Num-1);
        }
        return null;
    }

    @Override
    public String visitRelexp(calcParser.RelexpContext ctx) {
        switch (ctx.children.size()){
            case 1:
                String s = visitAddexp(ctx.addexp());
                return s;
            case 3:
                String s1 = visitRelexp(ctx.relexp());
                String s2 = visitAddexp(ctx.addexp());
                if(ctx.Comfunc().getText().equals("<=")){
                    results+="%"+Num+" = icmp sle i32 " + s1 + ", "+ s2 + "\n";
                }
                else if(ctx.Comfunc().getText().equals(">=")){
                    results+="%"+Num+" = icmp sge i32 " + s1 + ", "+ s2 + "\n";
                }
                else if(ctx.Comfunc().getText().equals("<")){
                    results+="%"+Num+" = icmp slt i32 " + s1 + ", "+ s2 + "\n";
                }
                else if(ctx.Comfunc().getText().equals(">")){
                    results+="%"+Num+" = icmp sgt i32 " + s1 + ", "+ s2 + "\n";
                }
                Register reg = new Register();
                reg.setName("%"+Num);
                reg.setNum(Num);
                reg.setType("i1");
                Reglist.getInstance().add(reg);
                Num++;
                return "%"+(Num-1);
        }
        return null;
    }
}
