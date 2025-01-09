import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;
    private Set<String> processingCells;  // Add this line

    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for(int i = 0; i < x; i++) {
            for(int j = 0; j < y; j++) {
                table[i][j] = new SCell("");
            }
        }
        processingCells = new HashSet<>();  // Add this line
        eval();
    }


    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    private String evaluateFormula(String formula, int currentX, int currentY) {
        if (!formula.startsWith("=")) {
            return formula;
        }

        // Remove the '=' sign
        String rawFormula = formula.substring(1);

        // Check for cycles
        String cellKey = currentX + "," + currentY;
        if (processingCells.contains(cellKey)) {
            get(currentX, currentY).setType(Ex2Utils.ERR_CYCLE_FORM);
            return Ex2Utils.ERR_CYCLE;
        }

        try {
            processingCells.add(cellKey);

            // If it's just a cell reference like "=A1"
            if (rawFormula.matches("[A-Z][0-9]+")) {
                String value = evaluateCellReference(rawFormula);
                // If the value is numeric, return it as is
                try {
                    double numValue = Double.parseDouble(value);
                    return value;
                } catch (NumberFormatException e) {
                    return Ex2Utils.ERR_FORM;
                }
            }

            // Replace cell references with their values
            StringBuilder processedFormula = new StringBuilder("=");
            StringBuilder token = new StringBuilder();

            for (int i = 0; i < rawFormula.length(); i++) {
                char c = rawFormula.charAt(i);

                if (Character.isLetter(c) && i + 1 < rawFormula.length() &&
                        Character.isDigit(rawFormula.charAt(i + 1))) {
                    // Found potential cell reference
                    token.append(c);
                    while (i + 1 < rawFormula.length() && Character.isDigit(rawFormula.charAt(i + 1))) {
                        token.append(rawFormula.charAt(++i));
                    }

                    String cellValue = evaluateCellReference(token.toString());
                    if (cellValue.equals(Ex2Utils.ERR_CYCLE) || cellValue.equals(Ex2Utils.ERR_FORM)) {
                        return cellValue;
                    }

                    processedFormula.append(cellValue);
                    token.setLength(0);
                } else {
                    processedFormula.append(c);
                }
            }

            // Now compute the formula with replaced values
            double result = SCell.computeForm(processedFormula.toString());
            if (result == -1) {
                return Ex2Utils.ERR_FORM;
            }
            return String.valueOf(result);

        } catch (Exception e) {
            return Ex2Utils.ERR_FORM;
        } finally {
            processingCells.remove(cellKey);
        }
    }

    private String evaluateCellReference(String ref) {
        // Convert cell reference (e.g., "A1") to coordinates
        ref = ref.toUpperCase();
        int col = ref.charAt(0) - 'A';
        int row = Integer.parseInt(ref.substring(1));

        if (!isIn(col, row)) {
            return Ex2Utils.ERR_FORM;
        }

        Cell cell = get(col, row);
        if (cell == null || cell.getData().isEmpty()) {
            return "0";
        }

        // If the referenced cell is a number, return it directly
        if (cell.getType() == Ex2Utils.NUMBER) {
            return cell.getData();
        }

        // If the referenced cell is a formula, evaluate it
        if (cell.getType() == Ex2Utils.FORM) {
            return eval(col, row);
        }

        // For text cells, try to parse as number, return 0 if not possible
        try {
            Double.parseDouble(cell.getData());
            return cell.getData();
        } catch (NumberFormatException e) {
            return "0";
        }
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
        processingCells.clear();
        // First, mark any cycles
        int[][] depths = depth();

        // Then evaluate all cells
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (get(i, j).getType() == Ex2Utils.FORM) {
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
        // Clear existing data
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                table[i][j] = new SCell("");
            }
        }

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = reader.readLine(); // Skip header line

        while ((line = reader.readLine()) != null) {
            // Split by comma, but not commas inside quotes
            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            if (parts.length >= 3) {
                try {
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());
                    String value = parts[2].trim();

                    if (isIn(x, y)) {
                        set(x, y, value);
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid lines
                    continue;
                }
            }
        }
        reader.close();
        eval(); // Evaluate all formulas after loading
    }


    @Override
    public void save(String fileName) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(fileName));
        writer.println("SpreadSheet (Ex2)- saved spreadsheet");

        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                Cell cell = get(x, y);
                if (cell != null && !cell.getData().isEmpty()) {
                    // Format: x,y,value[,optional remarks]
                    writer.println(x + "," + y + "," + cell.getData());
                }
            }
        }
        writer.close();
    }

    @Override
    public String eval(int x, int y) {
        if (!isIn(x, y)) {
            return null;
        }

        Cell cell = get(x, y);
        if (cell == null) {
            return null;
        }

        if (cell.getType() == Ex2Utils.FORM) {
            String result = evaluateFormula(cell.getData(), x, y);
            // If evaluation resulted in an error, set the cell type accordingly
            if (result.equals(Ex2Utils.ERR_CYCLE)) {
                cell.setType(Ex2Utils.ERR_CYCLE_FORM);
            } else if (result.equals(Ex2Utils.ERR_FORM)) {
                cell.setType(Ex2Utils.ERR_FORM_FORMAT);
            }
            return result;
        }

        return cell.getData();
    }

    private String evaluateFormula(String formula) {
        if (!formula.startsWith("=")) {
            return formula;
        }

        formula = formula.substring(1); // Remove '='

        try {
            // Handle cell references
            if (formula.matches("[A-Za-z][0-9]+")) {
                Cell referencedCell = get(formula);
                if (referencedCell != null) {
                    return referencedCell.toString();
                }
                return Ex2Utils.ERR_FORM;
            }

            // Replace cell references with their values
            StringBuilder processedFormula = new StringBuilder("=");
            StringBuilder currentToken = new StringBuilder();

            for (int i = 0; i < formula.length(); i++) {
                char c = formula.charAt(i);

                if (Character.isLetter(c) && i + 1 < formula.length() && Character.isDigit(formula.charAt(i + 1))) {
                    // Found potential cell reference
                    currentToken.append(c);
                    while (i + 1 < formula.length() && Character.isDigit(formula.charAt(i + 1))) {
                        currentToken.append(formula.charAt(++i));
                    }

                    String cellRef = currentToken.toString();
                    Cell referencedCell = get(cellRef);
                    if (referencedCell != null) {
                        processedFormula.append(referencedCell.toString());
                    } else {
                        return Ex2Utils.ERR_FORM;
                    }
                    currentToken.setLength(0);
                } else {
                    processedFormula.append(c);
                }
            }

            return String.valueOf(SCell.computeForm(processedFormula.toString()));
        } catch (Exception e) {
            return Ex2Utils.ERR_FORM;
        }
    }

}
