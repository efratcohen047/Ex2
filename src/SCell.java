import java.util.ArrayList;

public class SCell implements Cell {
    private String line;
    private int type;

    public SCell(String s) {
        setData(s);
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
    public String toString() {
        if (isForm(getData())) {
            double value = computeForm(getData());
            return String.valueOf(value);
        }
        return getData();
    }

    @Override
    public void setData(String s) {
        line = s;
        if (isNumber(s)) {
            setType(Ex2Utils.NUMBER);
        } else if (isForm(s)) {
            setType(Ex2Utils.FORM);
            // Convert formula to uppercase
            line = s.toUpperCase();
        } else {
            setType(Ex2Utils.TEXT);
        }
    }

    @Override
    public String getData() {
        return line;
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
        // Add your code here
    }

    public boolean isNumber(String txt) {
        boolean flag = false;
        try {
            Double.parseDouble(txt);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTxt(String text) {
        return !isNumber(text) && !isForm(text);
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

        // Ensure balanced parentheses
        return parens == 0;
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }


    // Helper to check if character is allowed in formula
    private static boolean isValidChar(char c) {
        return Character.isDigit(c) ||  // Numbers
                isOperator(c) ||         // Operators
                c == '.' ||              // Decimal point
                c == '(' || c == ')' ||  // Parentheses
                (c >= 'A' && c <= 'Z');  // Cell references
    }


    public static double computeForm(String text) {
        if (!isForm(text)) {
            return -1;
        }

        try {
            String formula = text.substring(1); // Remove '='

            // Handle parentheses first
            while (formula.contains("(")) {
                int start = formula.lastIndexOf("(");
                int end = formula.indexOf(")", start);
                if (end == -1) return -1; // Unmatched parentheses

                // Compute what's inside parentheses
                String inside = formula.substring(start + 1, end);
                double innerResult = evaluateExpression(inside);

                // Replace parentheses and their content with the result
                formula = formula.substring(0, start) + innerResult + formula.substring(end + 1);
            }

            return evaluateExpression(formula);
        } catch (Exception e) {
            return -1;
        }
    }

    private static double evaluateExpression(String expr) {
        try {
            // If it's just a number, return it
            if (expr.matches("-?\\d+(\\.\\d+)?")) {
                return Double.parseDouble(expr);
            }

            // Find the rightmost operator with lowest precedence
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

                return operator == '*' ? left * right : left / right;
            }

            // If we get here, something went wrong
            return -1;

        } catch (Exception e) {
            return -1;
        }
    }


    public static class computForm {
        private ArrayList<Double> priorities;
        private String formula;
        private final double MULTIPLY_PRIORITY = 0.1;
        private final double ADD_PRIORITY = 0.5;

        public computForm(String formula) {
            this.formula = formula;
            this.priorities = new ArrayList<>();
            mapPriorities();
        }

        private void mapPriorities() {
            for (int i = 0; i < formula.length(); i++) {
                char c = formula.charAt(i);
                if (Character.isDigit(c) || c == '.') {
                    priorities.add(null);
                } else if (c == '(' || c == ')') {
                    priorities.add(null);
                } else {
                    if (c == '+' || c == '-') {
                        priorities.add(ADD_PRIORITY + i);
                    } else if (c == '*' || c == '/') {
                        priorities.add(MULTIPLY_PRIORITY + i);
                    }
                }
            }
        }

        private int findNextOperation() {
            double minPriority = Double.MAX_VALUE;
            int minIndex = -1;

            for (int i = 0; i < priorities.size(); i++) {
                if (priorities.get(i) != null && priorities.get(i) < minPriority) {
                    minPriority = priorities.get(i);
                    minIndex = i;
                }
            }
            return minIndex;
        }

        public double calculate() {
            // Handle parentheses first
            if (formula.contains("(")) {
                int start = formula.lastIndexOf("(");
                int end = formula.indexOf(")", start);
                if (end == -1) return -1; // Unmatched parentheses

                String inner = formula.substring(start + 1, end);
                computForm innerCalc = new computForm(inner);
                double innerResult = innerCalc.calculate();

                String newFormula = formula.substring(0, start) +
                        innerResult +
                        formula.substring(end + 1);
                return new computForm(newFormula).calculate();
            }

            if (formula.length() == 0) return 0;

            try {
                return Double.parseDouble(formula);
            } catch (NumberFormatException e) {
                // Continue with operation processing
            }

            int opIndex = findNextOperation();
            if (opIndex == -1) return Double.parseDouble(formula);

            double result = getResult(opIndex);

            String newFormula = formula.substring(0, opIndex - 1) +
                    result +
                    formula.substring(opIndex + 2);

            computForm newCalc = new computForm(newFormula);
            return newCalc.calculate();
        }


        private double getResult(int opIndex) {
            try {
                StringBuilder num1Str = new StringBuilder();
                StringBuilder num2Str = new StringBuilder();

                // Get first number
                int i = opIndex - 1;
                while (i >= 0 && (Character.isDigit(formula.charAt(i)) || formula.charAt(i) == '.')) {
                    num1Str.insert(0, formula.charAt(i));
                    i--;
                }

                // Get second number
                i = opIndex + 1;
                while (i < formula.length() && (Character.isDigit(formula.charAt(i)) || formula.charAt(i) == '.')) {
                    num2Str.append(formula.charAt(i));
                    i++;
                }

                double num1 = Double.parseDouble(num1Str.toString());
                double num2 = Double.parseDouble(num2Str.toString());
                char operator = formula.charAt(opIndex);

                switch (operator) {
                    case '+':
                        return num1 + num2;
                    case '-':
                        return num1 - num2;
                    case '*':
                        return num1 * num2;
                    case '/':
                        return num1 / num2;
                    default:
                        return -1;
                }
            } catch (Exception e) {
                return -1;
            }
        }
    }
}















