package arabic.io;

import arabic.normalize.ArabicMarshall;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * s0ul on 1/26/15.
 */
public class AlRayaPDF {
    public static final String END_OF_PAGE = "END_OF_PAGE";

    public static String parsePDFString(File file) throws IOException {
        PDDocument pd = PDDocument.load(new FileInputStream(file));
        PDFTextStripper t = new PDFTextStripper("UTF8");
        t.setPageEnd("\n" + END_OF_PAGE + "\n");
        String text = t.getText(pd);
        pd.close();

        return ArabicMarshall.normalize(text);
    }

    public static void parse(File file, File path, Predicate<String> predicate) throws IOException {
        String content = parsePDFString(file);
        String[] split = content.split("\n");

        Predicate<String> logic = predicate != null ? predicate : s -> true;

        String filename = FilenameUtils.removeExtension(file.getName());
        int counter = 1;
        StringBuilder article = new StringBuilder();
        for (String line : split) {
            if (line.trim().isEmpty()) continue;
            if (isHeader(line)) {
                if (article.length() != 0) {
                    String text = article.toString();
                    System.out.println(logic.test(text));
                    if (logic.test(text))
                        writeToFile(text, path.getPath() + "/" + filename + "/" + counter++ + ".txt");
                    article.setLength(0);
                    if (line.equals(END_OF_PAGE)) continue;
                }
            }
            article.append(line).append("\n");
        }
    }

    private static boolean isHeader(String line) {
        return line.trim().equals(END_OF_PAGE) || (line.contains("-") || line.contains("\u2013")) && line.contains(":");
    }

    private static void writeToFile(String content, String path) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(file, Charset.forName(AIO.ENCODING)));
        writer.write(content);
        writer.close();
    }
}
