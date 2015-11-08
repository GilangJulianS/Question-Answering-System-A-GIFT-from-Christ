/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QuestionAnswering;

import IndonesianNLP.IndonesianPOSTagger;
import IndonesianNLP.IndonesianPhraseChunker;
import IndonesianNLP.IndonesianSentenceFormalization;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Entity;

/**
 *
 * @author gilang
 */
public class InformationExtractor {
    
    private static final int DIR_FORWARD = 0;
    private static final int DIR_BACKWARD = 1;
    private static final int DIR_BOTH = 2;
    
    public static void main(String[] args) throws FileNotFoundException, IOException{
        int counter = 1;
        
        IndonesianSentenceFormalization formalizer = new IndonesianSentenceFormalization();
        IndonesianPhraseChunker chunker = new IndonesianPhraseChunker();
        
        BufferedWriter writer = new BufferedWriter(new FileWriter("out/gilapromobaru.txt"));
        
        BufferedReader reader = new BufferedReader(new FileReader("res/gilapromobaru.csv"));
        String line;
        
        // baca header csv
        line = reader.readLine();
        
        while((line = reader.readLine()) != null){
            String[] temp = line.split("\",");
            
            line = temp[0];
            line = line.replace("(?i)#diskon", "diskon");   
            line = line.replace("(?i)#promo", "promo");
            line = line.replaceAll("@[\\w]*|#[\\w]*|[\\S]+\\.[\\S]+/[\\S]+", " "); // remove hashtag + url
            line = line.replaceAll("[!\"$'()*+/:;,<=>?@\\^_`{|}]+", " "); //remove punctiation
            line = line.replaceAll("(?i)hari|temukan|ini|gebyar|cuma|hanya|rp.", "");
            
            counter++;
            if(!line.equals("") && line != null){
                List<String[]> sentence = IndonesianPOSTagger.doPOSTag(line);
//                printSentence(writer, sentence);
                Entity e = extractEntity(sentence);
                e.date = temp[3].replace("\"", "").split(" ")[0];
                if(e.item != null){
                    writer.write(e.item + "||");
                    writer.write(e.discount + "||");
                    writer.write(e.discountPercent + "||");
                    writer.write(e.beligratis + "||");
                    writer.write(e.hargaMulai + "||");
                    writer.write(e.date + "\n");
                }
            }
        }
        reader.close();
        writer.close();
    }
    
    public static void printSentence(BufferedWriter writer, List<String[]> sentence) throws IOException{
        for(String[] word : sentence){
            for(String s : word){
                writer.write(s + " ");
            }
        }
    }
    
    public static Entity extractEntity(List<String[]> sentence){
        Entity e = new Entity();
        boolean discPercentFound = false;
        boolean discFound = false;
        boolean itemFound = false;
        boolean gratisFound = false;
        // iterate every word
        for(int i=0; i<sentence.size(); i++){
            String[] word = sentence.get(i);
            if(word[0].toLowerCase().equals("diskon")||word[0].toLowerCase().equals("cashback")){
                String[] discount = getNearestCDP(sentence, i, DIR_FORWARD);
                if(discount != null){
                    if(discount[0].contains("%") && !discPercentFound){
                        try{
                            e.discountPercent = Integer.valueOf(discount[0].replace("%", ""));
                        }catch(NumberFormatException ex){
                            sentence.remove(discount);
                            i--;
                            continue;
                        }
                        discPercentFound = true;
                    }else if(!discFound){
                        try{
                            e.discount = Integer.valueOf(discount[0].replace(".", ""));
                        }catch(NumberFormatException ex){
                            sentence.remove(discount);
                            i--;
                            continue;
                        }
                        discFound = true;
                    }
                }
                if(!itemFound){
                    String noun = extractNounBackward(sentence, i);
                    if(!noun.equals("")){
                        e.item = noun;
                        itemFound = true;
                    }
                }
            }else if(word[0].toLowerCase().equals("harga")){
                String[] discount = getNearestCDP(sentence, i, DIR_FORWARD);
                if(discount != null){
                    try{
                        e.hargaMulai = Integer.valueOf(discount[0].replace(".", ""));
                    }catch(NumberFormatException ex){
                        sentence.remove(discount);
                        i--;
                    }
                }
                if(!itemFound){
                    String noun = extractNounBackward(sentence, i);
                    if(!noun.equals("")){
                        e.item = noun;
                        itemFound = true;
                    }
                }
            }else if(word[0].toLowerCase().equals("promo") && !itemFound){
                String noun = extractNounBackward(sentence, i);
                if(!noun.equals("")){
                    e.item = noun;
                    itemFound = true;
                }
            }else if(word[0].toLowerCase().equals("beli") && !gratisFound){
                String beligratis = extractBeliGratis(sentence, i);
                if(!beligratis.equals("")){
                    e.beligratis = beligratis;
                    gratisFound = true;
                }
            }else if(word[0].toLowerCase().equals("gratis") && !gratisFound){
                String gratisan = extractGratis(sentence, i);
                if(!gratisan.equals("")){
                    e.beligratis = gratisan;
                    gratisFound = true;
                }
            }
        }
        return e;
    }
    
    public static String[] getNearestCDP(List<String[]> sentence, int curIdx, int dir){
//        System.out.println("called " + sentence.get(curIdx)[0]);
        int idx = -1;
        for(int i=1; i<Math.max(curIdx, sentence.size() - curIdx); i++){
//            System.out.println("i + idx = " + (i + curIdx) + " idx - i = " + (curIdx - i));
            if((dir == DIR_FORWARD || dir == DIR_BOTH) && (i + curIdx < sentence.size() && sentence.get(i + curIdx)[1].contains("CDP"))){
//                System.out.println(sentence.get(i + curIdx)[0]);
                idx = i + curIdx;
                break;
            }else if((dir == DIR_BACKWARD || dir == DIR_BOTH) && (curIdx - i >= 0 && sentence.get(curIdx - i)[1].contains("CDP"))){
//                System.out.println(sentence.get(curIdx - i)[0]);
                idx = curIdx - i;
                break;
            }
        }
        if(idx != -1)
            return sentence.get(idx);
        return null;
    }
    
    public static String extractBeliGratis(List<String[]> sentence, int curIdx){
        String beligratis = "";
        if(sentence.size() > curIdx + 3){
            if(sentence.get(curIdx + 1)[1].contains("CDP") && sentence.get(curIdx + 2)[0].toLowerCase().equals("gratis")){
                beligratis = sentence.get(curIdx)[0] + " " + sentence.get(curIdx + 1)[0] + " " + sentence.get(curIdx + 2)[0] + " " + sentence.get(curIdx + 3)[0];
            }
        }
        return beligratis;
    }
    
    public static String extractGratis(List<String[]> sentence, int curIdx){
        String beligratis = "";
        if(curIdx < sentence.size() - 1 ){
            for(int i=curIdx + 1; i<sentence.size(); i++){
                if(sentence.get(i)[1].equals("NN") || sentence.get(i)[1].equals("NNP")){
                    beligratis += sentence.get(i)[0] + " ";
                }
            }
            return beligratis;
        }
        return "";
    }
    
    public static String extractNounBackward(List<String[]> sentence, int curIdx){
        final int PREV_NNP = 0;
        final int PREV_NN = 1;
        final int PREV_OTHER = 2;
        int prev = -1;
        int startIdx = -1;
        int endIdx = curIdx - 1;
        boolean firstNN = true;
        if(curIdx > 0){
            for(int i=curIdx - 1; i>=0; i--){
//                System.out.println(i + " " + sentence.get(i)[1] + " " + startIdx);
                if(sentence.get(i)[1].contains("NNP")){
                    if(prev == PREV_NN){
                        startIdx = i+1; break;
                    }else if(prev == PREV_NNP){
                        startIdx = i;
                        continue;
                    }
                    startIdx = i;
                    prev = PREV_NNP;
                }else if(sentence.get(i)[1].contains("NN") || sentence.get(i)[1].contains("FW")){
                    if(prev == PREV_NN){
                        startIdx = i;
                        continue;
                    }else if(prev == PREV_NNP){
                        startIdx = i; continue;
                    }
                    startIdx = i;
                    prev = PREV_NN;
                }else{
                    if(prev == PREV_OTHER || prev == -1){
                        prev = PREV_OTHER;
                        endIdx = i - 1;
                    }else{
                        startIdx = i+1; break;
                    }
                }
            }
        }
        String result = "";
        if(startIdx >= 0 && endIdx < curIdx){
            for(int i=startIdx; i<=endIdx; i++){
                result += sentence.get(i)[0] + " ";
            }
        }
        return result;
    }
}
