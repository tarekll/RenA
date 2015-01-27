import arabic.io.AlRayaPDF;
import arabic.ner.RenA;
import arabic.ner.Tuple;
import arabic.stopword.StopWord;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

/**
 * s0ul on 1/13/15.
 */
public class PDFE {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        FileFilter filter = file -> file.getName().endsWith(".pdf");
        File[] files = new File("/Volumes/Tarek/Organized Result/").listFiles(filter);
        //List<File> files = random_sample(Arrays.asList(f), 4);
        RenA ner = RenA.load(new File("commons/demo/ner_demo"));
        ner.addStopWord(new StopWord("commons/stopwords/"));
        int length = files.length;
        for (int i = 0; i < length; i++) {
            File file = files[i];
            System.out.printf("[%d/%d] %s\n", i, length, file.getName());
            AlRayaPDF.parse(file, new File("/Volumes/Tarek/Articles/"), text -> {
                Set<Tuple<String, Set<String>>> tag = ner.uniqueTag(text);
                double unknown = tag.stream().map(Tuple::second).filter(s -> s.contains("UNKNOWN")).count();
                double ratio = unknown / tag.size();
                return ratio <= .65 && text.split("\n").length >= 10;
            });
        }
    }

    public static <T> List<T> random_sample(List<T> input, int subsetSize) {
        SecureRandom r = new SecureRandom();
        int inputSize = input.size();
        for (int i = 0; i < subsetSize; i++) {
            int indexToSwap = i + r.nextInt(inputSize - i);
            T temp = input.get(i);
            input.set(i, input.get(indexToSwap));
            input.set(indexToSwap, temp);
        }
        return input.subList(0, subsetSize);
    }
}
