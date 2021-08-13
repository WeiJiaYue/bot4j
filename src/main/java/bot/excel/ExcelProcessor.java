package bot.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by louisyuu on 2020/12/10 4:06 下午
 * <p>
 * <p>
 * <p>
 * Excel加工器
 */

@Slf4j
public abstract class ExcelProcessor {


    protected String filepath;

    protected String filename;

    protected int sheetNo = 0;

    protected String newFilename;

    protected String newSheetName;

    protected boolean neededGenerateNewExcel = true;


    public ExcelProcessor() {
    }

    public ExcelProcessor(String filepath, String filename) {
        if (filepath.lastIndexOf("/") == -1) {
            throw new RuntimeException("Filepath最后一个字符必须是斜杠[/]");
        }
        if (filename.indexOf("/") == 0) {
            throw new RuntimeException("Filename第一个字符不能是斜杠[/]");
        }
        this.filepath = filepath;
        this.filename = filename;
    }


    /**
     * First step
     * 获取原材料
     *
     * @return
     * @throws Exception
     */
    private ExcelDatum getRaw() {
        Workbook workbook = null;
        try {
            File file = new File(filepath + filename);
            workbook = ExcelUtil.newWorkbook(new FileInputStream(file));
            return ExcelUtil.convertToExcelDatum(workbook, sheetNo);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                }
            }
        }
    }


    /**
     * 生产
     *
     * @param rawAfterProcessed
     */
    private void produce(ExcelDatum rawAfterProcessed) {
        if (newFilename == null) {
            newFilename = "Proceed_by_excel_processor_" + UUID.randomUUID().toString() + ".xlsx";
        }
        if (newSheetName == null) {
            newSheetName = "HelloWorld";
        }
        HSSFWorkbook workbook = ExcelUtil.newWorkbookWithDatum(rawAfterProcessed, newSheetName);

        try {
            File file = new File(filepath + newFilename);
            workbook.write(new FileOutputStream(file));
        } catch (IOException e) {
            try {
                workbook.close();
            } catch (IOException ioException) {
            }
        }
    }


    /**
     * 加工
     */
    protected void process() {
        ExcelDatum raw = getRaw();
        try {
            doProcess(raw);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        if (isNeededGenerateNewExcel()) {
            produce(raw);
        }
    }


    protected abstract void doProcess(ExcelDatum raw) throws Exception;


    /**
     * =============Getters & Setters================
     */

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        if (filepath.lastIndexOf("/") == -1) {
            throw new RuntimeException("Filepath最后一个字符必须是斜杠[/]");
        }
        this.filepath = filepath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        if (filename.indexOf("/") == 0) {
            throw new RuntimeException("Filename第一个字符不能是斜杠[/]");
        }
        this.filename = filename;
    }

    public int getSheetNo() {
        return sheetNo;
    }

    public void setSheetNo(int sheetNo) {
        this.sheetNo = sheetNo;
    }

    public String getNewFilename() {
        return newFilename;
    }

    public void setNewFilename(String newFilename) {
        this.newFilename = newFilename;
    }

    public String getNewSheetName() {
        return newSheetName;
    }

    public void setNewSheetName(String newSheetName) {
        this.newSheetName = newSheetName;
    }

    public boolean isNeededGenerateNewExcel() {
        return neededGenerateNewExcel;
    }

    public void setNeededGenerateNewExcel(boolean neededGenerateNewExcel) {
        this.neededGenerateNewExcel = neededGenerateNewExcel;
    }
}
