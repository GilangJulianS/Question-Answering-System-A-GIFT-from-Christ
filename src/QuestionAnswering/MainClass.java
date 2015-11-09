/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QuestionAnswering;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import model.Entity;

/**
 *
 * @author gilang
 */
public class MainClass {

    public static void main(String[] args) throws FileNotFoundException, IOException {

        List<Entity> entities = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader("out/gilapromobaru.txt"));
        Scanner scanner = new Scanner(System.in);
        String line, input;

        while ((line = reader.readLine()) != null) {
            Entity e = new Entity();
            String[] atts = line.split("\\|\\|");
            e.item = atts[0];
            e.discount = Integer.valueOf(atts[1]);
            e.discountPercent = Integer.valueOf(atts[2]);
            e.beligratis = atts[3];
            e.hargaMulai = Integer.valueOf(atts[4]);
            e.date = atts[5];
            System.out.println(e.item);
            System.out.println(e.discount + " " + e.discountPercent + " " + e.beligratis + " " + e.hargaMulai + " " + e.date);
            entities.add(e);
        }

        reader.close();
        System.out.println("Silahkan masukkan pertanyaan anda:");
        input = scanner.nextLine();
        while (!input.equals("exit")) {
            Question q = TopicProcessor.processQuery(input);
            System.out.println("WP " + q.WP);
            System.out.println("Merchant " + q.merchant);
            System.out.println("Operan " + q.operan);
            System.out.println("Num " + q.numb);
            System.out.println("Percent " + q.percent);
            System.out.println("Tanggal " + q.tanggal);
            System.out.println("Type " + q.type);
            writeAnswer(getAnswer(q, entities));
            System.out.println("Silahkan masukkan pertanyaan anda:");

            input = scanner.nextLine();
        }
    }

    public static List<String> getAnswer(Question q, List<Entity> entities) {
        List<String> answers = new ArrayList<>();
        List<Entity> filtered = null;
        if (!q.tanggal.equals("")) {
            if (filtered != null) {
                filtered = filterDate(q.tanggal, filtered);
            } else {
                filtered = filterDate(q.tanggal, entities);
            }
        }else{
            filtered = new ArrayList<>();
            filtered.addAll(entities);
        }
        if (!q.merchant.equals("")) {
            filtered = filterMerchant(q.merchant, filtered);
        }
        if (!q.operan.equals("")) {
            filtered = filterNumber(q, filtered);
        }
        if (q.type.equals("diskon")) {
            for (Entity e : filtered) {
                String answer = "";
                if (e.discount != 0 && q.numb != 0) {
                    answer += "diskon Rp. " + String.valueOf(e.discount);
                    if (q.WP.equalsIgnoreCase("dimana")) {
                        answer += " di " + e.item;
                    }
                    if (q.tanggal.equals("")) {
                        answer += " pada " + e.date;
                    }
                    if (!answers.contains(answer)) {
                        answers.add(answer);
                    }
                }
                answer = "";
                if (e.discountPercent != 0 && q.percent != 0) {
                    answer += "diskon " + e.discountPercent + "%";
                    if (q.WP.equalsIgnoreCase("dimana")) {
                        answer += " di " + e.item;
                    }
                    if (q.tanggal.equals("")) {
                        answer += " pada " + e.date;
                    }
                    if (!answers.contains(answer)) {
                        answers.add(answer);
                    }
                }
            }
        }
        if (q.type.equals("promo")) {
            for (Entity e : filtered) {
                String answer = "";
                if (e.hargaMulai != 0) {
                    answer = "harga mulai Rp. " + String.valueOf(e.hargaMulai);
                    if (q.WP.equalsIgnoreCase("dimana")) {
                        answer += " di " + e.item;
                    }
                    if (q.tanggal.equals("")) {
                        answer += " pada " + e.date;
                    }
                    if (!answers.contains(answer)) {
                        answers.add(answer);
                    }
                }
                answer = "";
                if (e.beligratis != null && !e.beligratis.equals("") && !e.beligratis.equals("null")) {
                    answer = e.beligratis;
                    if (!answer.contains("beli")) {
                        answer = "gratis " + e.beligratis;
                    }
                    if (q.WP.equalsIgnoreCase("dimana")) {
                        answer += " di " + e.item;
                    }
                    if (q.tanggal.equals("")) {
                        answer += " pada " + e.date;
                    }
                    if (!answers.contains(answer)) {
                        answers.add(answer);
                    }
                }
            }
        }
        return answers;
    }

    public static List<Entity> filterMerchant(String merchant, List<Entity> es) {
        List<Entity> newList = new ArrayList<>();
        for (Entity e : es) {
            if (e.item.toLowerCase().contains(merchant.toLowerCase())) {
                newList.add(e);
            }
        }
        return newList;
    }

    public static List<Entity> filterNumber(Question q, List<Entity> es) {
        List<Entity> newList = new ArrayList<>();
        for (Entity e : es) {
            if (q.operan.toLowerCase().equals("lebih")) {
                if (q.type.equals("promo")) {
                    if (e.hargaMulai > q.numb && q.numb != 0) {
                        newList.add(e);
                    }
                } else if (q.type.equals("diskon")) {
                    if ((e.discount > q.numb && q.numb != 0) || (e.discountPercent > q.percent && q.percent != 0)) {
                        newList.add(e);
                    }
                } else {
                    if ((e.discount > q.numb && q.numb != 0) || (e.discountPercent > q.percent && q.percent != 0) || (e.hargaMulai > q.numb && q.numb != 0)) {
                        newList.add(e);
                    }
                }
            } else if (q.operan.toLowerCase().equals("kurang")) {
                if (q.type.equals("promo")) {
                    if (e.hargaMulai < q.numb && q.numb != 0) {
                        newList.add(e);
                    }
                } else if (q.type.equals("diskon")) {
                    if ((e.discount < q.numb && q.numb != 0) || (e.discountPercent < q.percent && q.percent != 0)) {
                        newList.add(e);
                    }
                } else {
                    if ((((e.discount < q.numb) || (e.hargaMulai < q.numb)) && q.numb != 0) || (e.discountPercent < q.percent)) {
                        newList.add(e);
                    }
                }
            }
        }
        return newList;
    }

    public static List<Entity> filterDate(String date, List<Entity> es) {
        List<Entity> newList = new ArrayList<>();
        for (Entity e : es) {
            if (date.equals(e.date)) {
                newList.add(e);
            }
        }
        return newList;
    }

    public static void writeAnswer(List<String> answers) {
        for (int i = 0; i < answers.size(); i++) {
            System.out.println((i + 1) + " " + answers.get(i));
        }
        if (answers.size() == 0) {
            System.out.println("Tidak ditemukan");
        }
    }

}
