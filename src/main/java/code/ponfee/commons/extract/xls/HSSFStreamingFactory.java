package code.ponfee.commons.extract.xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.ThreadPoolExecutor;

public class HSSFStreamingFactory {

    public static HSSFStreamingWorkbook open(InputStream input, ThreadPoolExecutor executor) {
        return new HSSFStreamingWorkbook(input, executor);
    }

    public static HSSFStreamingWorkbook open(File file, ThreadPoolExecutor executor) {
        try {
            return open(new FileInputStream(file), executor);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static HSSFStreamingWorkbook open(String filePath, ThreadPoolExecutor executor) {
        return open(new File(filePath), executor);
    }
}
