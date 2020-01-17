package kindleExtender.helpers;

import kindleExtender.models.Word;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class ExportHelper {
    public void exportToCSV(List<Word> words, File file) {
        try {
            FileWriter out = new FileWriter(file);
            CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT);
            words.forEach((word) -> {
                try {
                    printer.printRecord(word.getWord(), word.getTranslationValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
