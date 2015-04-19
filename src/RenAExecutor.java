import arabic.io.AIO;
import arabic.ner.RenA;
import arabic.stopword.StopWord;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * s0ul on 4/16/15.
 */
public class RenAExecutor {
    public static void main(String[] args) throws Exception {
        //entities ngram name_bool org_bool loc_bool fileOrFolder classifier stopwords output_dir
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        int length = args.length;
        if (length >= 8 && args[0].equalsIgnoreCase("entities")) {
            RenA ner = RenA.load(new File(args[6]));
            ner.addStopWord(new StopWord(args[7]));

            int ngram = Integer.valueOf(args[1]);
            List<String> list = new ArrayList<>();
            if (Boolean.valueOf(args[2]) || args[2].equalsIgnoreCase("1")) list.add("PERS");
            if (Boolean.valueOf(args[3]) || args[3].equalsIgnoreCase("1")) list.add("ORG");
            if (Boolean.valueOf(args[4]) || args[4].equalsIgnoreCase("1")) list.add("LOC");

            File[] files = null;
            File file = new File(args[5]);
            if (file.isDirectory()) {
                files = file.listFiles();
            }

            for (File f : files != null ? files : new File[] {file}) {
                String content = AIO.readUTF8EncodedFile(f);
                Map<String, Set<String>> extract = ner.extract(content, ngram, list.toArray(new String[list.size()]));

                Map<String, Object> map = new HashMap<>();
                map.put("title", Suite.getTitle(content));
                map.put("date", Suite.getDate(content));
                map.put("author", Suite.getAuthor(ner, content));

                for (String key : extract.keySet()) {
                    map.put(key, extract.get(key));
                }

                String result = gson.toJson(map);
                if (length != 9)
                    System.out.println(result);
                else {
                    File out = new File(args[8], FilenameUtils.getBaseName(f.getName()) + "_attribtue_ner.json");
                    writeToFile(out, result);
                }
            }
        } else {
            System.out.println("Invalid Parameters!");
            System.out.println("\tentities [ngram] [name_ent_bool] [org_ent_bool] [loc_ent_bool] [FileOrFolder] [classifier] [stopwords] (output_path)");
        }
    }

    private static void writeToFile(File file, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(file, "UTF-8"));
        writer.write(content);
        writer.close();
    }
}
