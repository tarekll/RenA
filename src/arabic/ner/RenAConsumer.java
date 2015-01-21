package arabic.ner;

import java.io.Serializable;

/**
 * s0ul on 1/9/15.
 */
public interface RenAConsumer extends Serializable {
    public Triple<String, String, Integer> accept(String word);
}
