package arabic.io;

import arabic.normalize.ArabicMarshall;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * s0ul on 1/9/15.
 */
public class AIO {
    protected static final String ENCODING = "UTF-8";

    public static List<String> readAllLinesInDirectory(File directory) throws IOException {
        List<String> lines = new ArrayList<>();
        File[] files = directory.listFiles(file -> { // Collect all readable files
            String name = file.getName();
            return !file.isDirectory() && (name.endsWith(".txt") || !name.contains("."));
        });

        for (File file : files) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), ENCODING));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static String readUTF8EncodedFile(File file) throws IOException {
        String text = "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), Charset.forName(ENCODING)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text += line + "\n";
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }

    public static String readUTF8URL(String url) throws IOException {
        String source = "";
        URL link = new URL(url);
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(link.openStream(), "UTF-8"));
        String input;
        while ((input = reader.readLine()) != null)
            source += (source.equals("") ? "" : "\n") + input;
        return source;
    }


}
