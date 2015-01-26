import arabic.io.AIO;
import arabic.ner.RenA;
import arabic.ner.Tuple;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * s0ul on 1/13/15.
 */
public class PDFE {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        FileFilter filter = file -> file.getName().endsWith(".pdf");
        File[] files = new File("/Users/s0ul/Documents/Programming/Research/commons/sample/aner_pdf/").listFiles(filter);
        //List<File> files = random_sample(Arrays.asList(f), 4);
        for (File file : files) {
            System.out.println(file.getName());
            AIO.automate(file, new File("commons/sample/aner_articles/"));
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
