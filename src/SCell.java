public class SCell implements Cell {
    private String data;  // Use only this variable, remove 'line'
    private int type;
    private int order;
    private Sheet parent;

    public SCell(String s) {
        setData(s);
    }

    public void setSheet(Sheet sheet) {
        this.parent = sheet;
    }

    @Override
    public int getOrder() {
        if (isNumber(getData())) {
            return 1;    // Numbers get order 1
        } else if (isForm(getData())) {
            return 2;    // Formulas get order 2
        }
        return 0;        // Text gets order 0
    }

    @Override
    public void setData(String s) {
        data = s;  // Use data instead of line
        if (isNumber(s)) {
            setType(Ex2Utils.NUMBER);
        } else if (isForm(s)) {
            setType(Ex2Utils.FORM);
            data = s.toUpperCase();  // Convert formula to uppercase
        } else {
            setType(Ex2Utils.TEXT);
        }
    }

    @Override
    public String getData() {
        return data;  // Use data instead of line
    }

    @Override
    public String toString() {
        if (type == Ex2Utils.FORM) {
            try {
                // First replace cell references with their values
                String processedFormula = processCellReferences(data);
                double value = computeForm(processedFormula);
                if (value == -1 && !processedFormula.equals("=-1")) {
                    return Ex2Utils.ERR_FORM;
                }
                return String.valueOf(value);
            } catch (Exception e) {
                return Ex2Utils.ERR_FORM;
            }
        }
        return data;
    }

    private String processCellReferences(String formula) {
        if (parent == null || !formula.startsWith("=")) return formula;

        StringBuilder processed = new StringBuilder("=");
        StringBuilder cellRef = new StringBuilder();
        boolean buildingRef = false;

        // Skip the '=' sign
        for (int i = 1; i < formula.length(); i++) {
            char c = formula.charAt(i);

            if (Character.isLetter(c) && i + 1 < formula.length() &&
                    Character.isDigit(formula.charAt(i + 1))) {
                buildingRef = true;
                cellRef.append(c);
                continue;
            }

            if (buildingRef && Character.isDigit(c)) {
                cellRef.append(c);
                continue;
            }

            if (buildingRef) {
                // Process the cell reference
                String ref = cellRef.toString().toUpperCase();
                Cell referencedCell = parent.get(ref);
                if (referencedCell != null) {
                    if (referencedCell.getType() == Ex2Utils.NUMBER) {
                        processed.append(referencedCell.getData());
                    } else if (referencedCell.getType() == Ex2Utils.FORM) {
                        String value = referencedCell.toString();
                        if (value.equals(Ex2Utils.ERR_FORM)) {
                            return Ex2Utils.ERR_FORM;
                        }
                        processed.append(value);
                    } else {
                        return Ex2Utils.ERR_FORM;  // Text cells should return ERR_FORM
                    }
                } else {
                    return Ex2Utils.ERR_FORM;  // Invalid reference should return ERR_FORM
                }
                cellRef.setLength(0);
                buildingRef = false;
            }

            processed.append(c);
        }

        // Handle last cell reference if exists
        if (buildingRef) {
            String ref = cellRef.toString().toUpperCase();
            Cell referencedCell = parent.get(ref);
            if (referencedCell != null) {
                if (referencedCell.getType() == Ex2Utils.NUMBER) {
                    processed.append(referencedCell.getData());
                } else if (referencedCell.getType() == Ex2Utils.FORM) {
                    String value = referencedCell.toString();
                    if (value.equals(Ex2Utils.ERR_FORM)) {
                        return Ex2Utils.ERR_FORM;
                    }
                    processed.append(value);
                } else {
                    return Ex2Utils.ERR_FORM;  // Text cells should return ERR_FORM
                }
            } else {
                return Ex2Utils.ERR_FORM;  // Invalid reference should return ERR_FORM
            }
        }

        return processed.toString();
    }


    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public void setOrder(int t) {
        order = t;
    }

    public static double computeForm(String formula) {
        if (!formula.startsWith("=")) return -1;
        formula = formula.substring(1).trim().toUpperCase();

        try {
            // Handle nested parentheses recursively
            while (formula.contains("(")) {
                int start = formula.lastIndexOf("(");
                int end = formula.indexOf(")", start);
                if (end == -1) return -1;

                String innerExpr = formula.substring(start + 1, end);
                double innerResult = evaluateExpression(innerExpr);

                formula = formula.substring(0, start) +
                        innerResult +
                        formula.substring(end + 1);
            }

            return evaluateExpression(formula);
        } catch (Exception e) {
            return -1;
        }
    }

    private static double evaluateExpression(String expr) {
        expr = expr.trim();

        // If it's just a number, return it
        if (expr.matches("-?\\d+(\\.\\d+)?")) {
            return Double.parseDouble(expr);
        }

        // Find the rightmost operator of lowest precedence
        int lastAdd = expr.lastIndexOf('+');
        int lastSub = expr.lastIndexOf('-');
        int lastMul = expr.lastIndexOf('*');
        int lastDiv = expr.lastIndexOf('/');

        // Handle addition/subtraction
        if (lastAdd >= 0 || lastSub >= 0) {
            int opIndex = Math.max(lastAdd, lastSub);
            char operator = expr.charAt(opIndex);

            String leftStr = expr.substring(0, opIndex).trim();
            String rightStr = expr.substring(opIndex + 1).trim();

            double left = evaluateExpression(leftStr);
            double right = evaluateExpression(rightStr);

            return operator == '+' ? left + right : left - right;
        }

        // Handle multiplication/division
        if (lastMul >= 0 || lastDiv >= 0) {
            int opIndex = Math.max(lastMul, lastDiv);
            char operator = expr.charAt(opIndex);

            String leftStr = expr.substring(0, opIndex).trim();
            String rightStr = expr.substring(opIndex + 1).trim();

            double left = evaluateExpression(leftStr);
            double right = evaluateExpression(rightStr);

            if (operator == '*') return left * right;
            if (operator == '/' && right != 0) return left / right;
            return -1; // Division by zero
        }

        return -1; // Invalid expression
    }


    public boolean isNumber(String txt) {
        if (txt == null || txt.isEmpty()) return false;
        try {
            Double.parseDouble(txt);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isForm(String txt) {
        if (txt == null || !txt.startsWith("=")) return false;
        txt = txt.substring(1);

        // Empty formula
        if (txt.isEmpty()) return false;

        // Track parentheses
        int parens = 0;
        char prev = ' ';

        for (int i = 0; i < txt.length(); i++) {
            char c = txt.charAt(i);

            // Check valid characters
            if (!Character.isDigit(c) &&
                    !Character.isLetter(c) &&
                    c != '+' && c != '-' &&
                    c != '*' && c != '/' &&
                    c != '(' && c != ')' &&
                    c != '.') {
                return false;
            }

            // Track parentheses
            if (c == '(') parens++;
            if (c == ')') {
                parens--;
                if (parens < 0) return false;
            }

            // No consecutive operators
            if (isOperator(c) && isOperator(prev)) return false;

            prev = c;
        }

        return parens == 0;
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }
}