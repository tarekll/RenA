package arabic.eval;

import arabic.ner.NER;
import arabic.ner.Tuple;
import arabic.normalize.ArabicMarshall;
import arabic.stopword.StopWord;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * s0ul on 2/5/15.
 */
public class BasicNER implements NER {
    private ExactDictionaryChunker chunker;

    public BasicNER(MapDictionary<String> dictionary, Set<Tuple<String, String>> exclusion) {
        chunker = new ExactDictionaryChunker(dictionary, IndoEuropeanTokenizerFactory.INSTANCE);
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

                set.add(type);
            }
            if (set.size() >= 2) set.remove("O");
            if (set.isEmpty()) continue;
            tags.add(new Tuple<>(word, set));
        }
        return tags;
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

    public Set<Tuple<String, Set<String>>> uniqueTag(String text, String... include) {
        List<Tuple<String, Set<String>>> tags = tag(text, include);
        return tags.stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void addStopWord(StopWord sw) {

    }
}
