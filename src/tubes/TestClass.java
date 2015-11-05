/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tubes;

import IndonesianNLP.IndonesianPOSTagger;
import IndonesianNLP.IndonesianPhraseChunker;
import java.util.List;
import model.Entity;
import static tubes.TestPostTag.extractEntity;
import static tubes.TestPostTag.printSentence;

/**
 *
 * @author gilang
 */
public class TestClass {
    
    public static void main(String[] args){
        
        String line = "\"#diskon Lazada Weekly Gadget Promo - Diskon Hingga 40%\",\"662249947459411968\",\"GilaPromoDotCom\",\"2015-11-05 19:47:46\",\"\",\"\"";
        String[] temp = line.split("\",");    
        line = temp[0];
         line = line.replace("(?i)#diskon", "diskon");   
            line = line.replace("(?i)#promo", "promo");
            line = line.replaceAll("@[\\w]*|#[\\w]*|[\\S]+\\.[\\S]+/[\\S]+", " "); // remove hashtag + url
            line = line.replaceAll("[!\"$'()*+/:;,<=>?@\\^_`{|}]+", " "); //remove punctiation
            line = line.replaceAll("(?i)cuma|hanya|rp.", "");
        List<String[]> sentence = IndonesianPOSTagger.doPOSTag(line);
        Entity e = TestPostTag.extractEntity(sentence);
        for(String[] word : sentence){
            for(String s : word){
                System.out.print(s + " ");
            }
        }
        System.out.println(" discount: " + e.discount + " item: " + e.item);
    }
}
