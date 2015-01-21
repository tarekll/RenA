import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

import java.io.IOException;
import java.util.List;

public class NERDemo {

    public static void main(String[] args) throws IOException {

        String serializedClassifier = "commons/classifiers/english.muc.7class.distsim.crf.ser.gz";

        CRFClassifier<CoreMap> classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);

      /* For either a file to annotate or for the hardcoded text example,
         this demo file shows two ways to process the output, for teaching
         purposes.  For the file, it shows both how to run NER on a String
         and how to run it on a whole file.  For the hard-coded String,
         it shows how to run it on a single sentence, and how to do this
         and produce an inline XML output format.
      */

        System.out.println(classifier.classifyToString("Go back, Sam. I'm going to Mordor alone."));
    }

}