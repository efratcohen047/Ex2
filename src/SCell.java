public class SCell implements Cell {
    private String data;  // Use only this variable, remove 'line'
    private int type;
    private int order;
    private Sheet parent;
    private boolean isProcessing;

    public SCell(String s) {
        setData(s);
        isProcessing = false;
    }

    public void setSheet(Sheet sheet) {
        this.parent = sheet;
    }

    @Override
    public int getOrder() {
        if (isNumber(getData())) {
            return 0;    // Numbers get order 0
        } else if (isForm(getData())) {
            if (isProcessing) {
                return -1;  // Cycle detected
            }
            return 1;    // Formulas get order 1
        }
        return 0;        // Text gets order 0
    }

    @Override
    public void setData(String s) {
        data = s;
        if (isNumber(s)) {
            setType(Ex2Utils.NUMBER);
        } else if (isForm(s)) {
            // First convert formula to uppercase
            data = s.toUpperCase();
            // Try to evaluate it immediately to set the correct error state
            setType(Ex2Utils.FORM);
            String result = evalForm();
            if (result.equals(Ex2Utils.ERR_FORM)) {
                setType(Ex2Utils.ERR_FORM_FORMAT);
            } else if (result.equals(Ex2Utils.ERR_CYCLE)) {
                setType(Ex2Utils.ERR_CYCLE_FORM);
            }
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
        if (type == Ex2Utils.FORM ||
                type == Ex2Utils.ERR_FORM_FORMAT ||
                type == Ex2Utils.ERR_CYCLE_FORM) {
            try {
                String result = evalForm();
                // Return error messages for error states
                if (type == Ex2Utils.ERR_FORM_FORMAT) {
                    return Ex2Utils.ERR_FORM;
                }
                if (type == Ex2Utils.ERR_CYCLE_FORM) {
                    return Ex2Utils.ERR_CYCLE;
                }
                return result;
            } catch (Exception e) {
                type = Ex2Utils.ERR_FORM_FORMAT;
                return Ex2Utils.ERR_FORM;
            }
        }
        return data;
    }

    private String evalForm() {
        if (isProcessing) {
            type = Ex2Utils.ERR_CYCLE_FORM;
            return Ex2Utils.ERR_CYCLE;
        }

        isProcessing = true;
        try {
            String processedFormula = processCellRef(data);

            if (processedFormula.equals(Ex2Utils.ERR_FORM)) {
                type = Ex2Utils.ERR_FORM_FORMAT;
                return Ex2Utils.ERR_FORM;
            }
            if (processedFormula.equals(Ex2Utils.ERR_CYCLE)) {
                type = Ex2Utils.ERR_CYCLE_FORM;
                return Ex2Utils.ERR_CYCLE;
            }

            double value = computeForm(processedFormula);
            if (value == -1 && !processedFormula.equals("=-1")) {
                type = Ex2Utils.ERR_FORM_FORMAT;
                return Ex2Utils.ERR_FORM;
            }

            return String.valueOf(value);
        } finally {
            isProcessing = false;
        }
    }



    private String processCellRef(String formula) {
        if (parent == null || !formula.startsWith("=")) return formula;

        StringBuilder processed = new StringBuilder("=");
        StringBuilder cellRef = new StringBuilder();
        boolean buildingRef = false;

        // Skip the '=' sign
        for (int i = 1; i < formula.length(); i++) {
            char c = Character.toUpperCase(formula.charAt(i));  // Convert to uppercase immediately

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
                String ref = cellRef.toString();
                Cell referencedCell = parent.get(ref);
                if (referencedCell instanceof SCell && ((SCell)referencedCell).isProcessing) {
                    return Ex2Utils.ERR_CYCLE;
                }

                if (referencedCell != null) {
                    if (referencedCell.getType() == Ex2Utils.NUMBER) {
                        processed.append(referencedCell.getData());
                    } else if (referencedCell.getType() == Ex2Utils.FORM) {
                        String value = referencedCell.toString();
                        if (value.equals(Ex2Utils.ERR_FORM) ||
                                value.equals(Ex2Utils.ERR_CYCLE)) {
                            return value;
                        }
                        // Make sure we're getting a numeric value
                        try {
                            double numValue = Double.parseDouble(value);
                            processed.append(numValue);
                        } catch (NumberFormatException e) {
                            return Ex2Utils.ERR_FORM;
                        }
                    } else {
                        return Ex2Utils.ERR_FORM;
                    }
                } else {
                    return Ex2Utils.ERR_FORM;
                }
                cellRef.setLength(0);
                buildingRef = false;
            }

            processed.append(c);
        }

        // Handle last cell reference if exists
        if (buildingRef) {
            String ref = cellRef.toString();
            Cell referencedCell = parent.get(ref);
            if (referencedCell instanceof SCell && ((SCell)referencedCell).isProcessing) {
                return Ex2Utils.ERR_CYCLE;
            }

            if (referencedCell != null) {
                if (referencedCell.getType() == Ex2Utils.NUMBER) {
                    processed.append(referencedCell.getData());
                } else if (referencedCell.getType() == Ex2Utils.FORM) {
                    String value = referencedCell.toString();
                    if (value.equals(Ex2Utils.ERR_FORM) ||
                            value.equals(Ex2Utils.ERR_CYCLE)) {
                        return value;
                    }
                    // Make sure we're getting a numeric value
                    try {
                        double numValue = Double.parseDouble(value);
                        processed.append(numValue);
                    } catch (NumberFormatException e) {
                        return Ex2Utils.ERR_FORM;
                    }
                } else {
                    return Ex2Utils.ERR_FORM;
                }
            } else {
                return Ex2Utils.ERR_FORM;
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

        // Find operators from right to left, handling precedence
        // First look for addition/subtraction (lower precedence)
        int lastAdd = -1;
        int lastSub = -1;
        int parentheses = 0;

        // Scan from right to left to find the rightmost + or - not in parentheses
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == ')') parentheses++;
            else if (c == '(') parentheses--;
            else if (parentheses == 0) {
                if (c == '+' && (i == 0 || !isOperator(expr.charAt(i-1)))) lastAdd = i;
                else if (c == '-' && (i == 0 || !isOperator(expr.charAt(i-1)))) lastSub = i;
            }
        }

        // Handle addition or subtraction if found
        int opIndex = Math.max(lastAdd, lastSub);
        if (opIndex >= 0) {
            char operator = expr.charAt(opIndex);
            String leftStr = expr.substring(0, opIndex).trim();
            String rightStr = expr.substring(opIndex + 1).trim();

            // Handle case where minus is a unary operator
            if (opIndex == 0 && operator == '-') {
                return -evaluateExpression(rightStr);
            }

            double left = evaluateExpression(leftStr);
            double right = evaluateExpression(rightStr);

            return operator == '+' ? left + right : left - right;
        }

        // If no addition/subtraction, look for multiplication/division
        int lastMul = expr.lastIndexOf('*');
        int lastDiv = expr.lastIndexOf('/');
        opIndex = Math.max(lastMul, lastDiv);

        if (opIndex >= 0) {
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