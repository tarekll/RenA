package arabic.ner;

import arabic.normalize.ArabicMarshall;
import arabic.stopword.StopWord;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * s0ul on 1/9/15.
 */
public class RenA implements NER {
    private ExactDictionaryChunker chunker;
    private Set<Tuple<String, String>> exclusion;
    private StopWord stopwords;
    private Set<String> exception;

    public RenA(MapDictionary<String> dictionary, Set<Tuple<String, String>> exclusion) {
        chunker = new ExactDictionaryChunker(dictionary, IndoEuropeanTokenizerFactory.INSTANCE);
        this.exclusion = exclusion;
        exception = new HashSet<>(Arrays.asList("ال", "ابن", "بن", "اﺑﻮ"));
    }

    public void addStopWord(StopWord sw) {
        this.stopwords = sw;
    }

    public Map<String, Set<String>> extract(String text, String[] keys) throws Exception {
        return extract(text, 2, keys);
    }

    public Map<String, Set<String>> extract(String text, int ngram, String[] keys) throws Exception {
        if (stopwords == null)
            throw new Exception("No stop words added. Add stop words by calling addStopWord()");
        Set<String> include = keys == null ? null : new HashSet<>(Arrays.asList(keys));

        // Tuple<String, Integer> = (word, index)
        return minimizer(collector(include, reducer(text)), ngram);
    }

    private List<String> reducer(String text) {
        Function<String, String> reducer = s -> s.replaceAll("\\.|,|\\(|\\)|:|;|،|؛|؟|\\?|»|«|-|\\\\|/|…", ""); //remove punctuations
        String[] split = text.split("\\s+");
        return Arrays.stream(split)
                .map(String::trim)
                .map(reducer)
                .filter(s -> !s.trim().isEmpty())
                .map(ArabicMarshall::normalize)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Map<String, IndexedList> collector(Set<String> include, List<String> collect) {
        Map<String, IndexedList> order = new HashMap<>();
        for (int i = 0, collectSize = collect.size(); i < collectSize; i++) {
            String word = collect.get(i);
            Chunking chunking = chunker.chunk(word);

            for (Chunk chunk : chunking.chunkSet()) {
                String tag = chunk.type();
                if (include != null && !include.contains(tag)) continue;
                if (exclusion.contains(new Tuple<>(word, tag))) continue;
                if (stopwords.contains(word) && !exception.contains(word)) continue;
                if (tag.equals("ORG") && stopwords.contains(word)) continue;

                IndexedList list = order.get(tag);
                if (list == null) {
                    list = new IndexedList();
                    order.put(tag, list);
                }
                list.add(word, i, stopwords.contains(word));
            }
        }
        return order;
    }

    private Map<String, Set<String>> minimizer(Map<String, IndexedList> order, int ngram) {
        Map<String, Set<String>> complete = new HashMap<>();
        IndexedList list = order.containsKey("PERS") ? order.get("PERS") : null;
        for (String key : order.keySet()) {
            List<String> result = new ArrayList<>();
            IndexedList chunks = order.get(key);

            int index = 0xDEADBEEF;
            String words = null;

            for (Tuple<String, Integer> chunk : chunks) {
                String word = chunk.first;
                if (key.equals("ORG") && list != null && list.contains(word)) continue;
                if (words == null) {
                    words = word;
                } else if ((index + 1) == chunk.second) {
                    words += " " + word;
                } else {
                    addHelper(result, words, ngram);
                    words = word;
                }
                index = chunk.second;
            }

            addHelper(result, words, ngram);
            if (result.isEmpty()) continue;
            complete.put(key, new LinkedHashSet<>(result));
        }
        return complete;
    }

    public List<Tuple<String, Set<String>>> tag(String text, String... include) {
        Set<String> includeTags = include.length == 0 ? null : new HashSet<>(Arrays.asList(include));

        List<String> reducer = reducer(text);
        List<Tuple<String, Set<String>>> tags = new ArrayList<>();

        for (String word : reducer) {
            Chunking chunking = chunker.chunk(word);
            Set<String> set = new HashSet<>();
            for (Chunk chunk : chunking.chunkSet()) {
                String type = chunk.type();
                if (includeTags != null && !includeTags.contains(type)) continue;
                if (stopwords.contains(word) && !exception.contains(word)) continue;

                set.add(type);
            }
            if (set.size() >= 2) set.remove("O");
            if (set.isEmpty()) continue;
            tags.add(new Tuple<>(word, set));
        }
        return tags;
    }

    public Set<Tuple<String, Set<String>>> uniqueTag(String text, String... include) {
        List<Tuple<String, Set<String>>> tags = tag(text, include);
        return tags.stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void addHelper(List<String> join, String word, int n) {
        if (word == null) return;

        if (word.split(" ").length >= n)
            join.add(word);
    }

    public void evaluate(String text, String... keys) throws Exception {
        Map<String, Set<String>> extract = extract(text, keys);

        int total = 0;
        for (String key : extract.keySet()) {
            Set<String> k = extract.get(key);
            total += k.size();

            System.out.printf("Number of %s Entity collected: %d.\n", key, k.size());
        }
        System.out.println("Number of Named Entity Collected: " + total + '.');
        System.out.println("Number of Total Words: " + text.split(" ").length + '.');
    }

    public static RenA load(File file) throws IOException, ClassNotFoundException {
        return NERBuilder.load(file).compile();
    }

    public static RenA rebuild(File file) throws IOException, ClassNotFoundException {
        NERBuilder load = NERBuilder.load(file);
        load.rebuild();

        NERBuilder.save(load, file.getAbsolutePath());
        return load.compile();
    }
}
