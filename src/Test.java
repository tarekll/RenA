import arabic.io.AIO;
import arabic.ner.RenA;
import arabic.stopword.StopWord;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * s0ul on 1/16/15.
 */
public class Test {
    public static void main(String[] args) throws Exception {
        RenA ner = RenA.rebuild(new File("commons/demo/ner_demo"));
        ner.addStopWord(new StopWord("commons/stopwords/"));
        String[] keys = {"PERS", "ORG"};

        File file = new File("/Users/s0ul/Documents/Programming/Research/commons/sample/aner_html/universal/3.txt");
        String result = AIO.readUTF8EncodedFile(file);

        Map<String, Set<String>> extract = ner.extract(result, keys);
        System.out.println("PERSON: " + extract.get("PERS"));
        System.out.println("ORGANIZATION: " + extract.get("ORG"));
    }
}
