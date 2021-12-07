public class Var {
    private String name;//变量名
    private String num;//寄存器编号
    private boolean isconst;//是否为const
    private boolean init;//是否已经初始化s
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public boolean isIsconst() {
        return isconst;
    }

    public void setIsconst(boolean isconst) {
        this.isconst = isconst;
    }
}
