import arabic.io.AIO;
import arabic.ner.RenA;
import arabic.stopword.StopWord;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

/**
 * s0ul on 1/9/15.
 */
public class PDFExtractionOrganizer {
    public static void main(String[] args) throws Exception {
        RenA ner = RenA.load(new File("commons/demo/ner_demo"));
        ner.addStopWord(new StopWord("commons/stopwords/"));
        String[] keys = {"PERS", "ORG"};

        FileFilter filter = file -> file.getName().endsWith(".pdf");
        File[] f = new File("/Volumes/Tarek/NewAlrayaPDF5200Files/").listFiles(filter);
        File out = new File("/Volumes/Tarek/Organized Result/");

        //List<File> files = PDFE.random_sample(Arrays.asList(f), 10);
        for (File file : f) {
            //AIO.parsePDF(file, "commons/sample/aner_articles/");
            String result = AIO.parsePDFString(file).trim();
            String filename = file.getName().replace(".pdf", "");

            if (result.length() < 100) { // Image
                System.out.println(filename + " \u2014 OCR PDF Detected.");
                continue;
            }
            Map<String, Set<String>> extract = ner.extract(result, keys);
            if (extract.isEmpty()) {
                System.out.println(filename + " \u2014 Unable to extract PDF properly.");
                continue;
            }

            if (!extract.containsKey("PERS") || !extract.containsKey("ORG")) continue;

            Map<String, Set<String>> m = new HashMap<>();
            for (String key : extract.keySet())
                m.put(key, new HashSet<>(extract.get(key)));
            int count = m.get("PERS").size() + m.get("ORG").size();
            if (count <= 200) continue;
            FileUtils.copyFile(file, new File(out.getAbsolutePath() + "/" + file.getName()));
        }
    }
}
