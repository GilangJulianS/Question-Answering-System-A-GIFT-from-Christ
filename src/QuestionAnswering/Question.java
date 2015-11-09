/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QuestionAnswering;

/**
 *
 * @author Ivana Clairine
 */
public class Question {
    public String WP; //kata tanya
    public int numb; // jika ada angka
    public int percent; // jika ada angka (dalam format persen)
    public String operan; // besar atau kecil
    public String merchant; // nama toko
    public String tanggal; // tanggal spesifik yang diinginkan
    public String type;
    
    public Question(){
        WP = "";
        numb = 0;
        operan = "";
        merchant = "";
        tanggal = "";
        percent = 0;
        type = "";
    }
    
}
