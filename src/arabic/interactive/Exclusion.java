package arabic.interactive;

import arabic.io.AIO;
import arabic.ner.RenA;
import arabic.ner.Tuple;
import arabic.normalize.ArabicMarshall;
import arabic.stopword.StopWord;
import org.apache.commons.io.output.FileWriterWithEncoding;

import java.io.*;
import java.util.*;

/**
 * s0ul on 1/14/15.
 */
public class Exclusion {
    public static void main(String[] args) throws Exception {
        File file = new File("/Users/s0ul/Documents/Programming/Research/commons/sample/aner_html/universal/3.txt");

        String content = ArabicMarshall.normalize(AIO.readUTF8EncodedFile(file));
        RenA ner = RenA.rebuild(new File("commons/demo/ner_demo"));
        StopWord sw = new StopWord("commons/stopwords/");
        ner.addStopWord(sw);
        Set<Tuple<String, Set<String>>> list = ner.uniqueTag(content, "PERS", "ORG");

        System.out.println("ORIGINAL COUNT: " + list.size());
        Map<String, Set<String>> map = new HashMap<>();

        Set<String> pt = new HashSet<>(AIO.readAllLinesInDirectory(new File("commons/ANER/gazetteer/training/person/")));
        Set<String> ot = new HashSet<>(AIO.readAllLinesInDirectory(new File("commons/ANER/gazetteer/training/organization/")));

        for (Tuple<String, Set<String>> t : list) {
            String word = t.first();
            Set<String> tags = t.second();
            if (tags.contains("PERS") && pt.contains(word))
                tags.remove("PERS");
            if (tags.contains("ORG") && ot.contains(word))
                tags.remove("ORG");
        }

        for (Tuple<String, Set<String>> t : list) {
            if (t.second().isEmpty()) continue;
            if (!map.containsKey(t.first())) {
                map.put(t.first(), t.second());
            } else {
                map.get(t.first()).addAll(t.second());
            }
        }

        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        Map<String, Writer> writer = new HashMap<>();
        writer.put("PERS", new BufferedWriter(new FileWriterWithEncoding(new File("commons/ANER/gazetteer/person/exclude/list"), "UTF-8", true)));
        writer.put("ORG", new BufferedWriter(new FileWriterWithEncoding(new File("commons/ANER/gazetteer/organization/exclude/list"), "UTF-8", true)));

        Map<String, Writer> trainer = new HashMap<>();
        trainer.put("PERS", new BufferedWriter(new FileWriterWithEncoding(new File("commons/ANER/gazetteer/training/person/list"), "UTF-8", true)));
        trainer.put("ORG", new BufferedWriter(new FileWriterWithEncoding(new File("commons/ANER/gazetteer/training/organization/list"), "UTF-8", true)));

        int i = 0;
        System.out.println("Total: " + map.size());
        for (String word : map.keySet()) {
            Set<String> tags = map.get(word);
            i++;

            if (sw.contains(word)) continue;
            System.out.printf("[%d] %s \u2014 %s: ", i, tags, word);

            String input = console.readLine();
            String[] split = input.trim().split(",\\s*");

            for (String s : split) {
                String k = s.toUpperCase();
                if (!writer.containsKey(k)) continue;

                write(writer.get(k), word);
                tags.remove(k);
            }
            train(trainer, word, tags);
        }

        console.close();

        for (Writer w : writer.values()) w.close();
        for (Writer w : trainer.values()) w.close();
    }

    private static void train(Map<String, Writer> trainer, String word, Set<String> tags) throws IOException {
        for (String tag : tags) {
            write(trainer.get(tag), word);
        }
    }
    private static void write(Writer w, String word) throws IOException {
        w.write(word + "\n");
        w.flush();
    }
}
