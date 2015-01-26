package arabic.interactive;

import arabic.io.AIO;
import arabic.ner.RenA;
import arabic.ner.Tuple;
import arabic.stopword.StopWord;
import org.apache.commons.io.output.FileWriterWithEncoding;

import java.io.*;
import java.util.*;

/**
 * s0ul on 1/14/15.
 */
public class Inclusion {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File file = new File("/Users/s0ul/Documents/Programming/Research/commons/sample/aner_html/lebanon/al-akhbar/2.txt");

        String content = AIO.readUTF8EncodedFile(file);
        RenA ner = RenA.rebuild(new File("commons/demo/ner_demo"));

        StopWord sw = new StopWord("commons/stopwords/");
        Set<Tuple<String, Set<String>>> list = ner.uniqueTag(content);
        Map<String, Set<String>> map = new HashMap<>();

        Set<String> pt = new HashSet<>(AIO.readAllLinesInDirectory(new File("commons/ANER/gazetteer/training/person/")));
        Set<String> ot = new HashSet<>(AIO.readAllLinesInDirectory(new File("commons/ANER/gazetteer/training/organization/")));
        Set<String> o = new HashSet<>(AIO.readAllLinesInDirectory(new File("commons/ANER/gazetteer/training/others/")));

        for (Tuple<String, Set<String>> t : list) {
            if (t.second().contains("PERS") || t.second().contains("ORG")) continue;
            if (o.contains(t.first())) continue;

            if (!map.containsKey(t.first()))
                map.put(t.first(), t.second());
            else
                map.get(t.first()).addAll(t.second());
        }

        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        Map<String, Writer> writer = new HashMap<>();
        writer.put("PERS", new BufferedWriter(new FileWriterWithEncoding(new File("commons/ANER/gazetteer/person/include/list"), "UTF-8", true)));
        writer.put("ORG", new BufferedWriter(new FileWriterWithEncoding(new File("commons/ANER/gazetteer/organization/include/list"), "UTF-8", true)));
        writer.put("LOC", new BufferedWriter(new FileWriterWithEncoding(new File("commons/ANER/gazetteer/location/include/list"), "UTF-8", true)));
        writer.put("S", new BufferedWriter(new FileWriterWithEncoding(new File("commons/stopwords/list"), "UTF-8", true)));

        Map<String, Writer> trainer = new HashMap<>();
        trainer.put("PERS", new BufferedWriter(new FileWriterWithEncoding(new File("commons/ANER/gazetteer/training/person/list"), "UTF-8", true)));
        trainer.put("ORG", new BufferedWriter(new FileWriterWithEncoding(new File("commons/ANER/gazetteer/training/organization/list"), "UTF-8", true)));
        trainer.put("O", new BufferedWriter(new FileWriterWithEncoding(new File("commons/ANER/gazetteer/training/others/list"), "UTF-8", true)));

        System.out.println("Total: "+ map.size());
        int i = 0;
        for (String word : map.keySet()) {
            i++;
            Set<String> tags = map.get(word);

            if (sw.contains(word)) tags.add("S");
            System.out.printf("[%d] %s \u2014 %s: ", i, tags, word);

            String input = console.readLine();
            String[] split = input.trim().split(",\\s*");

            for (String s : split) {
                String k = s.toUpperCase();
                if (tags.contains(k)) continue;
                if (!writer.containsKey(k)) continue;

                write(writer.get(k), word);
                tags.add(k);
            }

            save(pt, ot, o, trainer, word, tags);
        }
        console.close();

        for (Writer w : writer.values()) w.close();
        for (Writer w : trainer.values()) w.close();
    }

    private static void save(Set<String> pt, Set<String> ot, Set<String> o, Map<String, Writer> trainer, String word, Set<String> tags) throws IOException {
        boolean person = tags.contains("PERS");
        boolean org = tags.contains("ORG");
        if (person && !pt.contains(word))
            write(trainer.get("PERS"), word);
        if (org && !ot.contains(word))
            write(trainer.get("ORG"), word);
        if (!person && !org && !o.contains(word))
            write(trainer.get("O"), word);
    }

    private static void write(Writer w, String word) throws IOException {
        w.write(word + "\n");
        w.flush();
    }
}
