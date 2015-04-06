import arabic.io.AIO;
import arabic.ner.RenA;
import arabic.stopword.StopWord;
import org.apache.commons.io.output.FileWriterWithEncoding;

import java.io.BufferedWriter;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * s0ul on 2/16/15.
 */
public class NERArticleProcessor {
    public static void main(String[] args) throws Exception {
        RenA ner = RenA.load(new File(args[0]));
        ner.addStopWord(new StopWord(args[1]));

        File[] files = new File(args[2]).listFiles((dir, name) -> {
            return name.trim().endsWith(".txt");
        });

        for (File file : files) {
            String content = AIO.readUTF8EncodedFile(file);
            Map<String, Set<String>> extract = ner.extract(content, new String[]{"PERS", "ORG", "LOC"});

            File out = new File(args[2] + file.getName().replace(".txt", ".csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(out, "UTF-8", true));
            writer.write(",\n");
            writer.write("NER,\n");
            writer.write("ORG:,"+ extract.getOrDefault("ORG", new HashSet<>()).toString().replaceAll("\\[|]", "") + '\n');
            writer.write("PERS:,"+ extract.getOrDefault("PERS", new HashSet<>()).toString().replaceAll("\\[|]", "") + '\n');
            writer.write("LOC:,"+ extract.getOrDefault("LOC", new HashSet<>()).toString().replaceAll("\\[|]", "") + '\n');
            writer.close();
        }
    }
}
