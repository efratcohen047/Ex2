import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions .*;

class FormulaValidatorTest {


    @Test
    public void testValidFormula() {

        assertTrue(SCell.isForm("=A500+B2"));
        assertTrue(SCell.isForm("=3+5"));
        assertTrue(SCell.isForm("=A1*(B2+C3)"));
    }

    @Test
    public void testInvalidFormulaContainsSpace() {

        assertFalse(SCell.isForm("=A1 + B2"));
        assertFalse(SCell.isForm("=A1 B2"));
    }

    @Test
    public void testInvalidFormulaStartsWithOperator() {

        assertFalse(SCell.isForm("=+A1+B2"));
        assertFalse(SCell.isForm("=-3+5"));
    }

    @Test
    public void testInvalidFormulaConsecutiveOperators() {

        assertFalse(SCell.isForm("=A1++B2"));
        assertFalse(SCell.isForm("=A1**B2"));
    }

    @Test
    public void testInvalidFormulaUnmatchedParentheses() {

        assertFalse(SCell.isForm("=A1+(B2"));
        assertFalse(SCell.isForm("=A1+B2)"));
        assertFalse(SCell.isForm("=A1+(B2+C3"));
    }

    @Test
    public void testInvalidFormulaInvalidCharacter() {

        assertFalse(SCell.isForm("=A1&+B2"));
        assertFalse(SCell.isForm("=A1@B2"));
    }

    @Test
    public void SCell() {
        // Basic tests
        testFormula("=1+2");      // Should return 3.0
        testFormula("=2*3");      // Should return 6.0
        testFormula("=6/2");      // Should return 3.0
        testFormula("=5-2");      // Should return 3.0

        // Invalid inputs
        testFormula("1+2");       // Should return -1 (no =)
        testFormula("=1 + 2");    // Should return -1 (spaces)
        testFormula("=1++2");     // Should return -1 (invalid operators)
    }

    private static void testFormula(String input) {
        System.out.println("Testing: " + input);
        System.out.println("Result: " + SCell.computeForm(input));
    }


}