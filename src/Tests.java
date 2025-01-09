import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions .*;

class Tests {


    private Sheet sheet;

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

        assertTrue(SCell.isForm("=+A1+B2"));
        assertTrue(SCell.isForm("=-3+5"));
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

    @BeforeEach
    void setUp() {
        sheet = new Ex2Sheet(10, 10);
    }

    @Test
    void testBasicOperations() {
        sheet.set(0, 0, "5");
        assertEquals("5", sheet.get(0, 0).toString());

        sheet.set(0, 1, "=5+3");
        assertEquals("8.0", sheet.get(0, 1).toString());

        sheet.set(0, 2, "=2*3");
        assertEquals("6.0", sheet.get(0, 2).toString());
    }

    @Test
    void testCellReferences() {
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "=A0");
        assertEquals("5.0", sheet.get(0, 1).toString());

        sheet.set(0, 2, "=A0+2");
        assertEquals("7.0", sheet.get(0, 2).toString());
    }

    @Test
    void testCaseSensitivity() {
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "=a0");  // lowercase reference
        assertEquals("5.0", sheet.get(0, 1).toString());
    }

    @Test
    void testCyclicReferences() {
        sheet.set(0, 0, "=A1");
        sheet.set(0, 1, "=A0");
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.get(0, 0).toString());
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.get(0, 1).toString());
    }

    @Test
    void testComplexFormulas() {
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "3");
        sheet.set(0, 2, "=(A0+A1)*2");
        assertEquals("16.0", sheet.get(0, 2).toString());

        sheet.set(0, 3, "=(2+3)*A0");
        assertEquals("25.0", sheet.get(0, 3).toString());
    }

    @Test
    void testInvalidFormulas() {
        sheet.set(0, 0, "=1++2");
        assertEquals(Ex2Utils.ERR_FORM, sheet.get(0, 0).toString());

        sheet.set(0, 1, "=XX1");  // Invalid cell reference
        assertEquals(Ex2Utils.ERR_FORM, sheet.get(0, 1).toString());
    }

    @Test
    void testSaveAndLoad() throws IOException {
        // Set up some data
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "=A0+3");
        sheet.set(1, 0, "Test");

        // Save to a temporary file
        String tempFile = "test_sheet.csv";
        sheet.save(tempFile);

        // Create a new sheet and load
        Sheet newSheet = new Ex2Sheet(10, 10);
        newSheet.load(tempFile);

        // Verify the data
        assertEquals("5", newSheet.get(0, 0).toString());
        assertEquals("8.0", newSheet.get(0, 1).toString());
        assertEquals("Test", newSheet.get(1, 0).toString());

        // Clean up
        new File(tempFile).delete();
    }
}







