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
        assertEquals(5.0, SCell.computeForm("=2+3"));
        assertEquals(14.0, SCell.computeForm("=(2+3)*2+4"));
        assertEquals(13.0, SCell.computeForm("=5+2*4"));
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

    @Test
    void testValidEntries() {
        // Test basic valid entries
        CellEntry cell1 = new CellEntry("A0");
        assertTrue(cell1.isValid());
        assertEquals(0, cell1.getX());
        assertEquals(0, cell1.getY());

        CellEntry cell2 = new CellEntry("B5");
        assertTrue(cell2.isValid());
        assertEquals(1, cell2.getX());
        assertEquals(5, cell2.getY());

        // Test boundary cases
        CellEntry cell3 = new CellEntry("Z99");
        assertTrue(cell3.isValid());
        assertEquals(25, cell3.getX());
        assertEquals(99, cell3.getY());
    }

    @Test
    void testInvalidEntries() {
        // Test null
        CellEntry cell1 = new CellEntry(null);
        assertFalse(cell1.isValid());
        assertEquals(Ex2Utils.ERR, cell1.getX());
        assertEquals(Ex2Utils.ERR, cell1.getY());

        // Test empty string
        CellEntry cell2 = new CellEntry("");
        assertFalse(cell2.isValid());
        assertEquals(Ex2Utils.ERR, cell2.getX());
        assertEquals(Ex2Utils.ERR, cell2.getY());

        // Test invalid column
        CellEntry cell3 = new CellEntry("123");
        assertFalse(cell3.isValid());
        assertEquals(Ex2Utils.ERR, cell3.getX());
        assertEquals(Ex2Utils.ERR, cell3.getY());

        // Test invalid row
        CellEntry cell4 = new CellEntry("A100");
        assertFalse(cell4.isValid());
        assertEquals(Ex2Utils.ERR, cell4.getX());
        assertEquals(Ex2Utils.ERR, cell4.getY());

        // Test invalid format
        CellEntry cell5 = new CellEntry("AA1");
        assertFalse(cell5.isValid());
        assertEquals(Ex2Utils.ERR, cell5.getX());
        assertEquals(Ex2Utils.ERR, cell5.getY());
    }

    @Test
    void testToString() {
        CellEntry cell = new CellEntry("A1");
        assertEquals("A1", cell.toString());
    }

    @Test
    void testBoundaryValues() {
        // Test first cell
        CellEntry first = new CellEntry("A0");
        assertTrue(first.isValid());
        assertEquals(0, first.getX());
        assertEquals(0, first.getY());

        // Test last valid cell
        CellEntry last = new CellEntry("Z99");
        assertTrue(last.isValid());
        assertEquals(25, last.getX());
        assertEquals(99, last.getY());

        // Test just beyond valid range
        CellEntry tooHigh = new CellEntry("Z100");
        assertFalse(tooHigh.isValid());
        assertEquals(Ex2Utils.ERR, tooHigh.getX());
        assertEquals(Ex2Utils.ERR, tooHigh.getY());

        CellEntry invalidCol = new CellEntry("[0");  // Character after 'Z'
        assertFalse(invalidCol.isValid());
        assertEquals(Ex2Utils.ERR, invalidCol.getX());
        assertEquals(Ex2Utils.ERR, invalidCol.getY());
    }

}





