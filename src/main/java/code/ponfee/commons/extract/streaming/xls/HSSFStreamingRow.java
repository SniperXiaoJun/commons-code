package code.ponfee.commons.extract.streaming.xls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * The version for 2003 or early XSL excel file 
 * streaming reader
 * 
 * excel row
 * 
 * @author Ponfee
 */
public class HSSFStreamingRow implements Row {

    private final List<Cell> cells = new ArrayList<>();
    private final Sheet sheet;
    private final int rowNum; // excel row number
    private final int rowOrder; // excel row order

    public HSSFStreamingRow(Sheet sheet, int rowNum, int rowOrder) {
        this.sheet = sheet;
        this.rowNum = rowNum;
        this.rowOrder = rowOrder;
    }

    @Override
    public Iterator<Cell> iterator() {
        return this.cells.iterator();
    }

    @Override
    public void removeCell(Cell cell) {
        this.cells.remove(cell);
    }

    @Override
    public int getRowNum() {
        return this.rowNum;
    }

    public int getRowOrder() {
        return this.rowOrder;
    }

    @Override
    public Cell getCell(int cellnum) {
        return this.cells.get(cellnum);
    }

    @Override
    public Cell getCell(int cellnum, MissingCellPolicy policy) {
        return getCell(cellnum);
    }

    @Override
    public short getLastCellNum() {
        return (short) (cells.size() - 1);
    }

    @Override
    public int getPhysicalNumberOfCells() {
        return this.cells.size();
    }

    @Override
    public Iterator<Cell> cellIterator() {
        return iterator();
    }

    @Override
    public Sheet getSheet() {
        return this.sheet;
    }

    public void addCell(int index, Cell cell) {
        int size;
        if (index == (size = this.cells.size())) {
            this.cells.add(cell);
        } else if (index < size) {
            this.cells.set(index, cell);
        } else {
            for (int i = size; i < index; i++) {
                this.cells.add(null);
            }
            this.cells.add(cell);
        }
    }

    public boolean isEmpty() {
        return this.cells.isEmpty();
    }

    // ----------------------------------------------unsupported operation
    @Override
    public Cell createCell(int column) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Cell createCell(int column, CellType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRowNum(int rowNum) {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getFirstCellNum() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeight(short height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setZeroHeight(boolean zHeight) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getZeroHeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeightInPoints(float height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getHeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getHeightInPoints() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFormatted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CellStyle getRowStyle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRowStyle(CellStyle style) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getOutlineLevel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shiftCellsRight(int firstShiftColumnIndex, int lastShiftColumnIndex, int step) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shiftCellsLeft(int firstShiftColumnIndex, int lastShiftColumnIndex, int step) {
        throw new UnsupportedOperationException();
    }

}
