package arabic.stopword;

import arabic.io.AIO;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * s0ul on 1/9/15.
 */
public class StopWord extends HashSet<String> {
    public StopWord(String directory) throws IOException {
        super(AIO.readAllLinesInDirectory(new File(directory)));
    }
}
