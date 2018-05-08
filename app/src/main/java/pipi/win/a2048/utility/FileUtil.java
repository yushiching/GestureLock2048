package pipi.win.a2048.utility;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

public class FileUtil {

    /* Write an array list of strings to a specific path */
    public static void writeToFile(String path, List<String[]> data) {
        CSVWriter writer;
        try {
            writer = new CSVWriter(new FileWriter(path, true));
            writer.writeAll(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void writeToBuffer(StringWriter writer, List<String[]> data){
        CSVWriter csvWriter=new CSVWriter(writer);
        csvWriter.writeAll(data);
    }
}
