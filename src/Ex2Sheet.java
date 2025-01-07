import java.io.IOException;
import java.util.Map;
// Add your documentation below:

public class Ex2Sheet implements Sheet {
    private Cell[][] table;
    private Map< String, Boolean> isForm;


    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for(int i=0;i<x;i=i+1) {
            for(int j=0;j<y;j=j+1) {
                table[i][j] = new SCell("");
            }
        }
        eval();
    }
    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;
        Cell c = get(x,y);
        if (c != null) {
            ans = c.toString();
        }
        return ans;
    }

    @Override
    public Cell get(int x, int y) {
        if (isIn( x, y)){
            return table[x][y];
        } else {
            return null;
        }
    }

    @Override
    public Cell get(String cords) {
        CellEntry entry = new CellEntry(cords);
        if (entry.isValid() && isIn(entry.getX(), entry.getY())){
            return table [entry.getX()] [entry.getY()];
        }
        Cell ans = null;
        return ans;
    }

    @Override
    public int width() {
        return table.length;
    }
    @Override
    public int height() {
        return table[0].length;
    }
    @Override
    public void set(int x, int y, String s) {
        if (isIn( x, y)) {
            Cell c = new SCell(s);
            table[x][y] = c;
            eval();
        }
    }

    @Override
    public void eval() {
        int[][] depths = depth();
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (get(i,j).getType() == Ex2Utils.FORM) {
                    eval(i, j);
                }
            }
        }
    }

    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height();
    }

    @Override
    public int[][] depth() {
        int[][] ans = new int[width()][height()];
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                Cell cell = get(i,j);
                ans[i][j] = cell.getOrder();
            }
        }
        return ans;
    }

    @Override
    public void load(String fileName) throws IOException {
        // Add your code here

        /////////////////////
    }

    @Override
    public void save(String fileName) throws IOException {
        // Add your code here

        /////////////////////
    }

    @Override
    public String eval(int x, int y) {
        Cell cell = get(x,y);
        if (cell != null) {
            if (cell.getType() == Ex2Utils.FORM) {
                return String.valueOf(SCell.computeForm(cell.getData()));
            }
            return cell.getData();
        }
        return null;
    }
}
