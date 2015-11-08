/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QuestionAnswering;

import IndonesianNLP.IndonesianPOSTagger;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Entity;

/**
 *
 * @author SPM
 */
public class TopicProcessor {

    public static Question processQuery(String query) {
        Question question = new Question();
        
        query = query.replaceAll("#[\\w]*|[\\S]+\\.[\\S]+/[\\S]+", " "); // remove hashtag + url
        query = query.replaceAll("[!\"$'()*+/:;,?@\\^_`{|}]+", " "); //remove punctiation
        query = query.replace(">", "lebih dari");
        query = query.replace("<", "kurang dari");
        query = query.replace("=", "sama dengan");
        query = query.replaceAll("\\s*persen", "%");
        
        
        if(query.contains("diskon") || query.contains("potongan") || query.contains("cashback")){
            question.type = "diskon";
        }else if(query.contains("promo") || query.contains("mulai dari") || query.contains("harga")){
            question.type = "promo";
        }
        
        query = query.replaceAll("(?i)diskon|promo|tanggal|mulai|dari|cashback|sale", " ");
        
        query = convertDate(query);
        System.out.println("new query: " + query);
        List<String[]> sentence = IndonesianPOSTagger.doPOSTag(query);
        for(int i = 0; i < sentence.size(); i++){
            String[] word = sentence.get(i);
//            System.out.println(word[0] + " " + word[1]);
            if(word[1].equalsIgnoreCase("WP")) {
                question.WP = word[0];
            }
            if(word[1].equalsIgnoreCase("NN")){
                question.merchant = word[0];
            }
            if(word[1].equalsIgnoreCase("RB")){
                question.operan = word[0];
            }                     
            if(word[1].equalsIgnoreCase("CDP") && !isDate(word[0])){
                if(word[0].contains("%"))
                    question.percent = Integer.valueOf(word[0].replace("%", ""));
                else
                    question.numb = Integer.valueOf(word[0]);
            }
            if(word[1].equalsIgnoreCase("CDP") && isDate(word[0])){
                question.tanggal = word[0];
            }
        }
        
        return question;
    }
    
    public static String convertDate(String sentence){
        String s = sentence;
        String datePattern = "(\\d{1,2}|\\d{4})\\W((?i)Januari|Februari|Maret|April|Mei|Juni|Juli|Agustus|September|Oktober|November|Desember|\\d{1,2})\\W(\\d{4}|\\d{1,2})";
        Pattern p = Pattern.compile(datePattern);
        Matcher m = p.matcher(sentence);
        while(m.find()){
            String date = m.group(0);
            String newDate = formatDate(date);
            s = s.replaceAll(date, newDate);
        }
        return s;
    }
    
    public static String formatDate(String date){
        String d = date;
        d = d.replaceAll("(?i)januari", "01");
        d = d.replaceAll("(?i)februari", "02");
        d = d.replaceAll("(?i)maret", "03");
        d = d.replaceAll("(?i)april", "04");
        d = d.replaceAll("(?i)mei", "05");
        d = d.replaceAll("(?i)juni", "06");
        d = d.replaceAll("(?i)juli", "07");
        d = d.replaceAll("(?i)agustus", "08");
        d = d.replaceAll("(?i)september", "09");
        d = d.replaceAll("(?i)oktober", "10");
        d = d.replaceAll("(?i)november", "11");
        d = d.replaceAll("(?i)desember", "12");
        d = d.replaceAll("\\W", "-");
        String[] test = d.split("-");
        try{
            if(test[0].length() == 2 && test[2].length() == 4){
                String temp = test[0];
                test[0] = test[2];
                test[2] = temp;
                d = test[0] + "-" + test[1] + "-" + test[2];
            }
        }catch(Exception e){
            
        }
        return d;
    }
    
    public static void main(String[] args){
	  processQuery("Dimana ada diskon kurang dari 90%?");
    }

    public static boolean isDate(String dateString){
	  boolean correct = true;
	  String datePattern = "(\\d{1,2}|\\d{4})\\W((?i)Januari|Februari|Maret|April|Mei|Juni|Juli|Agustus|September|Oktober|November|Desember|\\d{1,2})\\W(\\d{4}|\\d{1,2})";
          Pattern p = Pattern.compile(datePattern);
          Matcher m = p.matcher(dateString);
	  return m.find();
    }
}
