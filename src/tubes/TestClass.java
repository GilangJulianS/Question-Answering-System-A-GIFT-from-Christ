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
        
        String line = "\"Dapatkan Diskon 30% at Toko Sepatu Bata OPI Mall, promo berlaku s.d Tgl 8 Nov 2015 [pic] — https://t.co/A0XIeKcvYU\",6.62115E+17,OPIMall,05/11/2015 10:52,,,,";
        String[] temp = line.split(",6.62");    
        line = temp[0];
        line = line.replace("#diskon", "diskon");   
        line = line.replace("#promo", "promo");
        line = line.replaceAll("#[\\w]*|[\\S]+\\.[\\S]+/[\\S]+", " "); // remove hashtag + url
        line = line.replaceAll("[!\"$'()*+/:;,<=>?@\\^_`{|}]+", " "); //remove punctiation
        List<String[]> sentence = IndonesianPOSTagger.doPOSTag(line);
        Entity e = TestPostTag.extractEntity(sentence);
//        for(String[] word : sentence){
//            for(String s : word){
//                System.out.print(s + " ");
//            }
//        }
        System.out.println(" discount: " + e.discount);
    }
}
