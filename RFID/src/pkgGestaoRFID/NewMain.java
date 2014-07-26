/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkgGestaoRFID;

/**
 *
 * @author Rafa
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

//        byte i = 26;
//        int b = (int)i;
//        System.out.println("int-> " + b);
//        
//        int teste = Integer.parseInt("12", 16);
//        System.out.println("teste-> " + teste );
//        
//        byte teste2 = Byte.parseByte("63", 16);
//        System.out.println("Teste2 -> " + teste2);
        // Substring replacement. 
        String org = "This is a test. This is, too.";
        String search = "is";
        String sub = "was";
        String result = "";
        int i;
        do { // replace all matching substrings 
            System.out.println(org);
            i = org.indexOf(search);
            if (i != -1) {
                result = org.substring(0, i);
                result = result + sub;
                result = result + org.substring(i + search.length());
                org = result;
            }
        } while (i != -1);
    }
}


