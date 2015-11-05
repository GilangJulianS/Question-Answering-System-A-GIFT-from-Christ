/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tubes;

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
public class TestPostTag {
    
    public static void main(String[] args) throws FileNotFoundException, IOException{
        int counter = 1;
        
        IndonesianSentenceFormalization formalizer = new IndonesianSentenceFormalization();
        IndonesianPhraseChunker chunker = new IndonesianPhraseChunker();
        
        BufferedWriter writer = new BufferedWriter(new FileWriter("out/diskon.txt"));
        
        BufferedReader reader = new BufferedReader(new FileReader("res/diskon.csv"));
        String line;
        while((line = reader.readLine()) != null){
            String[] temp = line.split("\",");
            
            line = temp[0];
            line = line.replace("(?i)#diskon", "diskon");   
            line = line.replace("(?i)#promo", "promo");
            line = line.replaceAll("@[\\w]*|#[\\w]*|[\\S]+\\.[\\S]+/[\\S]+", " "); // remove hashtag + url
            line = line.replaceAll("[!\"$'()*+/:;,<=>?@\\^_`{|}]+", " "); //remove punctiation
            line = line.replaceAll("(?i)hari|temukan|ini|gebyar|cuma|hanya|rp.", "");
            
            System.out.println(line);
            
            writer.write(counter + " ");
            counter++;
            if(!line.equals("") && line != null){
                List<String[]> sentence = IndonesianPOSTagger.doPOSTag(line);
                printSentence(writer, sentence);
                Entity e = extractEntity(sentence);
                writer.write("\ndiscount: " + e.discount + ", " + e.beligratis + "\n");
                writer.write("entity: " + e.item);
            }
            writer.write("\n");
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
        boolean discFound = false;
        boolean itemFound = false;
        boolean gratisFound = false;
        // iterate every word
        for(int i=0; i<sentence.size(); i++){
            String[] word = sentence.get(i);
            if((word[0].toLowerCase().equals("diskon")||word[0].toLowerCase().equals("harga"))){
                if(!discFound){
                    String[] discount = getNearestCDP(sentence, i);
                    if(discount != null){
                        e.discount = discount[0];
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
            }
        }
        
        return e;
    }
    
    public static String[] getNearestCDP(List<String[]> sentence, int curIdx){
//        System.out.println("called " + sentence.get(curIdx)[0]);
        int idx = -1;
        for(int i=1; i<Math.max(curIdx, sentence.size() - curIdx); i++){
//            System.out.println("i + idx = " + (i + curIdx) + " idx - i = " + (curIdx - i));
            if(i + curIdx < sentence.size() && sentence.get(i + curIdx)[1].contains("CDP")){
//                System.out.println(sentence.get(i + curIdx)[0]);
                idx = i + curIdx;
                break;
            }else if(curIdx - i >= 0 && sentence.get(curIdx - i)[1].contains("CDP")){
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
                }else if(sentence.get(i)[1].contains("NN")){
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
