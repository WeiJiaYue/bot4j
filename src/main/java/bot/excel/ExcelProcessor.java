package bot.excel;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by louisyuu
 * <p>
 * An abstract excel processor
 * Using Java to manipulate Excel's addition, deletion, modification, and query in batches is as simple as operating a Java collection
 * Simple to use
 */
public abstract class ExcelProcessor {


    private final static Logger log = Logger.getLogger(ExcelProcessor.class.getName());


    protected String filepath;

    protected String filename;

    protected int sheetNo = 0;

    protected boolean neededGenerateNewExcel = true;

    //Default suffix is xls
    //Use setNewFileSuffix() to override if required
    protected Helpers.ExcelSuffix newFileSuffix = Helpers.ExcelSuffix.xls;

    //Default new file name is ExcelDao_uuid.xls
    //Use setNewFileName() to override if required
    protected String newFilename = "ExcelDao_" + UUID.randomUUID() + newFileSuffix.name();


    //Default sheet name is ExcelDao
    //Use setNewSheetName() to override if required
    protected String newSheetName = "ExcelDao";


    public ExcelProcessor(String filepath, String filename) {
        if (filepath.lastIndexOf("/") == -1) {
            throw new RuntimeException("File path should end with forward slash [/]");
        }
        if (filename.indexOf("/") == 0) {
            throw new RuntimeException("File name should not start with forward slash [/]");
        }
        this.filepath = filepath;
        setFilename(filename);
    }


    /**
     * First step
     * <p>
     * Convert excel to java object ExcelTable from disk
     */
    private ExcelTable getExcelTable() {
        try {
            File file = new File(filepath + filename);
            return Helpers.excelToTable(new FileInputStream(file), sheetNo);
        } catch (Exception e) {
            log.warning(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    /**
     * Second step
     * <p>
     * Process by subclasses
     */
    public abstract void doProcess(ExcelTable table) throws Exception;


    /**
     * Last step
     *
     * Convert java object ExcelTable to excel file to disk
     */
    private void produce(ExcelTable table) {
        Workbook workbook = Helpers.tableToExcel(table, newFileSuffix, newSheetName);
        try {
            File file = new File(filepath + newFilename);
            workbook.write(new FileOutputStream(file));
        } catch (IOException e) {
            try {
                workbook.close();
            } catch (IOException ioException) {
            }
        }
        System.out.println("==> Proceed successfully.");
    }


    /**
     * Context process
     */
    public void process() {
        ExcelTable table = getExcelTable();
        try {
            doProcess(table);
        } catch (Exception e) {
            log.warning(e.getMessage());
            throw new RuntimeException("Do process exception", e);
        }
        if (isNeededGenerateNewExcel()) {
            produce(table);
        }
    }


    /**
     * =============Getters & Setters================
     */

    public void setFilepath(String filepath) {
        if (filepath.lastIndexOf("/") == -1) {
            throw new RuntimeException("File path should end with forward slash [/]");
        }
        this.filepath = filepath;
    }


    public void setFilename(String filename) {
        if (filename.indexOf("/") == 0) {
            throw new RuntimeException("File name should not start with forward slash [/]");
        }
        this.filename = filename;
    }

    public boolean isNeededGenerateNewExcel() {
        return neededGenerateNewExcel;
    }

    public void setNeededGenerateNewExcel(boolean neededGenerateNewExcel) {
        this.neededGenerateNewExcel = neededGenerateNewExcel;
    }

    public void setSheetNo(int sheetNo) {
        this.sheetNo = sheetNo;
    }


    public void setNewFileName(String newFilename) {
        this.newFilename = newFilename;
        if (this.newFilename.lastIndexOf(Helpers.ExcelSuffix.xls.name()) == -1
                || this.newFilename.lastIndexOf(Helpers.ExcelSuffix.xlsx.name()) == -1) {
            this.newFilename += ".xls";
        }
    }

    public void setNewFileSuffix(Helpers.ExcelSuffix newFileSuffix) {
        this.newFileSuffix = newFileSuffix;
    }

    public void setNewSheetName(String newSheetName) {
        this.newSheetName = newSheetName;
    }


}
