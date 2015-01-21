package arabic.ner;

import arabic.io.AIO;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;

import java.io.*;
import java.util.*;

/**
 * s0ul on 1/9/15.
 */
public class NERBuilder implements Serializable {
    private MapDictionary<String> dictionary;
    private Set<Tuple<String, String>> exclude;
    private List<Triple<String, RenAConsumer, Boolean>> rules;

    public NERBuilder() {
        dictionary = new MapDictionary<>();
        exclude = new HashSet<>();
        rules = new ArrayList<>();
    }

    public void add(String directory, RenAConsumer consumer) throws IOException {
        add(directory, consumer, false);
    }

    public void add(String directory, RenAConsumer consumer, boolean exclude) throws IOException {
        List<String> documents = AIO.readAllLinesInDirectory(new File(directory));
        documents.stream().forEach(s -> {
            Triple<String, String, Integer> word = consumer.accept(s);
            if (word == null) return;
            if (exclude)
                this.exclude.add(new Tuple<>(word.first, word.second));
            else
                dictionary.addEntry(new DictionaryEntry<>(word.first, word.second, word.third));
        });
    }

    public RenA compile() {
        return new RenA(dictionary, exclude);
    }

    public void rebuild() throws IOException {
        dictionary = new MapDictionary<>();
        for (Triple<String, RenAConsumer, Boolean> item : rules)
            add(item.first, item.second, item.third);
    }

    @SafeVarargs
    public static NERBuilder create(Triple<String, RenAConsumer, Boolean>... library) throws IOException {
        NERBuilder builder = new NERBuilder();
        for (Triple<String, RenAConsumer, Boolean> item : library) {
            builder.add(item.first, item.second, item.third);
            builder.rules.add(item);
        }
        return builder;
    }

    public static void save(NERBuilder ner, String file_out) throws IOException {
        ObjectOutput output = new ObjectOutputStream(new FileOutputStream(file_out));
        output.writeObject(ner);
        output.close();
    }

    public static NERBuilder load(File file) throws IOException, ClassNotFoundException {
        ObjectInputStream output = new ObjectInputStream(new FileInputStream(file));
        NERBuilder ner = (NERBuilder) output.readObject();
        output.close();
        return ner;
    }
}
