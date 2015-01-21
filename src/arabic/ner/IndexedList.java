package arabic.ner;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * s0ul on 1/19/15.
 */
public class IndexedList implements Iterable<Tuple<String, Integer>>{
    private List<Tuple<String, Integer>> list;
    protected Set<String> index;

    public IndexedList() {
        list = new ArrayList<>();
        index = new HashSet<>();
    }

    public boolean contains(String s) {
        return index.contains(s);
    }

    public void add(String word, Integer index, boolean exclude) {
        list.add(new Tuple<>(word, index));
        if (!exclude)
            this.index.add(word);
    }

    public Iterator<Tuple<String, Integer>> iterator() {
        return list.iterator();
    }
}
