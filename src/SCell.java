
// Add your documentation below:

import java.util.ArrayList;

public class SCell implements Cell {
    private String line;
    private int type;
    // Add your code here

    public SCell(String s) {
        // Add your code here
        setData(s);
    }

    @Override
    public int getOrder() {
        // Add your code here

        return 0;
        // ///////////////////
    }

    //@Override
    @Override
    public String toString() {
        return getData();
    }

    @Override
    public void setData(String s) {
        // Add your code here
        line = s;
        /////////////////////
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
        // Reject any spaces in formula
        if (txt.contains(" ")) return false;

        // Must start with "="
        if (!txt.startsWith("=")) return false;
        txt = txt.substring(1);

        int balance = 0;  // Track parentheses matching
        char lastChar = ' ';

        for (int i = 0; i < txt.length(); i++) {
            char c = txt.charAt(i);

            // Track opening/closing parentheses
            if (c == '(') balance++;
            if (c == ')') balance--;
            if (balance < 0) return false;  // Too many closing parentheses

            // Operators validation
            if (isOperator(c)) {
                // Can't start/end with operator
                if (i == 0 || i == txt.length() - 1) return false;
                // Can't have consecutive operators
                if (isOperator(lastChar)) return false;
            }

            // Verify only valid characters used
            if (!isValidChar(c)) return false;

            lastChar = c;
        }

        return balance == 0;  // Ensure all parentheses are matched
    }

    // Helper to check operators (+,-,*,/)
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
        boolean isValid = isForm(text);
        if (!isValid) {
            return -1;
        }

        try {
            double number = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
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
                if (Character.isDigit(c)) {
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
            if (formula.length() == 1) {
                return Double.parseDouble(formula);
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
            double num1 = Double.parseDouble(formula.substring(opIndex - 1, opIndex));
            double num2 = Double.parseDouble(formula.substring(opIndex + 1, opIndex + 2));

            double result = 0;
            char operator = formula.charAt(opIndex);

            if (operator == '+') {
                result = num1 + num2;
            } else if (operator == '-') {
                result = num1 - num2;
            } else if (operator == '*') {
                result = num1 * num2;
            } else if (operator == '/') {
                result = num1 / num2;
            }
            return result;
        }

    }
}














