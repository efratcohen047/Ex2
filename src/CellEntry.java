public class CellEntry implements Index2D {
    private String index;
    private int x, y;

    public CellEntry(String index) {
        this.index = index;
        parseIndex();
    }

    private void parseIndex() {
        // Reset to invalid state first
        x = Ex2Utils.ERR;
        y = Ex2Utils.ERR;

        if (isValid()) {
            // Get the column letter and convert to number (A=0, B=1, etc)
            char column = index.charAt(0);
            x = column - 'A';

            // Get the row number from the rest of the string
            try {
                y = Integer.parseInt(index.substring(1));
            } catch (NumberFormatException e) {
                x = Ex2Utils.ERR;
                y = Ex2Utils.ERR;
            }
        }
    }

    @Override
    public boolean isValid() {
        if (index == null || index.length() < 2) {
            return false;
        }

        // Check first character is A-Z
        char firstChar = index.charAt(0);
        if (firstChar < 'A' || firstChar > 'Z') {
            return false;
        }

        // Check rest is a valid number 0-99
        try {
            int rowNum = Integer.parseInt(index.substring(1));
            return rowNum >= 0 && rowNum < 100;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return index;
    }
}