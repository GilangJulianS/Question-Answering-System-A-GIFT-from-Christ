/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QuestionAnswering;

import IndonesianNLP.IndonesianPOSTagger;
import java.util.List;
import model.Entity;
import tubes.TestPostTag;

/**
 *
 * @author SPM
 */
public class TopicProcessor {

    public static void processQuery(String query) {
	  query = query.replaceAll("#[\\w]*|[\\S]+\\.[\\S]+/[\\S]+", " "); // remove hashtag + url
	  query = query.replaceAll("[!\"$'()*+/:;,<=>?@\\^_`{|}]+", " "); //remove punctiation
	  
	  List<String[]> sentence = IndonesianPOSTagger.doPOSTag(query);
	  for(int i = 0; i < sentence.size(); i++){
                Question Q = new Question();
		System.out.println(sentence.get(i)[0] + " " + sentence.get(i)[1]);
                if(sentence.get(i)[1].equalsIgnoreCase("WP")) {
                    Q.WP = sentence.get(i)[0];
                }
                if(sentence.get(i)[1].equalsIgnoreCase("RB")){
                    Q.operan = sentence.get(i)[0];
                }                     
                if(sentence.get(i)[1].equalsIgnoreCase("CDP") && !isDate(sentence.get(i)[1])){
                    Q.numb = Integer.valueOf(sentence.get(i)[0]);
                }
                if(sentence.get(i)[1].equalsIgnoreCase("CDP") && !isDate(sentence.get(i)[1])){
                    Q.tanggal = sentence.get(i)[0];
                }
	  }
	  
    }
    
    public static void main(String[] args){
	  processQuery("Dimana ada diskon kurang dari 90%?");
    }

    public static boolean isDate(String dateString){
	  boolean correct = true;
	  String datePattern = "(\\d2|\\d4)\\W(Januari|Februari|Maret|April|Mei|Juni|Juli|Agustus|September|Oktober|November|Desember|\\d2)\\W(\\d2|\\d4)";
	  return dateString.matches(datePattern);
    }
}
