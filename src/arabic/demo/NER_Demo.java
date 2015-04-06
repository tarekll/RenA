package arabic.demo;

import arabic.io.AIO;
import arabic.ner.*;
import arabic.normalize.ArabicMarshall;
import arabic.stopword.StopWord;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * s0ul on 1/9/15.
 */
public class NER_Demo {
    static HashMap<String, Integer> s = new HashMap<>();

    static {
        s.put("ORG", 2);
        s.put("PERS", 1);
        s.put("LOC", 3);
        s.put("MISC", 4);
        s.put("O", 5);
    }

    public static void demo_save() throws IOException {
        Triple<String, RenAConsumer, Boolean> corpus = new Triple<>("commons/ANER/corpus/include/",
                (RenAConsumer & Serializable) word -> {
                    String[] aligned = word.split(" ", 2);
                    String tag = aligned[1].replaceAll("[B|I]-", "");
                    String phrase = aligned[0];

                    return (!tag.equals("ORG") && !tag.equals("PERS") && !tag.equals("LOC") && !tag.equals("MISC") && !tag.equals("O"))
                            ? null : new Triple<>(phrase.trim(), tag, s.get(tag));
                }, false);

        Triple<String, RenAConsumer, Boolean> person = new Triple<>("commons/ANER/gazetteer/person/include/",
                (RenAConsumer & Serializable) word ->
                        (word.trim().isEmpty()) ? null : new Triple<>(ArabicMarshall.normalize(word.trim()), "PERS", s.get("PERS")), false);

        Triple<String, RenAConsumer, Boolean> organization = new Triple<>("commons/ANER/gazetteer/organization/include/",
                (RenAConsumer & Serializable) word ->
                        (word.trim().isEmpty()) ? null : new Triple<>(ArabicMarshall.normalize(word.trim()), "ORG", s.get("ORG")), false);

        Triple<String, RenAConsumer, Boolean> ex_person = new Triple<>("commons/ANER/gazetteer/person/exclude/",
                (RenAConsumer & Serializable) word -> new Triple<>(ArabicMarshall.normalize(word.trim()), "PERS", 0), true);

        Triple<String, RenAConsumer, Boolean> ex_org = new Triple<>("commons/ANER/gazetteer/organization/exclude/",
                (RenAConsumer & Serializable) word -> new Triple<>(ArabicMarshall.normalize(word.trim()), "ORG", 0), true);

        NERBuilder builder = NERBuilder.create(corpus, person, organization, ex_person, ex_org);
        NERBuilder.save(builder, "commons/demo/ner_demo");
    }

    public static void demo_load_extract() throws Exception {
        NERBuilder builder = NERBuilder.load(new File("commons/demo/ner_demo"));
        RenA ner = builder.compile(RenA.class);
        ner.addStopWord(new StopWord("commons/stopwords/"));
        System.out.println(ner.extract(
                AIO.readUTF8EncodedFile(
                        new File("commons/sample/aner_sample/basic_aner_test.txt")),
                new String[]{"PERS", "ORG"}));
    }

    public static void demo_load_tag() throws Exception {
        NERBuilder builder = NERBuilder.load(new File("commons/demo/ner_demo"));
        RenA ner = builder.compile();
        ner.addStopWord(new StopWord("commons/stopwords/"));

        String content = AIO.readUTF8EncodedFile(new File("commons/sample/aner_sample/basic_aner_test.txt"));
        System.out.println(ner.uniqueTag(content, "PERS", "ORG"));
    }

    public static void demo_load_extract_and_evaluate() throws Exception {
        NERBuilder builder = NERBuilder.load(new File("commons/demo/ner_demo"));
        RenA ner = builder.compile();
        StopWord words = new StopWord("commons/stopwords/");
        ner.addStopWord(words);
        ner.evaluate(
                AIO.readUTF8EncodedFile(
                        new File("commons/sample/aner_articles/0aaf872b-a5f1-445b-9ef0-ced1e6c8ca63/2.txt")),
                "ORG", "PERS");
    }

    public static void main(String[] args) throws Exception {
        demo_save();
        //demo_load_extract();
        //demo_load_extract_and_evaluate();
        demo_load_tag();
    }
}
