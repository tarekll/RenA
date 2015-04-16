import arabic.io.AIO;
import arabic.ner.DateExtractor;
import arabic.ner.RenA;
import arabic.stopword.StopWord;
import com.aliasi.util.Files;
import org.apache.commons.io.output.FileWriterWithEncoding;

import java.io.BufferedWriter;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * s0ul on 2/23/15.
 */
public class Suite {
    public static String getTitle(String text) {
        String[] contents = text.split("\n");
        String date = getDate(contents[0]);

        String content = date != null ? text.substring(contents[0].length() + 1) : text;
        Matcher matcher = Pattern.compile(":(?:.*?)[.|,|،|?|؟|;|؛|\u0609|%|\u060A|؍]").matcher(content.replaceAll("\n", " "));

        String title = "";
        if (matcher.find()) {
            String clean = matcher.group(0).replaceAll("[.|,|،|?|؟|;|؛|\u0609|%|\u060A|؍|:]", "").trim();
            String[] words = clean.split(" ");
            if (words.length > 15)
                for (int i = 0; i < 15; i++) title += words[i] + " ";
            else title = clean;
        } else {
            String[] words = content.split(" ");
            for (int i = 0; i < Math.min(15, words.length); i++) title += words[i] + " ";
        }
        return title.trim().replaceAll("\\(|\\)|\\{|}|\n|»", "").trim();
    }

    public static String getDate(String content) {
        String extract = DateExtractor.extract(content);
        return extract == null ? "التاريخ غير متوفر" : extract;
    }

    public static String getAuthor(RenA ner, String content) throws Exception {
        Matcher matcher = Pattern.compile("كتب.?-(.*?):").matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        String[] contents = content.split("\n");
        String text = contents[0] + " " + contents[1] + " " + contents[2] + " " + contents[3];
        Map<String, Set<String>> extract = ner.extract(text, 2, new String[]{"PERS"});
        Set<String> person = extract.getOrDefault("PERS", new HashSet<>());
        if (!person.isEmpty())
            return person.iterator().next(); //Retrieve first value of the LinkedHashSet
        return "اسم الكاتب غير متوفر";
    }

    public static void main(String[] args) throws Exception {
        RenA ner = RenA.load(new File(args[0]));
        ner.addStopWord(new StopWord(args[1]));

        File[] files = new File(args[2]).listFiles(pathname -> {
            return pathname.getName().endsWith(".txt");
        });

        for (File file : files) {
            String content = AIO.readUTF8EncodedFile(file);
            File csv = new File(file.getAbsolutePath().replace(".txt", ".csv"));

            if (csv.exists()) {
                String s = Files.readFromFile(csv, "utf-8");
                if (s.contains(",\nAttribute,\n")) continue;
            }

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(csv, "utf-8", true));
                writer.write(",\nAttribute,\n");
                writer.write("Title," + getTitle(content) + "\n");
                writer.write("Author," + getAuthor(ner, content) + "\n");
                writer.write("Dates," + getDate(content) + "\n");
                writer.close();
            } catch (Exception e) {
                System.out.println("Something is wrong with: " + file.getName());
            }
        }
    }
}
