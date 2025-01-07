// Add your documentation below:

public class CellEntry  implements Index2D {
    private String index;
    private int x, y;

    public CellEntry(String index){
        this.index = index.toUpperCase();
        parseIndex();
    }

    private void parseIndex(){
        if (isValid()){
            char column = index.charAt(0);
            String row = index.substring(1);
            x = column - 'A';
            y = Integer.parseInt(row);
        } else {
            x = -1;
            y = -1;
        }
    }

    @Override
    public String toString(){
        return index;
    }

    @Override
    public boolean isValid() {
        if (index == null || index.length() < 2) {
            return false;
        }
        char column = index.charAt(0);
        String row = index.substring(1);
        if (!(column >= 'A' && column <= 'Z')) {
            return false;
        }
        try {
            int rowNumber = Integer.parseInt(row);
            return rowNumber >= 0 && rowNumber <= 99;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public int getX() {return Ex2Utils.ERR;}

    @Override
    public int getY() {return Ex2Utils.ERR;}
}
