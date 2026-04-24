public class IdSymbol{

    private String name;
    private TYPE type = TYPE.UNKOWN;
    private int firstdefline = -1;
    private int firstdefcol = -1;


    public IdSymbol(String name){
        this.name = name;
    }

    public IdSymbol(String name, int line, int col){
        this(name);
        this.firstdefline = line;
        this.firstdefcol = col;
    }

    public String name(){
        return this.name;
    }

    public void setType(TYPE newType){

        type = newType;
    }

    public TYPE getType(){

        return type;
    }

    public int[] firstDefCoord(){

        int[] coord = new int[2];

        coord[0] = this.firstdefline;
        coord[1] = this.firstdefcol;

        return coord;
    }

    @Override
    public String toString(){

        return String.format("%s:%s", this.name, this.type);
    }
}