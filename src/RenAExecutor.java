import arabic.io.AIO;
import arabic.ner.RenA;
import arabic.stopword.StopWord;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
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
        //entities ngram name_bool org_bool loc_bool fileOrFolder classifier stopwords
        //attributes filenameOrFolder classifier stopwords
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        int length = args.length;
        if (length == 8 && args[0].equalsIgnoreCase("entities")) {
            RenA ner = RenA.load(new File(args[6]));
            ner.addStopWord(new StopWord(args[7]));

            int ngram = Integer.valueOf(args[1]);
            List<String> list = new ArrayList<>();
            if (Boolean.valueOf(args[2])) list.add("PERS");
            if (Boolean.valueOf(args[3])) list.add("ORG");
            if (Boolean.valueOf(args[4])) list.add("LOC");

            File[] files = null;
            File file = new File(args[5]);
            if (file.isDirectory()) {
                files = file.listFiles();
            }

            for (File f : files != null ? files : new File[] {file}) {
                String content = AIO.readUTF8EncodedFile(f);

                Map<String, Set<String>> extract = ner.extract(content, ngram, list.toArray(new String[list.size()]));

                String result = gson.toJson(extract);
                result = String.format("{\n  \"filename\":\"%s\",", f.getName()) + result.substring(1);
                System.out.println(result);
            }
        } else if (length == 4 && args[0].equalsIgnoreCase("attributes")) {
            RenA ner = RenA.load(new File(args[2]));
            ner.addStopWord(new StopWord(args[3]));

            File[] files = null;
            File file = new File(args[1]);
            if (file.isDirectory()) {
                files = file.listFiles();
            }

            for (File f : files != null ? files : new File[] {file}) {
                String content = AIO.readUTF8EncodedFile(f);

                Map<String, String> map = new HashMap<>();
                map.put("title", Suite.getTitle(content));
                map.put("date", Suite.getDate(content));
                map.put("author", Suite.getAuthor(ner, content));
                map.put("filename", f.getName());

                System.out.println(gson.toJson(map));
            }
        } else {
            System.out.println("Invalid Parameters!");
            System.out.println("\tentities [ngram] [name_ent_bool] [org_ent_bool] [loc_ent_bool] [FileOrFolder] [classifier] [stopwords]");
            System.out.println("\tattributes [FileOrFolder] [classifier] [stopwords]");
        }
    }
}
