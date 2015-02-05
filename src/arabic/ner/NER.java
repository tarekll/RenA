package arabic.ner;

import java.util.List;
import java.util.Set;

/**
 * s0ul on 2/5/15.
 */
public interface NER {
    public List<Tuple<String, Set<String>>> tag(String text, String... include);
    public Set<Tuple<String, Set<String>>> uniqueTag(String text, String... include);
}
