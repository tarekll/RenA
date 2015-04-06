package arabic.eval;

import arabic.io.AIO;
import arabic.ner.*;
import arabic.normalize.ArabicMarshall;
import arabic.stopword.StopWord;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.lucene.analysis.ar.ArabicNormalizer;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * s0ul on 2/5/15.
 */
public class Evaluate {
    static HashMap<String, Integer> s = new HashMap<>();
    static HashMap<String, String> k = new HashMap<>();

    static {
        s.put("ORG", 2);
        s.put("PERS", 1);
        s.put("LOC", 3);
        s.put("MISC", 4);
        s.put("O", 5);
        k.put("organization", "ORG");
        k.put("person", "PERS");
        k.put("location", "LOC");
    }

    private static NER buildRenA() throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
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
        Triple<String, RenAConsumer, Boolean> location = new Triple<>("commons/ANER/gazetteer/location/include/",
                (RenAConsumer & Serializable) word ->
                        (word.trim().isEmpty()) ? null : new Triple<>(ArabicMarshall.normalize(word.trim()), "LOC", s.get("LOC")), false);

        Triple<String, RenAConsumer, Boolean> ex_person = new Triple<>("commons/ANER/gazetteer/person/exclude/",
                (RenAConsumer & Serializable) word -> new Triple<>(ArabicMarshall.normalize(word.trim()), "PERS", 0), true);

        Triple<String, RenAConsumer, Boolean> ex_org = new Triple<>("commons/ANER/gazetteer/organization/exclude/",
                (RenAConsumer & Serializable) word -> new Triple<>(ArabicMarshall.normalize(word.trim()), "ORG", 0), true);

        NERBuilder builder = NERBuilder.create(corpus, person, organization, location, ex_person, ex_org);
        RenA renA = builder.compile(RenA.class);
        renA.addStopWord(new StopWord("/Users/s0ul/Documents/Programming/Research/commons/stopwords"));
        return renA;
    }

    private static NER buildBaselineNER() throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Triple<String, RenAConsumer, Boolean> corpus = new Triple<>("/Users/s0ul/Documents/Programming/Research/commons/baseline_ner/corpus/",
                (RenAConsumer & Serializable) word -> {
                    String[] aligned = word.split(" ", 2);
                    String tag = aligned[1].replaceAll("[B|I]-", "");
                    String phrase = aligned[0];

                    return (!tag.equals("ORG") && !tag.equals("PERS") && !tag.equals("LOC") && !tag.equals("MISC") && !tag.equals("O"))
                            ? null : new Triple<>(phrase.trim(), tag, s.get(tag));
                }, false);

        Triple<String, RenAConsumer, Boolean> person = new Triple<>("/Users/s0ul/Documents/Programming/Research/commons/baseline_ner/gazetteer/person/",
                (RenAConsumer & Serializable) word ->
                        (word.trim().isEmpty()) ? null : new Triple<>(ArabicMarshall.normalize(word.trim()), "PERS", s.get("PERS")), false);

        Triple<String, RenAConsumer, Boolean> organization = new Triple<>("/Users/s0ul/Documents/Programming/Research/commons/baseline_ner/gazetteer/organization/",
                (RenAConsumer & Serializable) word ->
                        (word.trim().isEmpty()) ? null : new Triple<>(ArabicMarshall.normalize(word.trim()), "ORG", s.get("ORG")), false);

        Triple<String, RenAConsumer, Boolean> location = new Triple<>("/Users/s0ul/Documents/Programming/Research/commons/baseline_ner/gazetteer/person/",
                (RenAConsumer & Serializable) word ->
                        (word.trim().isEmpty()) ? null : new Triple<>(ArabicMarshall.normalize(word.trim()), "LOC", s.get("LOC")), false);

        NERBuilder builder = NERBuilder.create(corpus, person, organization, location);
        return builder.compile(BasicNER.class);
    }

    private static Map<String, Tuple<Integer, Set<String>>> evaluate(NER ner, String content, Map<String, Set<String>> metadata) {
        Map<String, Tuple<Integer, Set<String>>> map = new HashMap<>();
        Set<Tuple<String, Set<String>>> set = ner.uniqueTag(content, "PERS", "ORG", "LOC");
        for (String key : Arrays.asList("PERS", "ORG", "LOC")) {
            Set<String> data = metadata.get(key);

            Set<String> a = new HashSet<>();
            for (String word : data) {
                int count = 0;
                for (Tuple<String, Set<String>> t : set) {
                    if (t.second().contains(key)) {
                        count++;
                        if (t.first().equals(word)) {
                            a.add(word);
                        }
                    }
                }
                map.put(key, new Tuple<>(count, a));

            }
        }

        return map;
    }

    private static Map<String, Set<String>> getMetadata(String content) {
        String regexp = "(?:location|organization|person|category)";
        Pattern compile = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compile.matcher(content);
        matcher.find();
        String first = matcher.group();
        String text = content.substring(content.indexOf(first));
        Map<String, Set<String>> result = new HashMap<>();
        String clean = clean(text);
        String[] split = clean.split("\n");
        for (String line : split) {
            String a = line.toLowerCase().trim();
            if (a.startsWith("person") || a.startsWith("organization") || a.startsWith("location")) {
                String[] sp = line.trim().split(":");
                if (sp.length != 2) {
                    result.put(k.get(sp[0].trim().toLowerCase()), new HashSet<>());
                    continue;
                }
                String range = sp[1].trim();
                String[] words = range.split(",");

                Set<String> l = new HashSet<>();
                for (String word : words) {
                    if (word.contains(" ")) {
                        List<String> collect = Arrays.stream(word.split(" ")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                        l.addAll(collect);
                        continue;
                    }
                    l.add(word);
                }
                result.put(k.get(sp[0].trim().toLowerCase()), l);
            }
        }
        return result;
    }

    private static String clean(String text) {
        String[] labels = text.replaceAll("،", ",").replaceAll("\\.", "").split("\n");

        StringBuilder builder = new StringBuilder("");
        for (int i = 0, labelsLength = labels.length; i < labelsLength; i++) {
            String line = labels[i];
            if (line.contains(":")) {
                String[] c = line.split(":");

                String data = "";
                for (int x = i + 1; x < labelsLength; x++) {
                    String a = labels[x];
                    if (a.contains(":")) break;
                    data += " " + a;
                }
                if (c.length == 2) {
                    data = c[1] + " " + data;
                }

                builder.append(c[0]).append(" : ").append(data).append("\n");
            }
        }

        return builder.toString();
    }

    public static void main(String[] args) throws IllegalAccessException, IOException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        NER rena = buildRenA();
        NER basic = buildBaselineNER();

        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("evaluation_new.csv")));
        writer.write(",,,RenA,,,Basic NER\n");
        writer.write(",,Precision,Recall,F1,Precision,Recall,F1\n");

        Files.newDirectoryStream(new File("/Users/s0ul/Documents/Programming/Research/commons/evaluation/").toPath()).
                forEach(p -> {
                    try {
                        File file = p.toFile();
                        String text = ArabicMarshall.normalize(AIO.readUTF8EncodedFile(file));
                        System.out.println(file.getName());
                        String[] content = text.split("\n\n");
                        if (content.length == 1) {
                            return;
                        }
                        Map<String, Set<String>> metadata = getMetadata(text);

                        Map<String, Tuple<Integer, Set<String>>> rena_eval = evaluate(rena, content[0], metadata);
                        Map<String, Tuple<Integer, Set<String>>> basic_eval = evaluate(basic, content[0], metadata);

                        Map<String, Map<String, Double>> r_phi = computePhi(rena_eval, metadata);
                        Map<String, Map<String, Double>> b_phi = computePhi(basic_eval, metadata);

                        Map<String, Double> r_org = r_phi.get("ORG");
                        Map<String, Double> b_org = b_phi.get("ORG");
                        Map<String, Double> r_pers = r_phi.get("PERS");
                        Map<String, Double> b_pers = b_phi.get("PERS");
                        Map<String, Double> r_loc = r_phi.get("LOC");
                        Map<String, Double> b_loc = b_phi.get("LOC");

                        writer.write(String.format("%s,PERS,%f,%f,%f,%f,%f,%f\n", FilenameUtils.removeExtension(file.getName()),
                                r_pers.get("PRECISION"), r_pers.get("RECALL"), r_pers.get("F1"),
                                b_pers.get("PRECISION"), b_pers.get("RECALL"), b_pers.get("F1")));



                        writer.write(String.format(",LOC,%f,%f,%f,%f,%f,%f\n",
                                r_loc.get("PRECISION"), r_loc.get("RECALL"), r_loc.get("F1"),
                                b_loc.get("PRECISION"), b_loc.get("RECALL"), b_loc.get("F1")));

                        writer.write(String.format(",ORG,%f,%f,%f,%f,%f,%f\n",
                                r_org.get("PRECISION"), r_org.get("RECALL"), r_org.get("F1"),
                                b_org.get("PRECISION"), b_org.get("RECALL"), b_org.get("F1")));

                        //train(metadata);
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        writer.close();
    }

    private static void train(Map<String, Set<String>> metadata) throws IOException {
        Map<String, Writer> writer = new HashMap<>();
        writer.put("PERS", new BufferedWriter(new FileWriterWithEncoding(new File("commons/ANER/gazetteer/person/include/extra"), "UTF-8", true)));
        writer.put("ORG", new BufferedWriter(new FileWriterWithEncoding(new File("commons/ANER/gazetteer/organization/include/extra"), "UTF-8", true)));
        writer.put("LOC", new BufferedWriter(new FileWriterWithEncoding(new File("commons/ANER/gazetteer/location/include/extra"), "UTF-8", true)));

        for (String key : metadata.keySet()) {
            Writer w = writer.get(key);
            for (String word : metadata.get(key)) {
                w.write(word + "\n");
            }
            w.flush();
            w.close();
        }
    }

    private static Map<String, Map<String, Double>> computePhi(Map<String, Tuple<Integer, Set<String>>> eval, Map<String, Set<String>> metadata) {
        Map<String, Map<String, Double>> map = new HashMap<>();
        for (String key : Arrays.asList("PERS", "LOC", "ORG")) {
            Tuple<Integer, Set<String>> t = eval.getOrDefault(key, new Tuple<>(0, new HashSet<>()));

            Set<String> retrieved = t.second() == null ? new HashSet<>() : t.second();
            Set<String> relevant = metadata.getOrDefault(key, new HashSet<>());

            int rr = t.first();
            Set<String> r = new HashSet<>(relevant);
            r.retainAll(retrieved);
            double precision = (double) r.size() / rr;
            double recall = (double) r.size() / relevant.size();

            double f1 = (2 * recall * precision) / (precision + recall);
            Map<String, Double> m = map.getOrDefault(key, new LinkedHashMap<>());
            m.put("PRECISION", precision);
            m.put("RECALL", recall);
            m.put("F1", f1);
            map.put(key, m);
        }
        return map;
    }
}