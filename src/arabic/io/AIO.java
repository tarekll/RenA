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

/**
 * s0ul on 1/9/15.
 */
public class AIO {
    private static final String ENCODING = "UTF-8";
    public static final String END_OF_PAGE = "END_OF_PAGE";

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

    public static String parsePDFString(File file) throws IOException {
        PDDocument pd = PDDocument.load(new FileInputStream(file));
        PDFTextStripper t = new PDFTextStripper("UTF8");
        t.setPageEnd("\n" + END_OF_PAGE + "\n");
        String text = t.getText(pd);
        pd.close();

        return ArabicMarshall.normalize(text);
    }

    public static void automate(File file, File path) throws IOException {
        String content = parsePDFString(file);
        String[] split = content.split("\n");

        String filename = FilenameUtils.removeExtension(file.getName());
        int counter = 1;
        StringBuilder article = new StringBuilder();
        for (String line : split) {
            if (line.trim().isEmpty()) continue;
            if (line.contains(":") && line.contains("-") || line.trim().equals(END_OF_PAGE)) {
                if (article.length() != 0) {
                    writeToFile(article.toString(), path.getPath() + "/" + filename + "/" + counter++ + ".txt");
                    article.setLength(0);
                    continue;
                }
            }
            article.append(line).append("\n");
        }
    }

    private static void writeToFile(String content, String path) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(file, Charset.forName(ENCODING)));
        writer.write(content);
        writer.close();
    }
}
