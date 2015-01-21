package arabic.normalize;

import org.apache.lucene.analysis.ar.ArabicNormalizer;

/**
 * s0ul on 1/9/15.
 */
public class ArabicMarshall {
    private static ArabicNormalizer normalizer = new Normalizer();

    public static String normalize(String s) {
        char[] cPhrase = s.toCharArray();
        int n = normalizer.normalize(cPhrase, cPhrase.length);
        return new String(cPhrase, 0, n);
    }
}
