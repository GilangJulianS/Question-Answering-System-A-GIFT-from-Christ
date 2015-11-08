package crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author christangga
 */
public class Parser {

    public static void main(String[] args) {
        String csvFile = "data/jokowi_sort_uniq.csv";
        String cvsSplitBy = ",";
        HashSet<String> tweetSet = new HashSet<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter("data/jokowi_sort_uniq_filtered.csv"));

            String line = br.readLine();
            writer.write(line + "\n");
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] tweet = line.split(cvsSplitBy);
                tweet[0] = tweet[0].substring(1, tweet[0].length() - 1);

                boolean isRT = false;
                List<String> tweetStatus = new LinkedList<>(Arrays.asList(tweet[0].replace(",", "").replace(":", "").split("\\s+")));
                for (int j = tweetStatus.size() - 1; j >= 0; --j) {
                    if (tweetStatus.get(j).equals("RT")) {
                        isRT = true;
                        break;
                    }
                    if (tweetStatus.get(j).contains("http") || tweetStatus.get(j).startsWith("#") || tweetStatus.get(j).endsWith("..")) {
                        tweetStatus.remove(j);
                    }
                }

                if (!isRT && tweetSet.add(String.join(" ", tweetStatus))) {
                    System.out.println("'" + String.join(" ", tweetStatus) + "'," + tweet[1] + "," + tweet[2] + "," + tweet[3] + "," + tweet[4] + "," + tweet[5]);
                    writer.write("'" + String.join(" ", tweetStatus) + "'," + tweet[1] + "," + tweet[2] + "," + tweet[3] + "," + tweet[4] + "," + tweet[5] + "\n");
                }
                // System.out.println(String.join(" ", tweetStatus));
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Done");
    }
}
