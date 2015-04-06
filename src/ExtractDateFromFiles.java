import arabic.io.AlRayaPDF;
import arabic.ner.DateExtractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * s0ul on 3/22/15.
 */
public class ExtractDateFromFiles {

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(new File("f/files.txt").toPath());

        int i = 1;
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("MappedDateToFile.csv")));
        for (String name : lines) {
            System.out.println(String.format("%d/%d", i++, lines.size()));
            String file = "/Users/s0ul/Documents/Organized Result/" + name + ".pdf";
            String abc = AlRayaPDF.parsePDFString(new File(file));
            String date = DateExtractor.extract(abc);
            writer.write(String.format("%s,%s\n", name, date));
            writer.flush();
        }
        writer.close();
    }
}
