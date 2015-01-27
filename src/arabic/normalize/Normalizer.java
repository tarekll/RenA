package arabic.normalize;

import org.apache.lucene.analysis.ar.ArabicNormalizer;
import org.apache.lucene.analysis.util.StemmerUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * s0ul on 1/20/15.
 */
public class Normalizer extends ArabicNormalizer {
    public int normalize(char[] s, int len) {
        for(int i = 0; i < len; ++i) {
            switch(s[i]) {
                case ALEF_MADDA:
                case ALEF_HAMZA_ABOVE:
                case ALEF_HAMZA_BELOW:
                    s[i] = ALEF;
                case 'ؤ':
                case 'ئ':
                case 'ا':
                case 'ب':
                case 'ت':
                case 'ث':
                case 'ج':
                case 'ح':
                case 'خ':
                case 'د':
                case 'ذ':
                case 'ر':
                case 'ز':
                case 'س':
                case 'ش':
                case 'ص':
                case 'ض':
                case 'ط':
                case 'ظ':
                case 'ع':
                case 'غ':
                case '\u063b':
                case '\u063c':
                case '\u063d':
                case '\u063e':
                case '\u063f':
                case 'ف':
                case 'ق':
                case 'ك':
                case 'ل':
                case 'م':
                case 'ن':
                case 'ه':
                case 'و':
                case YEH:
                case DOTLESS_YEH:
                default:
                    break;
                case TEH_MARBUTA:
                    s[i] = HEH;
                    break;
                case 'ـ':
                case 'ً':
                case 'ٌ':
                case 'ٍ':
                case 'َ':
                case 'ُ':
                case 'ِ':
                case 'ّ':
                case 'ْ':
                    len = StemmerUtil.delete(s, i, len);
                    --i;
                    break;
            }
        }

        return len;
    }
}
