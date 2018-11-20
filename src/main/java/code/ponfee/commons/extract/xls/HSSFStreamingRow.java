package code.ponfee.commons.extract.xls;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class HSSFStreamingRow implements Row {

    private final List<Cell> cells = new LinkedList<>();
    private final Sheet sheet;
    private final int rowNum;

    public HSSFStreamingRow(Sheet sheet, int rowNum) {
        this.sheet = sheet;
        this.rowNum = rowNum;
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

    @Override
    public Cell getCell(int cellnum) {
        return this.cells.get(cellnum);
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

    public void addCell(Cell cell) {
        this.cells.add(cell);
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
    public Cell getCell(int cellnum, MissingCellPolicy policy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getFirstCellNum() {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getLastCellNum() {
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
