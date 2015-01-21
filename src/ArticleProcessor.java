import arabic.io.AIO;
import arabic.ner.RenA;
import arabic.stopword.StopWord;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * s0ul on 1/14/15.
 */
public class ArticleProcessor {
    public static void main(String[] args) throws Exception {
        RenA ner = RenA.load(new File("commons/demo/ner_demo"));
        ner.addStopWord(new StopWord("commons/stopwords/"));
        String[] keys = {"PERS", "ORG"};

        File[] documents = new File("commons/sample/aner_articles/").listFiles();
        for (File document : documents) {
            for (File article : (document.listFiles())) {
                String a = AIO.readUTF8EncodedFile(article);

                Map<String, Set<String>> result = ner.extract(a, keys);
                boolean empty = result.isEmpty();
                boolean contains_keys = !result.containsKey("PERS") || !result.containsKey("ORG");
                boolean more_than_2_people = result.containsKey("PERS") && result.get("PERS").size() < 2;
                if (empty || contains_keys || more_than_2_people) continue; // DELETE files here
                System.out.println(result);
            }
        }
    }
}
