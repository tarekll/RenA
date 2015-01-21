package arabic.io;

import arabic.normalize.ArabicMarshall;
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
    public static final String END_OF_ARTICLE = "\n================== END OF ARTICLE ==================\n";
    public static final String END_OF_PAGE = "================== END OF PAGE ==================";

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
        t.setPageEnd(END_OF_PAGE);
        t.setParagraphEnd(END_OF_ARTICLE);
        t.setForceParsing(true);
        String text = t.getText(pd);
        pd.close();

        return ArabicMarshall.normalize(text);
    }

    public static void parsePDF(File file, String output) throws IOException {
        String text = parsePDFString(file);
        if (text.length() <= 50)
            return; // Are most likely images

        String[] split = text.split(END_OF_PAGE);
        String filename = file.getName().replace(".pdf", "");
        File out = new File(output + "/" + filename);
        out.mkdir();


        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(new File(out.getPath() + "/" + 0 + ".txt")), Charset.forName("UTF8")));
        writer.write(text.replaceAll(END_OF_PAGE, ""));
        writer.close();
        for (int i = 1; i < split.length; i++) {
            writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(new File(out.getPath() + "/" + i + ".txt")), Charset.forName("UTF8")));
            writer.write(split[i]);
            writer.close();
        }
    }

}
