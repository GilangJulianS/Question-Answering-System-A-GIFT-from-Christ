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
    public int numb; //kalo ada angka
    public String operan; //besar atau kecil
    public String merchant; //nama toko
    public String tanggal;
    
    public Question(){
        WP = "";
        numb = 0;
        operan = "";
        merchant = "";
        tanggal="";
    }
    
}
