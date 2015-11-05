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
		System.out.println(sentence.get(i)[0] + " " + sentence.get(i)[1]);
	  }
	  
    }
    
    public static void main(String[] args){
	  processQuery("Berapa diskon pizza tanggal 21-11-2012?");
    }
}
