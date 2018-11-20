package code.ponfee.commons.extract.xls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.CellRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;

public class HSSFStreamingWorkbook implements Workbook, HSSFListener, AutoCloseable {

    private volatile boolean readAllSheet = false;

    private int currentSheetIndex = -1; // start with 0
    private HSSFStreamingSheet currentSheet;

    private volatile int currentRowIndex = -1; // start with 0
    private volatile HSSFStreamingRow currentRow;

    private final List<Sheet> sheets = new ArrayList<>();
    private SSTRecord sstrec;

    /**
     * This method listens for incoming records and handles them as required.
     * 
     * @param record the record that was found while reading.
     */
    @Override
    public void processRecord(Record record) {
        if (record instanceof BOFRecord) { // beginning of a sheet or the workbook
            BOFRecord bof = (BOFRecord) record;
            if (bof.getType() == BOFRecord.TYPE_WORKBOOK) {
                // beginning the workbook
            } else if (bof.getType() == BOFRecord.TYPE_WORKSHEET) {
                readAllSheet = true;
                // beginning a sheet
                if (currentSheet != null) {
                    if (currentRow != null) {
                        currentSheet.putRow(currentRow);
                    }
                    currentSheet.end();
                }
                currentRow = null;
                currentSheet = (HSSFStreamingSheet) sheets.get(++currentSheetIndex);
            } else {
                // others uncapture
                System.err.println("Others uncapture BOFRecord " + bof);
            }
        } else if (record instanceof BoundSheetRecord) { // the workbook all of sheet
            BoundSheetRecord bsr = (BoundSheetRecord) record;
            sheets.add(new HSSFStreamingSheet(sheets.size(), bsr.getSheetname()));
        } else if (record instanceof RowRecord) {// batch row loading
            // nothing todo
        } else if (record instanceof SSTRecord) {// store a array of unique strings used in Excel.
            sstrec = (SSTRecord) record;
        } else if (record instanceof CellRecord) {
            CellRecord cellrec = (CellRecord) record;
            if (currentRowIndex != cellrec.getRow()) { // new row
                if (currentRow != null) {
                    currentSheet.putRow(currentRow);
                }
                currentRowIndex = cellrec.getRow();
                currentRow = new HSSFStreamingRow(currentSheet, currentRowIndex);
            }
            currentRow.addCell(new HSSFStreamingCell((int) cellrec.getColumn(), getCellAsString(cellrec)));
        } else {
            //System.err.println("Others uncapture Record " + record);
        }
    }

    public HSSFStreamingWorkbook(InputStream input, ThreadPoolExecutor executor) {
        //new Thread(new Async(this, input)).start();
        executor.submit(new Async(this, input));
    }

    @Override
    public Iterator<Sheet> iterator() {
        whileReadAllSheet();
        return sheets.iterator();
    }

    @Override
    public Iterator<Sheet> sheetIterator() {
        whileReadAllSheet();
        return iterator();
    }

    @Override
    public String getSheetName(int sheet) {
        whileReadAllSheet();
        return sheets.get(sheet).getSheetName();
    }

    @Override
    public int getSheetIndex(String name) {
        whileReadAllSheet();
        for (int i = 0; i < sheets.size(); i++) {
            if (sheets.get(i).getSheetName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getSheetIndex(Sheet sheet) {
        whileReadAllSheet();
        for (int i = 0; i < sheets.size(); i++) {
            if (sheets.get(i) == sheet) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getNumberOfSheets() {
        whileReadAllSheet();
        return sheets.size();
    }

    @Override
    public Sheet getSheetAt(int index) {
        whileReadAllSheet();
        return sheets.size() > index ? sheets.get(index) : null;
    }

    @Override
    public Sheet getSheet(String name) {
        whileReadAllSheet();
        for (Sheet sheet : sheets) {
            if (sheet.getSheetName().equals(name)) {
                return sheet;
            }
        }
        return null;
    }

    private void readEnd() {
        if (currentSheet != null && currentRow != null) {
            currentSheet.putRow(currentRow);
        }
        sheets.stream().forEach(s -> ((HSSFStreamingSheet) s).end());
        readAllSheet = true;
    }

    private String getCellAsString(CellRecord cell) {
        if (cell instanceof NumberRecord) {
            return String.valueOf(((NumberRecord) cell).getValue());
        } else if (cell instanceof LabelSSTRecord) {
            return sstrec.getString(((LabelSSTRecord) cell).getSSTIndex()).getString();
        } else {
            return null;
        }
    }

    private void whileReadAllSheet() {
        try {
            while (!readAllSheet) {
                Thread.sleep(31);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private class Async implements Runnable {
        final HSSFStreamingWorkbook streamming;
        final InputStream input;

        Async(HSSFStreamingWorkbook streamming, InputStream input) {
            this.streamming = streamming;
            this.input = input;
        }

        @Override
        public void run() {
            try (InputStream steam = input; 
                 POIFSFileSystem poifs = new POIFSFileSystem(steam); 
                 DocumentInputStream doc = poifs.createDocumentInputStream("Workbook")
            ) {
                HSSFRequest request = new HSSFRequest();
                request.addListenerForAllRecords(streamming);
                new HSSFEventFactory().processEvents(request, doc);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                streamming.readEnd();
            }
        }
    }

    // ------------------------------------------------------unsupported operation
    @Override
    public boolean isSheetHidden(int sheetIx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSheetVeryHidden(int sheetIx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getActiveSheetIndex() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setActiveSheet(int sheetIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFirstVisibleTab() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFirstVisibleTab(int sheetIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSheetOrder(String sheetname, int pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSelectedTab(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSheetName(int sheet, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sheet createSheet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sheet createSheet(String sheetname) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sheet cloneSheet(int sheetNum) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeSheetAt(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Font createFont() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Font findFont(boolean bold, short color, short fontHeight, String name,
        boolean italic, boolean strikeout, short typeOffset, byte underline) {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getNumberOfFonts() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNumberOfFontsAsInt() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Font getFontAt(short idx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Font getFontAt(int idx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CellStyle createCellStyle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNumCellStyles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CellStyle getCellStyleAt(int idx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNumberOfNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Name getName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends Name> getNames(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends Name> getAllNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Name getNameAt(int nameIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Name createName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNameIndex(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeName(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeName(Name name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int linkExternalWorkbook(String name, Workbook workbook) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPrintArea(int sheetIndex, String reference) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPrintArea(int sheetIndex, int startColumn,
        int endColumn, int startRow, int endRow) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPrintArea(int sheetIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removePrintArea(int sheetIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MissingCellPolicy getMissingCellPolicy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMissingCellPolicy(MissingCellPolicy missingCellPolicy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataFormat createDataFormat() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int addPicture(byte[] pictureData, int format) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends PictureData> getAllPictures() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CreationHelper getCreationHelper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHidden() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHidden(boolean hiddenFlag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSheetHidden(int sheetIx, boolean hidden) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SheetVisibility getSheetVisibility(int sheetIx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSheetVisibility(int sheetIx, SheetVisibility visibility) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addToolPack(UDFFinder toopack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setForceFormulaRecalculation(boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getForceFormulaRecalculation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetVersion getSpreadsheetVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int addOlePackage(byte[] oleData, String label, String fileName, String command)
        throws IOException {
        throw new UnsupportedOperationException();
    }

}
